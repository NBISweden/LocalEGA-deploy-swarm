package se.nbis.lega.deployment;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class RemoveStackTask extends LocalEGATask {

    @TaskAction
    public void run() throws IOException {
        exec(getMachineEnv(machineName), "docker stack rm", machineName);
    }

}
