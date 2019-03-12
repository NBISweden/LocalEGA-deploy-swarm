package se.nbis.lega.deployment.test;

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
import se.nbis.lega.deployment.cluster.Machine;

@Slf4j
public class VerifyTask extends TestTask {

    public VerifyTask() {
        super();
    }

    @TaskAction
    public void run() throws IOException, SQLException {
        log.info("Starting Verification...");
        String host = getProperty(LEGA_PUBLIC_IP);
        if (host == null) {
            host = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        String port = "5740";
        String db = "local_ega";
        String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
        Properties props = new Properties();
        props.setProperty("user", "lega_in");
        props.setProperty("password", readTrace(DB_LEGA_IN_PASSWORD));
        props.setProperty("ssl", "true");
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
