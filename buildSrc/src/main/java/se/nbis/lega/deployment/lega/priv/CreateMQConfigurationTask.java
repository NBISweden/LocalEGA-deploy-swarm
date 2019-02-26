package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;
import java.util.UUID;

public class CreateMQConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(Config.RABBITMQ_CONFIG.getName(), getProject().file("mq/rabbitmq.config"));
        String host = getProperty("legaPublicIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PUBLIC.getName());
        }
        writeTrace(PUBLIC_MQ_IP, host);
        writeTrace(COOKIE, UUID.randomUUID().toString().replace("-", ""));
    }

}
