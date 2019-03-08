package se.nbis.lega.deployment.cluster;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;

public class ServiceLogsTask extends ClusterTask {

    @TaskAction
    public void run() throws IOException {
        setMachineName(machineName);
        serviceLogs(getProperty("service"));
    }

}
