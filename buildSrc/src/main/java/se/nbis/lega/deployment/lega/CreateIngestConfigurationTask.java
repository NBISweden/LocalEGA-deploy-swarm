package se.nbis.lega.deployment.lega;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

import java.io.IOException;

public class CreateIngestConfigurationTask extends LocalEGATask {

    public CreateIngestConfigurationTask() {
        super();
        this.setGroup(Groups.LEGA_PUBLIC.name());
    }

    @TaskAction
    public void run() throws IOException {
       
    }

}
