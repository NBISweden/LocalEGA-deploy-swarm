package se.nbis.lega.deployment.lega.pub;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateMediatorConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String host = getProperty("legaIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        writeTrace(MEDIATOR_SERVER, host + ":8080");
    }

}
