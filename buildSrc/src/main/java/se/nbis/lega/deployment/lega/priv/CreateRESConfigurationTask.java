package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.lega.priv.LegaPrivateTask;

public class CreateRESConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws Exception {
        writeTrace("EGA_EBI_AWS_ACCESS_KEY", readTrace(S3_ACCESS_KEY));
        writeTrace("EGA_EBI_AWS_ACCESS_SECRET", readTrace(S3_SECRET_KEY));
    }

}
