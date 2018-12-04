package se.nbis.lega.deployment.lega;

import java.io.IOException;
import java.util.UUID;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.LegaPrivateTask;

public class CreateMinioConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        writeTrace(S3_ACCESS_KEY, UUID.randomUUID().toString().replace("-", ""));
        writeTrace(S3_SECRET_KEY, UUID.randomUUID().toString().replace("-", ""));
        
        writeTrace("MINIO_ACCESS_KEY", readTrace( S3_ACCESS_KEY));
        writeTrace("MINIO_SECRET_KEY", readTrace( S3_SECRET_KEY));
    }

}
