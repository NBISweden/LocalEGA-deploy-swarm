package se.nbis.lega.deployment.lega;

import java.io.IOException;
import java.util.UUID;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPrivateTask;

public class CreateDBConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        createConfig(LegaPrivateConfig.POSTGRESQL_CONF.getName(), getProject().file("db/postgresql.conf"));
        createConfig(LegaPrivateConfig.MAIN_SQL.getName(), getProject().file("db/main.sql"));
        createConfig(LegaPrivateConfig.GRANTS_SQL.getName(), getProject().file("db/grants.sql"));
        createConfig(LegaPrivateConfig.DOWNLOAD_SQL.getName(), getProject().file("db/download.sql"));
        createConfig(LegaPrivateConfig.EBI_SQL.getName(), getProject().file("db/ebi.sql"));
        createConfig(LegaPrivateConfig.QC_SQL.getName(), getProject().file("db/qc.sql"));
        createConfig(LegaPrivateConfig.DB_ENTRYPOINT_SH.getName(), getProject().file("db/entrypoint.sh"));
        writeTrace("DB_LEGA_IN_PASSWORD", UUID.randomUUID().toString().replace("-", ""));
        writeTrace("DB_LEGA_OUT_PASSWORD", UUID.randomUUID().toString().replace("-", ""));
    }

}
