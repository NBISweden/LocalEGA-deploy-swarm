package se.nbis.lega.deployment;

import java.io.IOException;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

public class ClearConfigurationTask extends LocalEGATask {

    private Set<String> configs;

    @TaskAction
    public void run() throws IOException {
        removeConfigs(configs);
        FileUtils.deleteDirectory(getProject().file(".tmp/"));
    }

    public void setConfigs(Set<String> configs) {
        this.configs = configs;
    }

}
