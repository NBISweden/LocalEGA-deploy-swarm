package se.nbis.lega.deployment.lega.pub;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

public class CreateMQConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(Config.DEFS_JSON.getName(), getProject().file("mq/defs.json"));
        createConfig(Config.RABBITMQ_CONFIG.getName(), getProject().file("mq/rabbitmq.config"));
        createConfig(Config.ENTRYPOINT_SH.getName(), getProject().file("mq/entrypoint.sh"));
        String cegaMQPassword = readTrace(getProject().getParent().file(CEGA_TMP_TRACE), CEGA_MQ_PASSWORD);
        String host = getProperty(CEGA_IP);
        if (cegaMQPassword != null) {
            if (host == null) {
                host = getMachineIPAddress(Machine.CEGA.getName());
            }
            writeTrace(CEGA_CONNECTION, String.format("amqp://lega:%s@%s:5672/lega", cegaMQPassword, host));
        } else if (getProperty(TEST_CEGA) != null) {
            String user = "norway1";
            String password = System.getenv(CEGA_MQ_PASSWORD);
            String port = "5271";
            String vhost = "norway1";
            host = "hellgate.crg.eu";
            writeTrace(CEGA_CONNECTION, String.format("amqps://%s:%s@%s:%s/%s", user, password, host, port, vhost));
        }
    }

}
