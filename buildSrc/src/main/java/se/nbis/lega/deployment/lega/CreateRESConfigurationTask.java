package se.nbis.lega.deployment.lega;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

public class CreateRESConfigurationTask extends LocalEGATask {

    public CreateRESConfigurationTask() {
        super();
        this.setGroup(Groups.LEGA.name());
    }

    @TaskAction
    public void run() throws Exception {
        writeTrace("EGA_EBI_AWS_ACCESS_KEY", readTrace("S3_ACCESS_KEY"));
        writeTrace("EGA_EBI_AWS_ACCESS_SECRET", readTrace("S3_SECRET_KEY"));
    }

}
