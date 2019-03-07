package se.nbis.lega.deployment;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class RemoveCEGATmpTask extends LocalEGATask {

    @TaskAction
    public void run() throws IOException {
        getProject().getParent().file(CEGA_TMP_TRACE).delete();
    }

}
