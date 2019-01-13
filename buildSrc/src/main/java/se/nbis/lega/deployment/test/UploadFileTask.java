package se.nbis.lega.deployment.test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.File;
import java.io.IOException;

public class UploadFileTask extends TestTask {

    public UploadFileTask() {
        super();
        this.dependsOn("encrypt");
    }

    @TaskAction
    public void run() throws IOException {
        String host = getProperty("legaIP");
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA.getName());
        }
        System.out.println("Connecting to " + host);
        SSHClient ssh;
        try {
            ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host, 2222);
            ssh.authPublickey("john",
                getProject().file("cega/.tmp/users/john.sec").getAbsolutePath());
        } catch (UserAuthException e) {
            ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host, 2222);
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
