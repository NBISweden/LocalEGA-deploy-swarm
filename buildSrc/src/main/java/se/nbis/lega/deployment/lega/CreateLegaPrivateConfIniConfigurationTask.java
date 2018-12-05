package se.nbis.lega.deployment.lega;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPrivateTask;

public class CreateLegaPrivateConfIniConfigurationTask extends LegaPrivateTask {

    public CreateLegaPrivateConfIniConfigurationTask() {
        super();
        this.dependsOn("clearConfiguration", "createDBConfiguration", "createKeysConfiguration",
                        "createRESConfiguration", "createMinioConfiguration");
    }

    @TaskAction
    public void run() throws IOException {
        generateConfIni();
        createConfig(LegaPrivateConfig.CONF_INI.getName(), getProject().file(".tmp/conf.ini"));
        createConfig(LegaPrivateConfig.DEFS_JSON.getName(), getProject().file("private_mq/defs.json"));
        createConfig(LegaPrivateConfig.RABBITMQ_CONFIG.getName(), getProject().file("private_mq/rabbitmq.config"));
    }

    private void generateConfIni() throws IOException {
        String s3AccessKey = readTrace(S3_ACCESS_KEY);
        String s3SecretKey = readTrace(S3_SECRET_KEY);
        String postgresPassword = readTrace("DB_LEGA_IN_PASSWORD");
        File confIni = getProject().file(".tmp/conf.ini");
        FileUtils.write(confIni, String.format("[DEFAULT]\n" + "log = console\n" + System.lineSeparator()
                        + "[keyserver]\n" + "port = 8080\n" + System.lineSeparator() + "[quality_control]\n"
                        + "keyserver_endpoint = http://keys:8080/keys/retrieve/%%s/private/bin?idFormat=hex\n"
                        + System.lineSeparator() + "[inbox]\n" + "location = /ega/inbox/%%s\n" + "mode = 2750\n"
                        + System.lineSeparator() + "[vault]\n" + "driver = S3Storage\n" + "url = http://s3:9000\n"
                        + "access_key = %s\n" + "secret_key = %s\n" + "#region = lega\n" + System.lineSeparator()
                        + System.lineSeparator() + "[outgestion]\n" + "# Just for test\n"
                        + "keyserver_endpoint = http://keys:8080/keys/retrieve/%%s/private/bin?idFormat=hex\n"
                        + System.lineSeparator() + "## Connecting to Local EGA\n" + "[broker]\n" + "host = mq\n"
                        + "connection_attempts = 30\n" + "# delay in seconds\n" + "retry_delay = 10\n"
                        + System.lineSeparator() + "[postgres]\n" + "host = db\n" + "port = 5432\n" + "user = lega_in\n"
                        + "password = %s\n" + "database = lega\n" + "try = 30\n" + "sslmode = require\n"
                        + System.lineSeparator() + "[eureka]\n" + "endpoint = http://cega-eureka:8761", s3AccessKey,
                        s3SecretKey, postgresPassword), Charset.defaultCharset());
    }

}
