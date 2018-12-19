package se.nbis.lega.deployment.lega;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPublicTask;

public class CreateInboxConfigurationTask extends LegaPublicTask {

    private static final String CEGA_REST_PASSWORD = "CEGA_REST_PASSWORD";
    private static final String CEGA_ENDPOINT = "CEGA_ENDPOINT";
    private static final String CEGA_ENDPOINT_CREDS = "CEGA_ENDPOINT_CREDS";

    @TaskAction
    public void run() throws IOException {
        String cegaRESTPassword = readTrace(getProject().getParent().file(CEGA_TMP_TRACE), CEGA_REST_PASSWORD);
        if (cegaRESTPassword != null) {
            String cegaUsersHost = "cega-users";
            writeTrace(CEGA_ENDPOINT,
                            String.format("http://%s/lega/v1/legas/users/%%s?idType=username", cegaUsersHost));
            writeTrace(CEGA_ENDPOINT_CREDS, "lega:" + cegaRESTPassword);
        }
    }

}
