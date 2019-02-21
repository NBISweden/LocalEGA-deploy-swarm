package se.nbis.lega.deployment.lega.pub;

import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import se.nbis.lega.deployment.lega.priv.Config;
import se.nbis.lega.deployment.lega.priv.LegaPrivateTask;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CreateNginxConfigurationTask extends LegaPrivateTask {

    @TaskAction
    public void run() throws IOException {
        generateNginxConf();
        createConfig(Config.NGINX_CONF.getName(), getProject().file(".tmp/nginx.conf"));
    }

    private void generateNginxConf() throws IOException {
        String cegaIP = getProperty("cegaIP");
        File conf = getProject().file(".tmp/nginx.conf");
        FileUtils.write(conf, String.format(
            "stream {\n" + "\tupstream central_ega {\n" + "\t    server %s:5672;\n" + "\t}\n"
                + "    server {\n" + "        listen            127.0.0.1:5672;\n"
                + "        proxy_pass        central_ega;\n" + "    }\n" + "}", cegaIP),
            Charset.defaultCharset());
    }

}
