package se.nbis.lega.deployment.cega;

import net.schmizz.sshj.common.Buffer;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class CreateUsersConfigurationTask extends CegaTask {

    @TaskAction
    public void run() throws IOException, GeneralSecurityException {
        createConfig(Config.SERVER_PY.getName(), getProject().file("server.py"));
        createConfig(Config.USERS_HTML.getName(), getProject().file("users.html"));
        String johnPassword = generateUser("john");
        createConfig(Config.JOHN_YML.getName(), getProject().file(".tmp/users/john.yml"));
        writeTrace(EGA_USER_PASSWORD_JOHN, johnPassword);
        String janePassword = generateUser("jane");
        createConfig(Config.JANE_YML.getName(), getProject().file(".tmp/users/jane.yml"));
        writeTrace(EGA_USER_PASSWORD_JANE, janePassword);
        writeTrace(LEGA_INSTANCES, LEGA_INSTANCE_NAME);
        String cegaRESTPassword = UUID.randomUUID().toString().replace("-", "");
        writeTrace(CEGA_REST_PASSWORD, cegaRESTPassword);
        writeTrace("CEGA_REST_" + LEGA_INSTANCE_NAME + "_PASSWORD", cegaRESTPassword);
    }

    private String generateUser(String username) throws GeneralSecurityException, IOException {
        KeyPair keyPair = KeyUtils.generateKeyPair("ssh-rsa", 4096);

        byte[] keyBytes =
            new Buffer.PlainBuffer().putPublicKey(keyPair.getPublic()).getCompactData();
        String sshKeyString = "ssh-rsa " + Base64.getEncoder().encodeToString(keyBytes);

        String password = UUID.randomUUID().toString().replace("-", "");
        String salt = UUID.randomUUID().toString().replace("-", "");
        String hash = Crypt.crypt(password, String.format("$1$%s$", salt));
        String uid = String.valueOf(Math.abs(new SecureRandom().nextInt()));

        File userYML = getProject().file(String.format(".tmp/users/%s.yml", username));
        FileUtils.writeLines(userYML, Arrays
            .asList("---", "password_hash: " + hash, "pubkey: " + sshKeyString, "uid: " + uid));
        writePublicKey(keyPair, getProject().file(String.format(".tmp/users/%s.pub", username)));
        writePrivateKey(keyPair, getProject().file(String.format(".tmp/users/%s.sec", username)));

        return password;
    }

}
