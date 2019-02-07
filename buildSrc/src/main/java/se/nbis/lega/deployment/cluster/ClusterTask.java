package se.nbis.lega.deployment.cluster;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

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

    protected Map<String, String> createMachineOpenStack(String name, String openStackConfig) throws IOException {
        return createMachineOpenStack(name, readFileAsMap(new File(openStackConfig)));
    }

    protected Map<String, String> createMachineOpenStack(String name, Map<String, String> openStackEnvironment)
                    throws IOException {
        exec(true, openStackEnvironment, "docker-machine create", "--driver", "openstack", name);
        return getMachines(name).get(name);
    }

    /**
     * Create Docker Machine using generic driver
     * 
     * @param name
     * @param openStackEnvironment
     * @param ip
     * @param user
     * @param sshKeyFile
     * @return
     * @throws IOException
     */
    protected Map<String, String> createMachineWithIp(String name, String ip, String user, String sshKeyFile)
                    throws IOException {
        exec(true, "docker-machine create", "--driver", "generic", "--generic-ip-address", ip, "--generic-ssh-user",
                        user, "--generic-ssh-key", sshKeyFile, name);
        exec(true, "eval", "\"$(docker-machine env " + machineName + "\")");
        return getMachines(name).get(name);
    }

    protected void removeMachine(String name) throws IOException {
        exec("docker-machine rm -y", name);
    }

}
