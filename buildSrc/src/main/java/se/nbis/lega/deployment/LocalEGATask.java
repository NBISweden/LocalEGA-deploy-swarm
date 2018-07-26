package se.nbis.lega.deployment;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.gradle.api.DefaultTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyPair;
import java.security.Security;
import java.util.*;

public class LocalEGATask extends DefaultTask {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private DefaultExecutor executor = new DefaultExecutor();

    protected void writeTrace(String key, String value) throws IOException {
        File traceFile = getProject().file(".tmp/.trace");
        String existingValue = readTrace(traceFile, key);
        if (existingValue == null) {
            FileUtils.writeLines(traceFile, Collections.singleton(String.format("%s=%s", key, value)), true);
        }
    }

    protected String readTrace(File traceFile, String key) throws IOException {
        try {
            List<String> lines = FileUtils.readLines(traceFile, Charset.defaultCharset());
            for (String line : lines) {
                if (line.startsWith(key)) {
                    return line.split("=")[1].trim();
                }
            }
            return null;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    protected String readTrace(String key) throws IOException {
        File traceFile = getProject().file(".tmp/.trace");
        return readTrace(traceFile, key);
    }

    protected Map<String, String> getTraceAsMAp() throws IOException {
        File traceFile = getProject().file(".tmp/.trace");
        if (!traceFile.exists()) {
            return Collections.emptyMap();
        }
        List<String> lines = FileUtils.readLines(traceFile, Charset.defaultCharset());
        Map<String, String> result = new HashMap<>();
        for (String line : lines) {
            result.put(line.split("=")[0].trim(), line.split("=")[1].trim());
        }
        return result;
    }

    protected void removeConfig(String name) throws IOException {
        exec(true, "docker config rm", name);
    }

    protected void removeVolume(String name) throws IOException {
        exec(true, "docker volume rm", name);
    }

    protected void createConfig(String name, File file) throws IOException {
        exec("docker config create", name, file.getAbsolutePath());
    }

    protected int exec(boolean ignoreExitCode, String command, String... arguments) throws IOException {
        return exec(ignoreExitCode, null, command, arguments);
    }

    protected int exec(String command, String... arguments) throws IOException {
        return exec(false, null, command, arguments);
    }

    protected int exec(Map<String, String> environment, String command, String... arguments) throws IOException {
        return exec(false, environment, command, arguments);
    }

    protected int exec(boolean ignoreExitCode, Map<String, String> environment, String command, String... arguments) throws IOException {
        CommandLine commandLine = CommandLine.parse(command);
        commandLine.addArguments(arguments);
        try {
            return executor.execute(commandLine, environment);
        } catch (ExecuteException e) {
            if (ignoreExitCode) {
                return e.getExitValue();
            } else {
                throw e;
            }
        }
    }

    protected void writePublicKey(KeyPair keyPair, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        JcaPEMWriter pemWriter = new JcaPEMWriter(fileWriter);
        pemWriter.writeObject(keyPair.getPublic());
        pemWriter.close();
    }

    protected void writePrivateKey(KeyPair keyPair, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        JcaPEMWriter pemWriter = new JcaPEMWriter(fileWriter);
        pemWriter.writeObject(keyPair.getPrivate());
        pemWriter.close();
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(file.toPath(), perms);
    }

}