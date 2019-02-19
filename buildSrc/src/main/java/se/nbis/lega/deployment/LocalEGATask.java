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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LocalEGATask extends DefaultTask {
    private static final Logger logger = LoggerFactory.getLogger(LocalEGATask.class);

    public static final String MACHINE = "machine";
    public static final String TMP_TRACE = ".tmp/.trace";
    public static final String CEGA_TMP_TRACE = "cega/.tmp/.trace";
    public static final String LEGA_PRIVATE_TMP_TRACE = "lega-private/.tmp/.trace";

    public static final List<String> DOCKER_ENV_VARS =
                    Arrays.asList("DOCKER_TLS_VERIFY", "DOCKER_HOST", "DOCKER_CERT_PATH", "DOCKER_MACHINE_NAME");

    public static final String LEGA_INSTANCES = "LEGA_INSTANCES";
    public static final String LEGA_INSTANCE_NAME = "lega";
    public static final String INBOX_S3_ACCESS_KEY = "INBOX_S3_ACCESS_KEY";
    public static final String INBOX_S3_SECRET_KEY = "INBOX_S3_SECRET_KEY";
    public static final String S3_ENDPOINT = "S3_ENDPOINT";
    public static final String VAULT_S3_ACCESS_KEY = "VAULT_S3_ACCESS_KEY";
    public static final String VAULT_S3_SECRET_KEY = "VAULT_S3_SECRET_KEY";
    public static final String CEGA_MQ_PASSWORD = "CEGA_MQ_PASSWORD";
    public static final String CEGA_REST_PASSWORD = "CEGA_REST_PASSWORD";
    public static final String CEGA_CONNECTION = "CEGA_CONNECTION";
    public static final String MEDIATOR_SERVER = "MEDIATOR_SERVER";
    public static final String CEGA_ENDPOINT = "CEGA_ENDPOINT";
    public static final String CEGA_ENDPOINT_CREDS = "CEGA_ENDPOINT_CREDS";
    public static final String KEYS_PASSWORD = "KEYS_PASSWORD";
    public static final String LEGA_PASSWORD = "LEGA_PASSWORD";
    public static final String PGP_PASSPHRASE = "PGP_PASSPHRASE";
    public static final String DB_LEGA_IN_PASSWORD = "DB_LEGA_IN_PASSWORD";
    public static final String DB_LEGA_OUT_PASSWORD = "DB_LEGA_OUT_PASSWORD";
    public static final String EGA_USER_PASSWORD_JOHN = "EGA_USER_PASSWORD_JOHN";
    public static final String EGA_USER_PASSWORD_JANE = "EGA_USER_PASSWORD_JANE";
    public static final String VAULT_S3_BUCKET_NAME = "lega";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    protected String machineName;

    public void setMachineName(String machineName) {
        String machineNameProperty = getProperty(MACHINE);
        if (machineNameProperty != null) {
            this.machineName = machineNameProperty;
        } else {
            this.machineName = machineName;
        }
    }

    public Map<String, String> getTraceAsMap() throws IOException {
        File traceFile = getProject().file(TMP_TRACE);
        return readFileAsMap(traceFile);
    }

    public String readTrace(String key) throws IOException {
        File traceFile = getProject().file(TMP_TRACE);
        return readTrace(traceFile, key);
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
        exec(true, getMachineEnvironment(machineName), "docker config rm", name);
    }

    protected void removeVolume(String name) throws IOException {
        exec(true, getMachineEnvironment(machineName), "docker volume rm --force", name);
    }

    protected void createConfig(String name, File file) throws IOException {
        exec(true, getMachineEnvironment(machineName), "docker config create", name, file.getAbsolutePath());
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
            logger.info("Executing Command: " + commandLine.toString());
            executor.execute(commandLine, systemEnvironment);
            String output = outputStream.toString();
            logger.info("Execution result: " + output);
            return Arrays.asList(output.split(System.lineSeparator()));
        } catch (ExecuteException e) {
            String output = outputStream.toString();
            logger.error(output);
            if (ignoreExitCode) {
                return Arrays.asList(output.split(System.lineSeparator()));
            } else {
                throw e;
            }
        }
    }

    protected String getMachineIPAddress(String name) throws IOException {
        return exec("docker-machine ip", name).iterator().next();
    }

    protected Map<String, String> getMachineEnvironment(String name) throws IOException {
        List<String> env = exec("docker-machine env", name);
        Map<String, String> variables = new HashMap<>();
        for (String variable : env) {
            String[] split = variable.substring(7).split("=");
            if (DOCKER_ENV_VARS.contains(split[0])) {
                variables.put(split[0], split[1].replace("\"", ""));
            }
        }
        return variables;
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
