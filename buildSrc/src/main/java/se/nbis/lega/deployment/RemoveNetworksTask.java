package se.nbis.lega.deployment;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.Set;

public class RemoveNetworksTask extends LocalEGATask {

    private Set<String> networks;

    @TaskAction
    public void run() throws IOException {
        for (String volume : networks) {
            removeNetwork(volume);
        }
    }

    public void setNetworks(Set<String> networks) {
        this.networks = networks;
    }

}
