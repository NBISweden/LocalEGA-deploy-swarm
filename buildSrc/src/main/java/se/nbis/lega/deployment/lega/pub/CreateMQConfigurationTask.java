package se.nbis.lega.deployment.lega.pub;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateMQConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String cegaMQPassword =
            readTrace(getProject().getParent().file(CEGA_TMP_TRACE), "CEGA_MQ_PASSWORD");
        String host = getProperty("cegaIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.CEGA.getName());
        }
        writeTrace(CEGA_CONNECTION,
            String.format("amqp://lega:%s@%s:5672/lega", cegaMQPassword, host));
        host = getProperty("legaPrivateIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        writeTrace(PRIVATE_CONNECTION, String.format("amqp://guest:guest@%s:5672", host));
    }

}
