package se.nbis.lega.deployment.lega.priv;

public enum Config {

    DEFS_JSON("lega-private.defs.json"),
    RABBITMQ_CONFIG("lega-private.rabbitmq.config"),
    ENTRYPOINT_SH("lega-private.entrypoint.sh"),
    POSTGRESQL_CONF("postgresql.conf"),
    MAIN_SQL("main.sql"),
    GRANTS_SQL("grants.sql"),
    DOWNLOAD_SQL("download.sql"),
    EBI_SQL("ebi.sql"),
    QC_SQL("qc.sql"),
    DB_ENTRYPOINT_SH("db.entrypoint.sh"),
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
