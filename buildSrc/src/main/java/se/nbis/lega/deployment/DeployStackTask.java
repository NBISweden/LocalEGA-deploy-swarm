package se.nbis.lega.deployment;

import java.io.IOException;
import java.util.Formatter;
import java.util.Map;
import java.util.Set;
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
        Set<String> keySet = environment.keySet();
        for (String string : keySet) {
            logger.info(new Formatter().format("%s=%s", string, environment.get(string)).toString());
        }
        exec(environment, "docker stack deploy", "--compose-file", composeFile, machineName);
    }

    public void setComposeFile(String composeFile) {
        this.composeFile = composeFile;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

}
