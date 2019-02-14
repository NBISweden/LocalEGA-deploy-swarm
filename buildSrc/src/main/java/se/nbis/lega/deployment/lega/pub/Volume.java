package se.nbis.lega.deployment.lega.pub;

public enum Volume {

    INBOX("lega-public_inbox"),
    PORTAINER("lega-public_portainer");

    private String name;

    Volume(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
