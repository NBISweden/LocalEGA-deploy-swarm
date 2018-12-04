package se.nbis.lega.deployment.lega;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPublicTask;

public class CreateIngestConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        writeTrace(S3_ACCESS_KEY, readTrace(getProject().getParent().file("lega-private/.tmp/.trace"), S3_ACCESS_KEY));
        writeTrace(S3_SECRET_KEY, readTrace(getProject().getParent().file("lega-private/.tmp/.trace"), S3_SECRET_KEY));
    }

}
