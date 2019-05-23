package se.nbis.lega.deployment.lega.priv;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateMQConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(Config.MQ_CERT.getName(), getProject().getParent().file("common/.tmp/ssl/privateMQ.cert"));
        createConfig(Config.MQ_KEY.getName(), getProject().getParent().file("common/.tmp/ssl/privateMQ.key"));
    }

}
