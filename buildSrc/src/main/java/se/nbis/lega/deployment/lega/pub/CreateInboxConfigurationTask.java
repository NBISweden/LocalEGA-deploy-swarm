package se.nbis.lega.deployment.lega.pub;

import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

public class CreateInboxConfigurationTask extends LegaPublicTask {

    @TaskAction
    public void run() throws IOException {
        String cegaRESTPassword = readTrace(getProject().getParent().file(CEGA_TMP_TRACE), CEGA_REST_PASSWORD);
        if (cegaRESTPassword != null) {
            String host = getProperty(CEGA_IP);
            if (host == null) {
                host = getMachineIPAddress(Machine.CEGA.getName());
            }
            if (getProperty(TEST_CEGA) != null) {
                host = "egatest.crg.eu";
                cegaRESTPassword = "pqax576fbsfe";
            }
            writeTrace(CEGA_ENDPOINT, String.format("http://%s/lega/v1/legas/users/%%s?idType=username", host));
            writeTrace(CEGA_ENDPOINT_CREDS, "lega:" + cegaRESTPassword);
        }
        String inboxAccessKey = readTrace(getProject().getParent().file(LEGA_PRIVATE_TMP_TRACE), INBOX_S3_ACCESS_KEY);
        String inboxSecretKey = readTrace(getProject().getParent().file(LEGA_PRIVATE_TMP_TRACE), INBOX_S3_SECRET_KEY);
        writeTrace(INBOX_S3_ACCESS_KEY, inboxAccessKey);
        writeTrace(INBOX_S3_SECRET_KEY, inboxSecretKey);
        String host = getProperty("legaPrivateIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        writeTrace(S3_ENDPOINT, host + ":9001");
    }

}
