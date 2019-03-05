package se.nbis.lega.deployment.test;

import java.io.File;
import java.io.IOException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import se.nbis.lega.deployment.cluster.Machine;

@Slf4j
public class UploadFileTask extends TestTask {

    public UploadFileTask() {
        super();
        this.dependsOn("encrypt");
    }

    @TaskAction
    public void run() throws IOException {
        try {
            String host = getProperty("legaPublicIP");
            if (host == null) {
                host = getMachineIPAddress(Machine.LEGA_PUBLIC.getName());
            }
            log.info("Connecting to " + host);
            SSHClient ssh = new SSHClient();
            try {
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                ssh.connect(host, 2222);
                if (getProperty(TEST_CEGA) == null) {
                    ssh.authPublickey("john", getProject().file("cega/.tmp/users/john.sec").getAbsolutePath());
                } else {
                    ssh.authPassword("dummy", "dummy.sec");
                }

            } catch (UserAuthException e) {
                log.error("UserAuthException");
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                ssh.connect(host, 2222);
                ssh.authPublickey("dummy", "dummy");
            }
            log.info("Uploading a file...");
            SFTPClient client = ssh.newSFTPClient();
            client.put(getEncFile().getAbsolutePath(), "data.raw.enc");
            ssh.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @InputFile
    public File getEncFile() {
        return getProject().file(".tmp/data.raw.enc");
    }

}
