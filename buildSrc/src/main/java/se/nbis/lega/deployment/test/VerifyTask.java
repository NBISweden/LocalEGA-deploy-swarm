package se.nbis.lega.deployment.test;

import static se.nbis.lega.deployment.cluster.Machine.LEGA_PRIVATE;
import static se.nbis.lega.deployment.lega.priv.Config.SSL_CERT;
import static se.nbis.lega.deployment.lega.priv.Config.SSL_KEY;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyTask extends TestTask {

    private static final String LEGA_PRIVATE_SSL = LEGA_PRIVATE_TMP + "/ssl/";

    public VerifyTask() {
        super();
    }

    @TaskAction
    public void run() throws IOException, SQLException {
        log.info("Starting Verification...");
        String host = getProperty(LEGA_PRIVATE_IP);
        if (host == null) {
            host = getMachineIPAddress(LEGA_PRIVATE.getName());
        }
        String port = "5432";
        String db = "lega";
        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
        Properties props = new Properties();
        props.setProperty("user", "lega_in");
        props.setProperty("password", readTrace(getProject().file(LEGA_PRIVATE_TMP_TRACE), DB_LEGA_IN_PASSWORD));
        props.setProperty("ssl", "true");
        props.setProperty("sslrootcert", getProject().file(LEGA_PRIVATE_SSL + SSL_CERT.getName()).getAbsolutePath());
        props.setProperty("sslcert", getProject().file(LEGA_PRIVATE_SSL + SSL_CERT.getName()).getAbsolutePath());
        props.setProperty("sslkey", getProject().file(LEGA_PRIVATE_SSL + SSL_KEY.getName()).getAbsolutePath());
        try {
            Connection conn = DriverManager.getConnection(url, props);
            String sql = "select status from local_ega.files where id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "1");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.wasNull() || !resultSet.next()) {
                throw new GradleException("Verification failed");
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        log.info("Verification completed successfully");
    }
}
