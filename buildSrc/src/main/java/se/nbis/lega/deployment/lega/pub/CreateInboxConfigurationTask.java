package se.nbis.lega.deployment.lega.pub;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.IOException;

public class CreateInboxConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String cegaRESTPassword =
            readTrace(getProject().getParent().file(CEGA_TMP_TRACE), "CEGA_REST_PASSWORD");
        if (cegaRESTPassword != null) {
            String host = getProperty("cegaIP");
            if (host == null) {
                host = getMachineIPAddress(Machine.CEGA.getName());
            }
            writeTrace("CEGA_ENDPOINT",
                String.format("http://%s/lega/v1/legas/users/%%s?idType=username", host));
            writeTrace("CEGA_ENDPOINT_CREDS", "lega:" + cegaRESTPassword);
        }
    }

}
