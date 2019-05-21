package se.nbis.lega.deployment.lega.pub;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateMQConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String cegaMQPassword =
            readTrace(getProject().getParent().file(CEGA_TMP_TRACE), CEGA_MQ_PASSWORD);
        if (cegaMQPassword != null) { // test pipeline
            String cegaHost = getProperty("cegaIP");
            if (cegaHost == null) {
                cegaHost = getMachineIPAddress(Machine.CEGA.getName());
            }
            writeTrace(CEGA_CONNECTION,
                String.format("amqp://lega:%s@%s:5672/lega", cegaMQPassword, cegaHost));
        } else { // staging pipeline
            writeTrace(CEGA_CONNECTION, System.getenv(CEGA_MQ_CONNECTION));
        }

        // any pipeline
        String privateHost = getProperty("legaPrivateIP");
        if (privateHost == null) {
            privateHost = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        writeTrace(PRIVATE_CONNECTION, String.format("amqp://admin:guest@%s:5672", privateHost));

        createConfig(Config.CA_CERT.getName(),
            getProject().getParent().file("common/.tmp/ssl/CA.cert"));
        createConfig(Config.MQ_CERT.getName(),
            getProject().getParent().file("common/.tmp/ssl/publicMQ.cert"));
        createConfig(Config.MQ_KEY.getName(),
            getProject().getParent().file("common/.tmp/ssl/publicMQ.key"));
    }

}
