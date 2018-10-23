package se.nbis.lega.deployment.lega;

public enum Config {

    DEFS_JSON("lega.defs.json"),
    RABBITMQ_CONFIG("lega.rabbitmq.config"),
    ENTRYPOINT_SH("lega.entrypoint.sh"),
    DB_SQL("db.sql"),
    SSL_CERT("ssl.cert"),
    SSL_KEY("ssl.key"),
    EGA_SEC("ega.sec"),
    EGA2_SEC("ega2.sec"),
    EGA_SEC_PASS("ega.sec.pass"),
    EGA2_SEC_PASS("ega2.sec.pass"),
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
