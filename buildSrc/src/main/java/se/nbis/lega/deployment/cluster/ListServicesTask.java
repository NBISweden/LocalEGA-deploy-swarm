package se.nbis.lega.deployment.cluster;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;

public class ListServicesTask extends ClusterTask {

    @TaskAction
    public void run() throws IOException {
        setMachineName(machineName);
        listServices();
    }

}
