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
                + "\n" + "[inbox]\n" + "storage_driver = S3Storage\n" + "url = http://inbox-s3:9000\n"
                + "access_key = %s\n" + "secret_key = %s\n" + "#region = lega\n" + "\n"
                + "[archive]\n" + "storage_driver = S3Storage\n" + "s3_url = http://vault-s3:9000\n"
                + "s3_access_key = %s\n" + "s3_secret_key = %s\n" + "#region = lega\n" + "\n"
                + "[outgestion]\n" + "# Just for test\n"
                + "keyserver_endpoint = http://keys:8080/keys/retrieve/%%s/private/bin?idFormat=hex\n"
                + "\n" + "## Connecting to Local EGA\n" + "[broker]\n" + "host = mq\n"
                + "username = admin\n" + "connection_attempts = 30\n" + "# delay in seconds\n"
                + "retry_delay = 10\n" + "\n" + "[postgres]\n" + "host = db\n" + "port = 5432\n"
                + "user = lega_in\n" + "password = %s\n" + "database = lega\n" + "try = 30\n"
                + "sslmode = require\n" + "\n" + "[eureka]\n"
                + "endpoint = http://cega-eureka:8761", inboxS3AccessKey, inboxS3SecretKey,
            vaultS3AccessKey, vaultS3SecretKey, postgresPassword), Charset.defaultCharset());
    }

}
