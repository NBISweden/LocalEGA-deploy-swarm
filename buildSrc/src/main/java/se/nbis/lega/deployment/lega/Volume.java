package se.nbis.lega.deployment.lega;

public enum Volume {

    LEGA_INBOX("lega-public_inbox"),
    LEGA_INBOX_S3("lega-private_inbox_s3"),
    LEGA_VAULT_S3("lega-private_vault_s3"),
    LEGA_DB("lega-private_db");

    private String name;

    Volume(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
