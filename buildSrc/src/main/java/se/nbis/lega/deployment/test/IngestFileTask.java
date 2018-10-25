package se.nbis.lega.deployment.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.xmlpull.v1.XmlPullParserException;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

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

public class IngestFileTask extends LocalEGATask {

    public IngestFileTask() {
        super();
        this.setGroup(Groups.TEST.name());
        this.dependsOn("upload");
    }

    @TaskAction
    public void run() throws IOException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException, TimeoutException, InvalidKeyException, XmlPullParserException, InvalidPortException, ErrorResponseException, NoResponseException, InvalidBucketNameException, InsufficientDataException, InvalidEndpointException, InternalException, InterruptedException {
        String host = getHost();
        int expectedAmount = getFilesAmount(host) + 1;
        ingest(host);

        int maxAttempts = 120;
        while ((getFilesAmount(host) != expectedAmount)) {
            if (maxAttempts-- == 0) {
                throw new GradleException("File is not ingested!");
            }
            Thread.sleep(1000);
        }
        URL resURL = new URL(String.format("http://%s:8081/file?sourceKey=%s&sourceIV=%s&filePath=%s",
                host,
                readTrace("sessionKey"),
                readTrace("iv"),
                expectedAmount));
        File downloadedFile = getProject().file(".tmp/data.raw.out");
        FileUtils.copyURLToFile(resURL, downloadedFile);
        boolean equals = FileUtils.contentEquals(getRawFile(), downloadedFile);
        if (!equals) {
            throw new GradleException("The retrieved file doesn't match the original one!");
        }
    }

    private void ingest(String host) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        String mqPassword = readTrace(getProject().file("cega/.tmp/.trace"), "CEGA_MQ_PASSWORD");
        String mqConnectionString;
        String username;
        if (mqPassword != null) {
            mqConnectionString = String.format("amqp://lega:%s@%s:5672/lega", mqPassword, host);
            username = "john";
        } else {
            mqConnectionString = System.getenv("CEGA_CONNECTION");
            String password = mqConnectionString.split(":")[2].split("@")[0];
            mqConnectionString = mqConnectionString.replace(password, URLEncoder.encode(password, Charset.defaultCharset().displayName()));
            username = "dummy";
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(mqConnectionString);
        Connection connectionFactory = factory.newConnection();
        Channel channel = connectionFactory.createChannel();
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().
                deliveryMode(2).
                contentType("application/json").
                contentEncoding(StandardCharsets.UTF_8.displayName()).
                build();


        String stableId = "EGAF" + UUID.randomUUID().toString().replace("-", "");
        channel.basicPublish("localega.v1",
                "files",
                properties,
                String.format("{\"user\":\"%s\",\"filepath\":\"data.raw.enc\",\"stable_id\":\"%s\"}", username, stableId).getBytes());

        channel.close();
        connectionFactory.close();
    }

    private int getFilesAmount(String host) throws XmlPullParserException, IOException, InvalidPortException, InvalidEndpointException, InsufficientDataException, NoSuchAlgorithmException, NoResponseException, InternalException, InvalidKeyException, InvalidBucketNameException, ErrorResponseException {
        String accessKey = readTrace(getProject().file("lega/.tmp/.trace"), "S3_ACCESS_KEY");
        String secretKey = readTrace(getProject().file("lega/.tmp/.trace"), "S3_SECRET_KEY");
        MinioClient minioClient = new MinioClient(String.format("http://%s:9000", host), accessKey, secretKey);
        if (!minioClient.bucketExists("lega")) {
            return 0;
        }
        return IterableUtils.size(minioClient.listObjects("lega"));
    }

    @InputFile
    public File getRawFile() {
        return getProject().file(".tmp/data.raw");
    }

}
