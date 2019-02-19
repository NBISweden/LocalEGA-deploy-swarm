package se.nbis.lega.deployment.cluster;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

public abstract class ClusterTask extends LocalEGATask {
    private static final Logger logger = LoggerFactory.getLogger(ClusterTask.class);

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
        logger.info("createMachineVirtualBox");
        exec(true, "docker-machine create", "--driver", "virtualbox", name);
        return getMachines(name).get(name);
    }

    protected Map<String, String> createMachineOpenStack(String name, String openStackConfig) throws IOException {
        return createMachineOpenStack(name, readFileAsMap(new File(openStackConfig)));
    }

    protected Map<String, String> createMachineOpenStack(String name, Map<String, String> openStackEnvironment)
                    throws IOException {
        logger.info("createMachineOpenStack");
        try {
            List<String> output =
                            exec(true, openStackEnvironment, "docker-machine create", "--driver", "openstack", name);
            for (Iterator<String> iterator = output.iterator(); iterator.hasNext();) {
                String string = (String) iterator.next();
                logger.info(string);
            }
            if (output.get(0).startsWith("Error checking TLS connection")) {
                regenrateCerts(name, openStackEnvironment);
            }
        } catch (Exception e) {
            logger.error("Error Creating openStack machine: " + e.getMessage());
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
