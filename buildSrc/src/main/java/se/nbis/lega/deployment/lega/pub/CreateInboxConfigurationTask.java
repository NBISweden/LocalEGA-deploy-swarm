package se.nbis.lega.deployment.lega.pub;

import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class CreateInboxConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String cegaRESTPassword =
            readTrace(getProject().getParent().file(CEGA_TMP_TRACE), "CEGA_REST_PASSWORD");
        if (cegaRESTPassword != null) {
            writeTrace("CEGA_ENDPOINT",
                String.format("http://%s/lega/v1/legas/users/%%s?idType=username", getHost()));
            writeTrace("CEGA_ENDPOINT_CREDS", "lega:" + cegaRESTPassword);
        }
    }

}
