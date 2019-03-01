package se.nbis.lega.deployment.lega.pub;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

public class CreateInboxConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String cegaRESTPassword = readTrace(getProject().getParent().file(CEGA_TMP_TRACE), CEGA_REST_PASSWORD);
        String cegaHost = getProperty(TEST_CEGA);
        String user = "lega";
        if (cegaHost != null) {
            cegaRESTPassword = System.getenv(CEGA_REST_PASSWORD);
            user = "norway1";
        } else if (cegaRESTPassword != null) {
            cegaHost = getProperty(CEGA_IP);
            if (cegaHost == null) {
                cegaHost = getMachineIPAddress(Machine.CEGA.getName());
            }
        }
        writeTrace(CEGA_ENDPOINT, String.format("http://%s/lega/v1/legas/users/%%s?idType=username", cegaHost));
        writeTrace(CEGA_ENDPOINT_CREDS, user + ":" + cegaRESTPassword);
        String inboxAccessKey = readTrace(getProject().getParent().file(LEGA_PRIVATE_TMP_TRACE), INBOX_S3_ACCESS_KEY);
        String inboxSecretKey = readTrace(getProject().getParent().file(LEGA_PRIVATE_TMP_TRACE), INBOX_S3_SECRET_KEY);
        writeTrace(INBOX_S3_ACCESS_KEY, inboxAccessKey);
        writeTrace(INBOX_S3_SECRET_KEY, inboxSecretKey);
        String legaPrivateHost = getProperty("legaPrivateIP");
        if (legaPrivateHost == null) {
            legaPrivateHost = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        writeTrace(S3_ENDPOINT, legaPrivateHost + ":9001");
    }

}
