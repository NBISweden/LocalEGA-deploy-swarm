package se.nbis.lega.deployment.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.IterableUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class IngestFileTask extends TestTask {

    public IngestFileTask() {
        super();
        this.dependsOn("upload");
    }

    @TaskAction
    public void run() throws Exception {
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
        System.out.println("File seems to be ingested successfully.");
    }

    private void ingest()
        throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException,
        TimeoutException {
        String mqPassword = readTrace(getProject().file(CEGA_TMP_TRACE), CEGA_MQ_PASSWORD);
        String mqConnectionString;
        String username;
        if (mqPassword != null) { // test pipeline
            String host = getProperty("cegaIP");
            if (host == null) {
                host = getMachineIPAddress(Machine.CEGA.getName());
            }
            mqConnectionString = String.format("amqp://lega:%s@%s:5672/lega", mqPassword, host);
            username = "john";
        } else { // staging pipeline
            mqConnectionString = System.getenv(CEGA_MQ_CONNECTION);
            username = "dummy";
        }
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(mqConnectionString);
        Connection connectionFactory = factory.newConnection();
        Channel channel = connectionFactory.createChannel();
        AMQP.BasicProperties properties =
            new AMQP.BasicProperties().builder().deliveryMode(2).contentType("application/json")
                .contentEncoding(StandardCharsets.UTF_8.displayName()).build();


        String stableId = "EGAF" + UUID.randomUUID().toString().replace("-", "");
        channel.basicPublish("localega.v1", "files", properties, String
            .format("{\"user\":\"%s\",\"filepath\":\"data.raw.enc\",\"stable_id\":\"%s\"}",
                username, stableId).getBytes());

        channel.close();
        connectionFactory.close();
    }

    private int getFilesAmount(String host) throws Exception {
        File traceFile = getProject().file(LEGA_PRIVATE_TMP_TRACE);
        String accessKey = readTrace(traceFile, VAULT_S3_ACCESS_KEY);
        String secretKey = readTrace(traceFile, VAULT_S3_SECRET_KEY);
        MinioClient minioClient =
            new MinioClient(host, 9000, accessKey, secretKey, "us-east-1", true, getOkHttpClient());
        if (!minioClient.bucketExists(VAULT_S3_BUCKET_NAME)) {
            return 0;
        }
        return IterableUtils.size(minioClient.listObjects(VAULT_S3_BUCKET_NAME));
    }

    private OkHttpClient getOkHttpClient() throws Exception {
        SSLContext sslContext;
        TrustManager[] trustManagers;
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        File certFile = getProject().getParent().file("common/.tmp/ssl/CA.cert");
        try (FileInputStream fis = new FileInputStream(certFile)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate =
                (X509Certificate) certificateFactory.generateCertificate(fis);
            keyStore
                .setCertificateEntry(certificate.getSubjectX500Principal().getName(), certificate);
            TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            trustManagers = trustManagerFactory.getTrustManagers();
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
        }
        return new OkHttpClient.Builder()
            .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
            .build();
    }

    @InputFile public File getRawFile() {
        return getProject().file(".tmp/data.raw");
    }

}
