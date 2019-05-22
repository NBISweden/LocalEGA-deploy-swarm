package se.nbis.lega.deployment.lega.priv;

public enum Config {

    CA_CERT("CA.cert"),
    MQ_CERT("mq.cert"),
    MQ_KEY("mq.key"),
    DB_CERT("db.cert"),
    DB_KEY("db.key"),
    DB_ENTRYPOINT_SH("db.entrypoint.sh"),
    INBOX_S3_CERT("inboxS3.cert"),
    INBOX_S3_KEY("inboxS3.key"),
    VAULT_S3_CERT("vaultS3.cert"),
    VAULT_S3_KEY("vaultS3.key"),
    VERIFY_CERT("verify.cert"),
    VERIFY_KEY("verify.key"),
    FINALIZE_CERT("finalize.cert"),
    FINALIZE_KEY("finalize.key"),
    INGEST_CERT("ingest.cert"),
    INGEST_KEY("ingest.key"),
    KEYS_CERT("keys.cert"),
    KEYS_KEY("keys.key"),
    EGA_SEC("ega.sec"),
    EGA_SEC_PASS("ega.sec.pass"),
    EGA2_SEC("ega2.sec"),
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
