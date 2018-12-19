package se.nbis.lega.deployment.lega;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPrivateTask;

public class CreateIngestConfigurationTask extends LegaPrivateTask {

    public CreateIngestConfigurationTask() {
        super();
        this.dependsOn("clearConfiguration", "createDBConfiguration", "createKeysConfiguration",
                        "createRESConfiguration", "createMinioConfiguration");
    }

    @TaskAction
    public void run() throws IOException {
        writeTrace(S3_ACCESS_KEY, readTrace(S3_ACCESS_KEY));
        writeTrace(S3_SECRET_KEY, readTrace(S3_SECRET_KEY));
    }

}
