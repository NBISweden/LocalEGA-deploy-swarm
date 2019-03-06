package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateMQConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        String host = getProperty("legaPublicIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PUBLIC.getName());
        }
        writeTrace(PUBLIC_CONNECTION, String.format("amqp://guest:guest@%s:5672", host));
    }

}
