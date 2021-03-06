package se.nbis.lega.deployment.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.openpgp.PGPException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import no.uio.ifi.crypt4gh.stream.Crypt4GHOutputStream;

public class EncryptFileTask extends TestTask {

    public EncryptFileTask() {
        super();
        this.dependsOn("file");
    }

    @TaskAction
    public void run() throws IOException, PGPException {
        System.out.println("Encrypting the file with Crypt4GH...");
        byte[] digest = DigestUtils.sha256(FileUtils.openInputStream(getRawFile()));
        String key = FileUtils.readFileToString(new File("lega-private/.tmp/pgp/ega.pub"), Charset.defaultCharset());
        FileOutputStream fileOutputStream = new FileOutputStream(getEncFile());
        Crypt4GHOutputStream crypt4GHOutputStream = new Crypt4GHOutputStream(fileOutputStream, key, digest);
        String sessionKey = Hex.encodeHexString(crypt4GHOutputStream.getSessionKeyBytes());
        String iv = Hex.encodeHexString(crypt4GHOutputStream.getIvBytes());
        writeTrace("sessionKey", sessionKey);
        writeTrace("iv", iv);
        FileUtils.copyFile(getRawFile(), crypt4GHOutputStream);
        crypt4GHOutputStream.close();
    }

    @InputFile
    public File getRawFile() {
        return getProject().file(".tmp/data.raw");
    }

    @OutputFile
    public File getEncFile() {
        return getProject().file(".tmp/data.raw.enc");
    }

}
