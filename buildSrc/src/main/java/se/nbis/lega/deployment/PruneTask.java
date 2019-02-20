package se.nbis.lega.deployment;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class PruneTask extends LocalEGATask {

    @TaskAction
    public void run() throws IOException {
        exec(getMachineEnvironment(machineName), "docker system prune", "--force", "--volumes");
    }

}
