package se.nbis.lega.deployment.lega;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPublicTask;

public class CreateMQConfigurationTask extends LegaPublicTask {

    private static final String CEGA_MQ_PASSWORD2 = "CEGA_MQ_PASSWORD";

    @TaskAction
    public void run() throws IOException {
        createConfig(LegaPublicConfig.DEFS_JSON.getName(), getProject().file("mq/defs.json"));
        createConfig(LegaPublicConfig.RABBITMQ_CONFIG.getName(), getProject().file("mq/rabbitmq.config"));
        createConfig(LegaPublicConfig.ENTRYPOINT_SH.getName(), getProject().file("mq/entrypoint.sh"));
        String cegaMQPassword = readTrace(getProject().getParent().file(CEGA_TMP_TRACE), CEGA_MQ_PASSWORD2);
        if (cegaMQPassword != null) {
            String cegaMQHost = "cega-mq";
            String cegaMQPort = "5672";
            writeTrace(CEGA_CONNECTION,
                            String.format("amqp://lega:%s@%s:%s/lega", cegaMQPassword, cegaMQHost, cegaMQPort));
        }
    }

}
