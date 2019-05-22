package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.UUID;

public class CreateDBConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        writeTrace(DB_LEGA_IN_PASSWORD, UUID.randomUUID().toString().replace("-", ""));
        writeTrace(DB_LEGA_OUT_PASSWORD, UUID.randomUUID().toString().replace("-", ""));

        createConfig(Config.DB_CERT.getName(), getProject().getParent().file("common/.tmp/ssl/db.cert"));
        createConfig(Config.DB_KEY.getName(), getProject().getParent().file("common/.tmp/ssl/db.key"));
        createConfig(Config.DB_ENTRYPOINT_SH.getName(), getProject().file("db/entrypoint.sh"));
    }

}
