package se.nbis.lega.deployment.cluster;

import lombok.extern.slf4j.Slf4j;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        log.info("createMachineVirtualBox");
        exec(true, "docker-machine create", "--driver", "virtualbox", name);
        return getMachines(name).get(name);
    }

    protected Map<String, String> createMachineOpenStack(String name, String openStackConfig)
        throws IOException {
        return createMachineOpenStack(name, readFileAsMap(new File(openStackConfig)));
    }

    protected Map<String, String> createMachineOpenStack(String name,
        Map<String, String> openStackEnvironment) throws IOException {
        log.info("createMachineOpenStack");
        try {
            exec(true, openStackEnvironment, "docker-machine create", "--driver", "openstack",
                name);
            regenerateCertificates(name, openStackEnvironment);
        } catch (Exception e) {
            log.error("Error Creating openStack machine: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return getMachines(name).get(name);
    }

    protected void removeMachine(String name) throws IOException {
        List<String> machines = exec("docker-machine ls --filter name=", name);
        if (machines.size() > 1) {
            exec("docker-machine rm -y", name);
        }
    }

}
