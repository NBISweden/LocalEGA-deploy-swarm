package se.nbis.lega.deployment.lega;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPublicTask;

public class CreateLegaPublicConfIniConfigurationTask extends LegaPublicTask {

    public CreateLegaPublicConfIniConfigurationTask() {
        super();
        this.dependsOn("clearConfiguration",
                "createMQConfiguration",
                "createInboxConfiguration",
                "createIngestConfiguration"
                );
    }

    @TaskAction
    public void run() throws IOException {
    }


}
