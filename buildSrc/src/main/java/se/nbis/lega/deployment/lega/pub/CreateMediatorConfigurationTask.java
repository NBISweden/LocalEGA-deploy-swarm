package se.nbis.lega.deployment.lega.pub;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.lega.pub.LegaPublicTask;

import java.io.IOException;

public class CreateMediatorConfigurationTask extends LegaPublicTask {

    @TaskAction public void run() throws IOException {
        writeTrace("MEDIATOR_SERVER", getHost() + ":8080");
    }

}
