package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.UUID;

public class CreateInboxS3ConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        writeTrace(INBOX_S3_ACCESS_KEY, UUID.randomUUID().toString().replace("-", ""));
        writeTrace(INBOX_S3_SECRET_KEY, UUID.randomUUID().toString().replace("-", ""));
    }

}
