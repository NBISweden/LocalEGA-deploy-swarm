package se.nbis.lega.deployment.lega.priv;

import java.io.File;
import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.lega.priv.LegaPrivateTask;

public class CreateIngestConfigurationTask extends LegaPrivateTask {

    public CreateIngestConfigurationTask() {
        super();
        this.dependsOn("clearConfiguration", "createDBConfiguration", "createKeysConfiguration",
                        "createRESConfiguration", "createMinioConfiguration");
    }

    @TaskAction
    public void run() throws IOException {
        File traceFile = getProject().file(LEGA_PRIVATE_TMP_TRACE);
        writeTrace(S3_ACCESS_KEY, readTrace(traceFile, S3_ACCESS_KEY));
        writeTrace(S3_SECRET_KEY, readTrace(traceFile, S3_SECRET_KEY));
    }

}
