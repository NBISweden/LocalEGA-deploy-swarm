package se.nbis.lega.deployment.cluster;

import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ClusterTask extends LocalEGATask {

    public ClusterTask() {
        super();
        this.setGroup(Groups.CLUSTER.name());
    }

    protected Map<String, Map<String, String>> getMachines(String prefix) throws IOException {
        Map<String, Map<String, String>> result = new HashMap<>();
        List<String> ls = exec("docker-machine", "ls");
        for (String line : ls) {
            if (line.startsWith(prefix)) {
                String machineName = line.split(" ")[0];
                Map<String, String> variables = getMachineEnvironment(machineName);
                result.put(machineName, variables);
            }
        }
        return result;
    }

    protected Map<String, String> createMachineVirtualBox(String name) throws IOException {
        exec(true, "docker-machine create", "--driver", "virtualbox", name);
        return getMachines(name).get(name);
    }

    protected Map<String, String> createMachineOpenStack(String name, String openStackConfig)
        throws IOException {
        return createMachineOpenStack(name, readFileAsMap(new File(openStackConfig)));
    }

    protected Map<String, String> createMachineOpenStack(String name, Map<String, String> openStackEnvironment)
        throws IOException {
        exec(true, openStackEnvironment, "docker-machine create", "--driver", "openstack", name);
        return getMachines(name).get(name);
    }

    protected void removeMachine(String name) throws IOException {
        exec("docker-machine rm -y", name);
    }

}
