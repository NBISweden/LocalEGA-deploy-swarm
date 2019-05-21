package se.nbis.lega.deployment.lega.priv;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CreateConfIniConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        generateConfIni();
        createConfig(Config.CONF_INI.getName(), getProject().file(".tmp/conf.ini"));

        createConfig(Config.CA_CERT.getName(), getProject().getParent().file("common/.tmp/ssl/CA.cert"));

        createConfig(Config.VERIFY_CERT.getName(), getProject().getParent().file("common/.tmp/ssl/verify.cert"));
        createConfig(Config.VERIFY_KEY.getName(), getProject().getParent().file("common/.tmp/ssl/verify.key"));

        createConfig(Config.FINALIZE_CERT.getName(), getProject().getParent().file("common/.tmp/ssl/finalize.cert"));
        createConfig(Config.FINALIZE_KEY.getName(), getProject().getParent().file("common/.tmp/ssl/finalize.key"));

        createConfig(Config.INGEST_CERT.getName(), getProject().getParent().file("common/.tmp/ssl/ingest.cert"));
        createConfig(Config.INGEST_KEY.getName(), getProject().getParent().file("common/.tmp/ssl/ingest.key"));
    }

    private void generateConfIni() throws IOException {
        String inboxS3AccessKey = readTrace(INBOX_S3_ACCESS_KEY);
        String inboxS3SecretKey = readTrace(INBOX_S3_SECRET_KEY);
        String vaultS3AccessKey = readTrace(VAULT_S3_ACCESS_KEY);
        String vaultS3SecretKey = readTrace(VAULT_S3_SECRET_KEY);
        String postgresPassword = readTrace(DB_LEGA_IN_PASSWORD);
        File confIni = getProject().file(".tmp/conf.ini");
        FileUtils.write(confIni, String.format(
            "[DEFAULT]\n" + "log = debug\n" + "\n" + "[keyserver]\n" + "port = 8080\n" + "\n"
                + "[quality_control]\n"
                + "keyserver_endpoint = http://keys:8080/keys/retrieve/%%s/private/bin?idFormat=hex\n"
                + "\n" + "[inbox]\n" + "storage_driver = S3Storage\n"
                + "s3_url = http://inbox-s3:9000\n" + "s3_access_key = %s\n"
                + "s3_secret_key = %s\n" + "#region = lega\n" + "\n" + "[archive]\n"
                + "storage_driver = S3Storage\n" + "s3_url = http://vault-s3:9000\n"
                + "s3_access_key = %s\n" + "s3_secret_key = %s\n" + "#region = lega\n" + "\n"
                + "[outgestion]\n" + "# Just for test\n"
                + "keyserver_endpoint = http://keys:8080/keys/retrieve/%%s/private/bin?idFormat=hex\n"
                + "\n" + "## Connecting to Local EGA\n" + "[broker]\n"
                + "connection = amqps://admin:guest@mq:5671/%%2F?heartbeat=0&connection_attempts=30&retry_delay=10\n"
                + "enable_ssl = yes\n" + "verify_peer = yes\n" + "verify_hostname = no\n"
                + "cacertfile = /etc/ega/CA.cert\n" + "certfile = /etc/ega/ssl.cert\n"
                + "keyfile = /etc/ega/ssl.key\n" + "\n" + "[db]\n"
                + "connection = postgres://lega_in:%s@db:5432/lega?application_name=LocalEGA&sslmode=verify-full&sslcert=/etc/ega/ssl.cert&sslkey=/etc/ega/ssl.key.lega&sslrootcert=/etc/ega/CA.cert\n"
                + "try = 30\n" + "try_interval = 1\n", inboxS3AccessKey, inboxS3SecretKey,
            vaultS3AccessKey, vaultS3SecretKey, postgresPassword), Charset.defaultCharset());
    }

}
