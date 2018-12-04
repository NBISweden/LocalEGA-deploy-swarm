package se.nbis.lega.deployment.lega;

import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.Groups;
import se.nbis.lega.deployment.LocalEGATask;

import java.io.IOException;
import java.util.UUID;

public class CreateMinioConfigurationTask extends LocalEGATask {

    public CreateMinioConfigurationTask() {
        super();
        this.setGroup(Groups.LEGA_PRIVATE.name());
    }

    @TaskAction
    public void run() throws IOException {
        writeTrace("S3_ACCESS_KEY", UUID.randomUUID().toString().replace("-", ""));
        writeTrace("S3_SECRET_KEY", UUID.randomUUID().toString().replace("-", ""));
        
        writeTrace("MINIO_ACCESS_KEY", readTrace(getProject(), "S3_ACCESS_KEY"));
        writeTrace("MINIO_SECRET_KEY", readTrace(getProject(), "S3_SECRET_KEY"));
    }

}
