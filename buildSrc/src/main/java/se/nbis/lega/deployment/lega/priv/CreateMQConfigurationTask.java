package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateMQConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(Config.DEFS_JSON.getName(), getProject().file("mq/defs.json"));
        createConfig(Config.RABBITMQ_CONFIG.getName(), getProject().file("mq/rabbitmq.config"));
        createConfig(Config.ENTRYPOINT_SH.getName(), getProject().file("mq/entrypoint.sh"));
        String cegaMQPassword =
            readTrace(getProject().getParent().file(CEGA_TMP_TRACE), "CEGA_MQ_PASSWORD");
        if (cegaMQPassword != null) {
            String host = getProperty("cegaIP");
            if (host == null) {
                host = getMachineIPAddress(Machine.CEGA.getName());
            }
            writeTrace(CEGA_CONNECTION,
                String.format("amqp://lega:%s@%s:5672/lega", cegaMQPassword, host));
        }
    }

}
