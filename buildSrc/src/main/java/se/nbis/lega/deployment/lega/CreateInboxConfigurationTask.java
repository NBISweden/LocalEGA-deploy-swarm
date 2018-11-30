package se.nbis.lega.deployment.lega;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

import java.io.IOException;

public class CreateInboxConfigurationTask extends LocalEGATask {

    public CreateInboxConfigurationTask() {
        super();
        this.setGroup(Groups.LEGA_PUBLIC.name());
    }

    @TaskAction
    public void run() throws IOException {
        String cegaRESTPassword = readTrace(getProject().file("../cega/.tmp/.trace"), "CEGA_REST_PASSWORD");
        if (cegaRESTPassword != null) {
            writeTrace("CEGA_ENDPOINT", "http://cega-users/lega/v1/legas/users/%s?idType=username");
            writeTrace("CEGA_ENDPOINT_CREDS", "lega:" + cegaRESTPassword);
        }
    }

}
