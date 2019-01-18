package se.nbis.lega.deployment.cluster;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.Map;

public class CreateMachineTask extends ClusterTask {

    @TaskAction
    public void run() throws IOException {
        String openStackConfig = getProperty("openStackConfig");
        Map<String, String> env = createMachine(machineName, openStackConfig);
        String machineIPAddress = getMachineIPAddress(machineName);
        exec(true, env, "docker swarm init", "--advertise-addr", machineIPAddress);
    }

}
