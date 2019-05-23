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
        writeTrace(PRIVATE_CONNECTION, "amqps://admin:guest@" + privateHost
            + ":5671/%2F?heartbeat=0\\&connection_attempts=30\\&retry_delay=10\\&server_name_indication=privateMQ\\&verify=verify_peer\\&fail_if_no_peer_cert=true\\&cacertfile=/etc/rabbitmq/CA.cert\\&certfile=/etc/rabbitmq/ssl.cert\\&keyfile=/etc/rabbitmq/ssl.key");

        try {
            createConfig(Config.CA_CERT.getName(),
                getProject().getParent().file("common/.tmp/ssl/CA.cert"));
        } catch (Exception ignore) {
            // ignore already existing CA
        }
        createConfig(Config.MQ_CERT.getName(),
            getProject().getParent().file("common/.tmp/ssl/publicMQ.cert"));
        createConfig(Config.MQ_KEY.getName(),
            getProject().getParent().file("common/.tmp/ssl/publicMQ.key"));
    }

}
