package se.nbis.lega.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyPair;
import java.security.Security;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.gradle.api.DefaultTask;

public abstract class LocalEGATask extends DefaultTask {

    public static final String TMP_TRACE = ".tmp/.trace";
    public static final String CEGA_TMP_TRACE = "cega/.tmp/.trace";
    public static final String LEGA_PRIVATE_TMP_TRACE = "lega-private/.tmp/.trace";
    public static final String S3_SECRET_KEY = "S3_SECRET_KEY";
    public static final String S3_ACCESS_KEY = "S3_ACCESS_KEY";
    public static final String CEGA_MQ_PASSWORD = "CEGA_MQ_PASSWORD";
    public static final String CEGA_CONNECTION = "CEGA_CONNECTION";
    public static final String LEGA = "lega";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public Map<String, String> getTraceAsMap() throws IOException {
        File traceFile = getProject().file(TMP_TRACE);
        return readFileAsMap(traceFile);
    }

    public String readTrace(String key) throws IOException {
        File traceFile = getProject().file(TMP_TRACE);
        return readTrace(traceFile, key);
    }

    protected String getHost() {
        String host = System.getenv("DOCKER_HOST");
        return host == null ? "localhost" : host.substring(6).split(":")[0];
    }

    protected String getProperty(String key) {
        return (String) getProject().getProperties().getOrDefault(key, null);
    }

    protected void writeTrace(String key, String value) throws IOException {
        File traceFile = getProject().file(TMP_TRACE);
        writeTrace(traceFile, key, value);
    }

    public void writeTrace(File traceFile, String key, String value) throws IOException {
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

    protected Map<String, String> readFileAsMap(File file) throws IOException {
        if (!file.exists()) {
            return Collections.emptyMap();
        }
        List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
        Map<String, String> result = new HashMap<>();

        for (String line : lines) {
            String[] split = line.split("=");
            String key = split[0].trim();
            String[] value = Arrays.copyOfRange(split, 1, split.length);
            result.put(key, String.join("=", value));
        }
        return result;
    }

    protected void removeConfig(String name) throws IOException {
        exec(true, "docker config rm", name);
    }

    protected void removeVolume(String name) throws IOException {
        exec(true, "docker volume rm", name);
    }

    protected void removeNetwork(String name) throws IOException {
        exec(true, "docker network rm", name);
    }

    protected void createConfig(String name, File file) throws IOException {
        exec("docker config create", name, file.getAbsolutePath());
    }

    protected List<String> exec(String command, String... arguments) throws IOException {
        return exec(false, null, command, arguments);
    }

    protected List<String> exec(boolean ignoreExitCode, String command, String... arguments) throws IOException {
        return exec(ignoreExitCode, null, command, arguments);
    }

    protected List<String> exec(Map<String, String> environment, String command, String... arguments)
                    throws IOException {
        return exec(false, environment, command, arguments);
    }

    protected List<String> exec(boolean ignoreExitCode, Map<String, String> environment, String command,
                    String... arguments) throws IOException {
        Map<String, String> systemEnvironment = new HashMap<>(System.getenv());
        if (environment != null) {
            systemEnvironment.putAll(environment);
        }
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        CommandLine commandLine = CommandLine.parse(command);
        commandLine.addArguments(arguments);
        try {
            executor.execute(commandLine, systemEnvironment);
            String output = outputStream.toString();
            System.out.println(output);
            return Arrays.asList(output.split(System.lineSeparator()));
        } catch (ExecuteException e) {
            String output = outputStream.toString();
            System.out.println(output);
            if (ignoreExitCode) {
                return Arrays.asList(output.split(System.lineSeparator()));
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
