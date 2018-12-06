package se.nbis.lega.deployment.lega;

import java.io.File;
import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPublicTask;

public class CreateIngestConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        File traceFile = getProject().getParent().file(LEGA_PRIVATE_TMP_TRACE);
        writeTrace(S3_ACCESS_KEY, readTrace(traceFile, S3_ACCESS_KEY));
        writeTrace(S3_SECRET_KEY, readTrace(traceFile, S3_SECRET_KEY));
    }

}
