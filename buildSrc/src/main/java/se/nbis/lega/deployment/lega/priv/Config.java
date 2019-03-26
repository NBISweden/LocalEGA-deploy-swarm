package se.nbis.lega.deployment.lega.priv;

public enum Config {

    SSL_CERT("ssl.cert"),
    SSL_KEY("ssl.key"),
    EGA_SEC("ega.sec"),
    EGA2_SEC("ega2.sec"),
    EGA_SHARED_PASS("ega.shared.pass"),
    CONF_INI("conf.ini"),
    KEYS_INI_ENC("keys.ini.enc");

    private String name;

    Config(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
