package se.nbis.lega.deployment.cluster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.gradle.api.tasks.TaskAction;

public class CreateMachineTask extends ClusterTask {

    public static final String SSH_KEY_FILE = "sshKeyFile";
    public static final String SSH_USER = "sshUser";

    @TaskAction
    public void run() throws IOException {
        Map<String, String> env;
        String openStackConfig = getProperty("openStackConfig");
        Map<String, String> openStackEnvironment = getOpenStackEnvironment();
        setMachineIp(getMachineIPAddress(machineName));
        if (openStackConfig != null) {
            env = createMachineOpenStack(machineName, openStackConfig);
        } else if (openStackEnvironment != null) {
            env = createMachineOpenStack(machineName, openStackEnvironment);
        } else if (machineIp != null) {
            env = createMachineWithIp(machineName, machineIp, getProperty(SSH_USER), getProperty(SSH_KEY_FILE));
        } else {
            env = createMachineVirtualBox(machineName);
        }
        exec(true, env, "docker swarm init", "--advertise-addr", machineIp);
    }

    private Map<String, String> getOpenStackEnvironment() {
        Map<String, String> openStackEnvironment = new HashMap<>();
        openStackEnvironment.put("OS_USERNAME", System.getenv("OS_USERNAME"));
        openStackEnvironment.put("OS_PASSWORD", System.getenv("OS_PASSWORD"));
        openStackEnvironment.put("OS_TENANT_ID", System.getenv("OS_TENANT_ID"));
        openStackEnvironment.put("OS_DOMAIN_NAME", System.getenv("OS_DOMAIN_NAME"));
        openStackEnvironment.put("OS_AUTH_URL", System.getenv("OS_AUTH_URL"));
        openStackEnvironment.put("OS_IDENTITY_API_VERSION", System.getenv("OS_IDENTITY_API_VERSION"));
        openStackEnvironment.put("OS_REGION_NAME", System.getenv("OS_REGION_NAME"));
        openStackEnvironment.put("OS_NETWORK_NAME", System.getenv("OS_NETWORK_NAME"));
        openStackEnvironment.put("OS_SECURITY_GROUPS", System.getenv("OS_SECURITY_GROUPS"));
        openStackEnvironment.put("OS_SSH_USER", System.getenv("OS_SSH_USER"));
        openStackEnvironment.put("OS_FLAVOR_NAME", System.getenv("OS_FLAVOR_NAME"));
        openStackEnvironment.put("OS_IMAGE_ID", System.getenv("OS_IMAGE_ID"));
        if (openStackEnvironment.values().contains(null)) {
            return null;
        } else {
            return openStackEnvironment;
        }
    }

}
