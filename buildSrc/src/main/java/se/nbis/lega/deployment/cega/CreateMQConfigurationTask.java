package se.nbis.lega.deployment.cega;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class CreateMQConfigurationTask extends CegaTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(Config.RABBITMQ_CONFIG.getName(), getProject().file("mq/rabbitmq.config"));
        String password = generateCEGAMQConfiguration();
        createConfig(Config.DEFS_JSON.getName(), getProject().file(".tmp/mq/defs.json"));
        writeTrace(CEGA_MQ_PASSWORD, password);
        writeTrace(CEGA_REST_PASSWORD, UUID.randomUUID().toString().replace("-", ""));
    }

    public String generateCEGAMQConfiguration() throws IOException {
        byte[] saltBytes = new byte[4];
        new SecureRandom().nextBytes(saltBytes);
        String password = UUID.randomUUID().toString().replace("-", "");
        byte[] passwordBytes = password.getBytes();
        byte[] concat = ByteBuffer.allocate(saltBytes.length + passwordBytes.length).put(saltBytes)
            .put(passwordBytes).array();
        byte[] hash = DigestUtils.sha256(concat);
        concat =
            ByteBuffer.allocate(saltBytes.length + hash.length).put(saltBytes).put(hash).array();
        String saltedHash = Base64.getEncoder().encodeToString(concat);
        File defsJSON = getProject().file(".tmp/mq/defs.json");
        FileUtils.write(defsJSON, String.format("{\"rabbit_version\":\"3.6.14\",\n"
            + " \"users\":[{\"name\":\"lega\",\"password_hash\":\"%s\",\"hashing_algorithm\":\"rabbit_password_hashing_sha256\",\"tags\":\"administrator\"}],\n"
            + " \"vhosts\":[{\"name\":\"lega\"}],\n"
            + " \"permissions\":[{\"user\":\"lega\", \"vhost\":\"lega\", \"configure\":\".*\", \"write\":\".*\", \"read\":\".*\"}],\n"
            + " \"parameters\":[],\n"
            + " \"global_parameters\":[{\"name\":\"cluster_name\", \"value\":\"rabbit@localhost\"}],\n"
            + " \"policies\":[],\n"
            + " \"queues\":[{\"name\":\"v1.inbox\",           \"vhost\":\"lega\", \"durable\":true, \"auto_delete\":false, \"arguments\":{}},\n"
            + "           {\"name\":\"v1.stableIDs\",       \"vhost\":\"lega\", \"durable\":true, \"auto_delete\":false, \"arguments\":{}},\n"
            + "\t   {\"name\":\"v1.files\",           \"vhost\":\"lega\", \"durable\":true, \"auto_delete\":false, \"arguments\":{}},\n"
            + "\t   {\"name\":\"v1.completed\",       \"vhost\":\"lega\", \"durable\":true, \"auto_delete\":false, \"arguments\":{}},\n"
            + "\t   {\"name\":\"v1.errors\",          \"vhost\":\"lega\", \"durable\":true, \"auto_delete\":false, \"arguments\":{}}],\n"
            + " \"exchanges\":[{\"name\":\"localega.v1\", \"vhost\":\"lega\", \"type\":\"topic\", \"durable\":true, \"auto_delete\":false, \"internal\":false, \"arguments\":{}}],\n"
            + " \"bindings\":[{\"source\":\"localega.v1\",\"vhost\":\"lega\",\"destination_type\":\"queue\",\"arguments\":{},\"destination\":\"v1.inbox\",\"routing_key\":\"files.inbox\"},\n"
            + "\t     {\"source\":\"localega.v1\",\"vhost\":\"lega\",\"destination_type\":\"queue\",\"arguments\":{},\"destination\":\"v1.stableIDs\",\"routing_key\":\"files.stableIDs\"},\n"
            + "\t     {\"source\":\"localega.v1\",\"vhost\":\"lega\",\"destination_type\":\"queue\",\"arguments\":{},\"destination\":\"v1.files\",\"routing_key\":\"files\"},\n"
            + "\t     {\"source\":\"localega.v1\",\"vhost\":\"lega\",\"destination_type\":\"queue\",\"arguments\":{},\"destination\":\"v1.completed\",\"routing_key\":\"files.completed\"},\n"
            + "\t     {\"source\":\"localega.v1\",\"vhost\":\"lega\",\"destination_type\":\"queue\",\"arguments\":{},\"destination\":\"v1.errors\",\"routing_key\":\"files.error\"}]\n"
            + "}", saltedHash), Charset.defaultCharset());
        return password;
    }

}
