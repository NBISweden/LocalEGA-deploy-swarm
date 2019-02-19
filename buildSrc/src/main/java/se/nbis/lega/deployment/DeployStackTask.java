package se.nbis.lega.deployment;

import java.io.IOException;
import java.util.Map;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployStackTask extends LocalEGATask {
    private static final Logger logger = LoggerFactory.getLogger(DeployStackTask.class);

    private String composeFile;
    private Map<String, String> environment;

    @TaskAction
    public void run() throws IOException {
        environment.putAll(getMachineEnvironment(machineName));
        exec(environment, "docker stack deploy", "--compose-file", composeFile, machineName);
    }

    public void setComposeFile(String composeFile) {
        this.composeFile = composeFile;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

}
