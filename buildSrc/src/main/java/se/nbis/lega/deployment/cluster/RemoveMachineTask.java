package se.nbis.lega.deployment.cluster;

import org.gradle.api.tasks.TaskAction;
import java.io.IOException;

public class RemoveMachineTask extends ClusterTask {

    private String machineName;

    @TaskAction
    public void run() throws IOException {
        removeMachine(machineName);
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

}
