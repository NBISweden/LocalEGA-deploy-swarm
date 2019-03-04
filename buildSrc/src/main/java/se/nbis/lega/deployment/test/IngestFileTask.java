package se.nbis.lega.deployment.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.xmlpull.v1.XmlPullParserException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.NoResponseException;
import lombok.extern.slf4j.Slf4j;
import se.nbis.lega.deployment.cluster.Machine;

@Slf4j
public class IngestFileTask extends TestTask {

    public IngestFileTask() {
        super();
        this.dependsOn("upload");
    }

    @TaskAction
    public void run() throws IOException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException,
                    TimeoutException, InvalidKeyException, XmlPullParserException, InvalidPortException,
                    ErrorResponseException, NoResponseException, InvalidBucketNameException, InsufficientDataException,
                    InvalidEndpointException, InternalException, InterruptedException {
        System.out.println("Starting ingestion...");
        String host = getProperty("legaPrivateIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        int expectedAmount = getFilesAmount(host) + 1;
        ingest();

        int maxAttempts = 120;
        while ((getFilesAmount(host) != expectedAmount)) {
            if (maxAttempts-- == 0) {
                throw new GradleException("File is not ingested!");
            }
            Thread.sleep(1000);
        }
        System.out.println("File seems to be ingested successfully: trying to download it...");
        String url = String.format("http://%s:8081/file?sourceKey=%s&sourceIV=%s&filePath=%s", host,
                        readTrace("sessionKey"), readTrace("iv"), expectedAmount);
        System.out.println("URL : " + url);
        URL resURL = new URL(url);
        File downloadedFile = getProject().file(".tmp/data.raw.out");
        FileUtils.copyURLToFile(resURL, downloadedFile);
        boolean equals = FileUtils.contentEquals(getRawFile(), downloadedFile);
        if (!equals) {
            throw new GradleException("The retrieved file doesn't match the original one!");
        }
        System.out.println("File is downloaded successfully!");
        byte[] bytes = DigestUtils.sha256(FileUtils.openInputStream(downloadedFile));
        System.out.println("Checksum: " + Hex.encodeHexString(bytes));
        System.out.println("Files are identical.");
    }

    private void ingest() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException,
                    TimeoutException {
        try {
            String host = getProperty(CEGA_IP);
            String mqPassword = readTrace(getProject().file(CEGA_TMP_TRACE), CEGA_MQ_PASSWORD);
            String mqConnectionString;
            String username;

            if (getProperty(TEST_CEGA) == null) {
                if (host == null) {
                    host = getMachineIPAddress(Machine.CEGA.getName());
                }
                if (mqPassword != null) {
                    mqConnectionString = String.format("amqp://lega:%s@%s:5672/lega", mqPassword, host);
                    username = "john";
                } else {
                    mqConnectionString = System.getenv(CEGA_CONNECTION);
                    String password = mqConnectionString.split(":")[2].split("@")[0];
                    mqConnectionString = mqConnectionString.replace(password,
                                    URLEncoder.encode(password, Charset.defaultCharset().displayName()));
                    username = "dummy";
                }
            } else {
                username = "norway1";
                String password = System.getenv(CEGA_MQ_PASSWORD);
                String port = "5271";
                String vhost = "norway1";
                host = "hellgate.crg.eu";
                mqConnectionString = String.format("amqp://%s:%s@%s:%s/%s", username, password, host, port, vhost);
                writeTrace(CEGA_CONNECTION, mqConnectionString);
            }
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(mqConnectionString);
            Connection connectionFactory = factory.newConnection();
            Channel channel = connectionFactory.createChannel();
            AMQP.BasicProperties properties =
                            new AMQP.BasicProperties().builder().deliveryMode(2).contentType("application/json")
                                            .contentEncoding(StandardCharsets.UTF_8.displayName()).build();


            String stableId = "EGAF" + UUID.randomUUID().toString().replace("-", "");
            channel.basicPublish("localega.v1", "files", properties,
                            String.format("{\"user\":\"%s\",\"filepath\":\"data.raw.enc\",\"stable_id\":\"%s\"}",
                                            username, stableId).getBytes());

            channel.close();
            connectionFactory.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private int getFilesAmount(String host) throws XmlPullParserException, IOException, InvalidPortException,
                    InvalidEndpointException, InsufficientDataException, NoSuchAlgorithmException, NoResponseException,
                    InternalException, InvalidKeyException, InvalidBucketNameException, ErrorResponseException {
        File traceFile = getProject().file(LEGA_PRIVATE_TMP_TRACE);
        String accessKey = readTrace(traceFile, VAULT_S3_ACCESS_KEY);
        String secretKey = readTrace(traceFile, VAULT_S3_SECRET_KEY);
        MinioClient minioClient = new MinioClient(String.format("http://%s:9000", host), accessKey, secretKey);
        if (!minioClient.bucketExists(VAULT_S3_BUCKET_NAME)) {
            return 0;
        }
        int size = IterableUtils.size(minioClient.listObjects(VAULT_S3_BUCKET_NAME));
        return size;
    }

    @InputFile
    public File getRawFile() {
        return getProject().file(".tmp/data.raw");
    }

}
