package se.nbis.lega.deployment.test;

import java.io.File;
import java.io.IOException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import se.nbis.lega.deployment.TestTask;

public class UploadFileTask extends TestTask {

    public UploadFileTask() {
        super();
        this.dependsOn("encrypt");
    }

    @TaskAction
    public void run() throws IOException {
        String host = getHost();
        System.out.println("Connecting to " + host);
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(host, 2222);
        try {
            ssh.authPublickey("john", getProject().file("cega/.tmp/users/john.sec").getAbsolutePath());
        } catch (UserAuthException e) {
            ssh.authPublickey("dummy", "dummy.sec");
        }
        System.out.println("Uploading a file...");
        SFTPClient client = ssh.newSFTPClient();
        client.put(getEncFile().getAbsolutePath(), "data.raw.enc");
        ssh.close();
    }

    @InputFile
    public File getEncFile() {
        return getProject().file(".tmp/data.raw.enc");
    }

}
