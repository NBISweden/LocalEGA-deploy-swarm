package se.nbis.lega.deployment.test;

import lombok.extern.slf4j.Slf4j;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static se.nbis.lega.deployment.cluster.Machine.LEGA_PRIVATE;

@Slf4j
public class VerifyTask extends TestTask {

    public VerifyTask() {
        super();
        dependsOn("ingest");
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
        props.setProperty("password",
            readTrace(getProject().file(LEGA_PRIVATE_TMP_TRACE), DB_LEGA_IN_PASSWORD));
        props.setProperty("ssl", "true");
        props.setProperty("sslmode", "verify-full");
        props.setProperty("application_name", "LocalEGA");
        props.setProperty("sslrootcert",
            getProject().file("common/.tmp/ssl/CA.cert").getAbsolutePath());
        props
            .setProperty("sslcert", getProject().file("common/.tmp/ssl/db.cert").getAbsolutePath());
        props.setProperty("sslkey", getProject().file("common/.tmp/ssl/db.der.key").getAbsolutePath());
        try {
            Connection conn = DriverManager.getConnection(url, props);
            String sql = "select status from local_ega.files where id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, 1);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.wasNull() || !resultSet.next()) {
                throw new GradleException("Verification failed");
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        System.out.println("Verification completed successfully");
    }

}
