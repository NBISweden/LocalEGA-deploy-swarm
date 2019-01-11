package se.nbis.lega.deployment.lega.priv;

import java.io.IOException;
import java.util.UUID;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.lega.priv.LegaPrivateTask;
import se.nbis.lega.deployment.lega.priv.Config;

public class CreateDBConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(Config.POSTGRESQL_CONF.getName(), getProject().file("db/postgresql.conf"));
        createConfig(Config.MAIN_SQL.getName(), getProject().file("db/main.sql"));
        createConfig(Config.GRANTS_SQL.getName(), getProject().file("db/grants.sql"));
        createConfig(Config.DOWNLOAD_SQL.getName(), getProject().file("db/download.sql"));
        createConfig(Config.EBI_SQL.getName(), getProject().file("db/ebi.sql"));
        createConfig(Config.QC_SQL.getName(), getProject().file("db/qc.sql"));
        createConfig(Config.DB_ENTRYPOINT_SH.getName(), getProject().file("db/entrypoint.sh"));
        writeTrace("DB_LEGA_IN_PASSWORD", UUID.randomUUID().toString().replace("-", ""));
        writeTrace("DB_LEGA_OUT_PASSWORD", UUID.randomUUID().toString().replace("-", ""));
    }

}
