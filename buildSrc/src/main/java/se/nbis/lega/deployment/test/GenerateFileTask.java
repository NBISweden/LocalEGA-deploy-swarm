package se.nbis.lega.deployment.test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.TestTask;

public class GenerateFileTask extends TestTask {


    @TaskAction
    public void run() throws IOException {
        long fileSize = 1024 * 1024 * 10;
        System.out.println("Generating " + fileSize + " bytes file to submit...");
        RandomAccessFile randomAccessFile = new RandomAccessFile(getRawFile(), "rw");
        randomAccessFile.setLength(fileSize);
        randomAccessFile.close();
        byte[] bytes = DigestUtils.sha256(FileUtils.openInputStream(getRawFile()));
        System.out.println("Checksum: " + Hex.encodeHexString(bytes));
    }

    @OutputFile
    public File getRawFile() {
        return getProject().file(".tmp/data.raw");
    }

}
