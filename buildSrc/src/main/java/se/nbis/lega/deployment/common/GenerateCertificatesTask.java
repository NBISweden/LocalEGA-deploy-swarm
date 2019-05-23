package se.nbis.lega.deployment.common;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.Pair;
import se.nbis.lega.deployment.cluster.Machine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

public class GenerateCertificatesTask extends CommonTask {

    @TaskAction
    public void run() throws Exception {
        getProject().file(".tmp/ssl/").mkdirs();
        String privateHost = getProperty("legaPrivateIP");
        if (privateHost == null) {
            privateHost = getMachineIPAddress(Machine.LEGA_PRIVATE.getName());
        }
        Pair<X509Certificate, KeyPair> root = generateCertificate(null, null, "CA", null);
        generateCertificate(root.left, root.right, "cegaMQ", null);
        generateCertificate(root.left, root.right, "cegaUsers", null);
        generateCertificate(root.left, root.right, "privateMQ", null);
        generateCertificate(root.left, root.right, "db", null);
        generateCertificate(root.left, root.right, "ingest", null);
        generateCertificate(root.left, root.right, "verify", null);
        generateCertificate(root.left, root.right, "finalize", null);
        generateCertificate(root.left, root.right, "keys", privateHost);
        generateCertificate(root.left, root.right, "inboxS3", privateHost);
        generateCertificate(root.left, root.right, "vaultS3", privateHost);
        generateCertificate(root.left, root.right, "publicMQ", null);
        Pair<X509Certificate, KeyPair> inbox =
            generateCertificate(root.left, root.right, "inbox", null);
        saveAsKeyStore(root, inbox);
    }

    private void saveAsKeyStore(Pair<X509Certificate, KeyPair> root,
        Pair<X509Certificate, KeyPair> client) throws Exception {
        X509Certificate rootCertificate = root.left;
        X509Certificate clientCertificate = client.left;
        KeyPair clientKeyPair = client.right;
        Certificate[] chain = new Certificate[] {clientCertificate, rootCertificate};
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry(rootCertificate.getSubjectX500Principal().getName(),
            rootCertificate);
        String keyStorePassword = UUID.randomUUID().toString().replace("-", "");
        keyStore.setKeyEntry(clientCertificate.getSubjectX500Principal().getName(),
            clientKeyPair.getPrivate(), keyStorePassword.toCharArray(), chain);
        File file = getProject().file(".tmp/ssl/inbox.jks");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            keyStore.store(fos, keyStorePassword.toCharArray());
            writeTrace(INBOX_JKS_PASSWORD, keyStorePassword);
        }
    }

    private Pair<X509Certificate, KeyPair> generateCertificate(X509Certificate rootCertificate,
        KeyPair parentKeypair, String service, String machineIPAddress)
        throws IOException, GeneralSecurityException, OperatorCreationException {
        KeyPair keyPair = KeyUtils.generateKeyPair("ssh-rsa", 2048);

        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.C, "NO")
            .addRDN(BCStyle.ST, "Norway").addRDN(BCStyle.L, "Oslo").addRDN(BCStyle.O, "UiO")
            .addRDN(BCStyle.OU, "IFI").addRDN(BCStyle.CN, service)
            .addRDN(BCStyle.EmailAddress, "nels-developers@googlegroups.com").build();

        X509v3CertificateBuilder builder = rootCertificate == null ?
            new JcaX509v3CertificateBuilder(subject, getSecureRandomSerial(),
                Date.from(LocalDate.of(2018, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                subject, keyPair.getPublic()) :
            new JcaX509v3CertificateBuilder(rootCertificate, getSecureRandomSerial(),
                Date.from(LocalDate.of(2018, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant()),
                subject, keyPair.getPublic());
        if (rootCertificate == null) {
            builder.addExtension(Extension.subjectKeyIdentifier, false,
                getSubjectKeyId(keyPair.getPublic()));
            builder.addExtension(Extension.authorityKeyIdentifier, false,
                getAuthorityKeyId(keyPair.getPublic()));
            builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(true));
            builder.addExtension(Extension.keyUsage, true, new KeyUsage(
                KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment
                    | KeyUsage.dataEncipherment | KeyUsage.cRLSign));
            ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(
                new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth,
                    KeyPurposeId.anyExtendedKeyUsage});
            builder.addExtension(Extension.extendedKeyUsage, true, extendedKeyUsage);
        } else {
            builder.addExtension(Extension.subjectKeyIdentifier, false,
                getSubjectKeyId(keyPair.getPublic()));
            builder.addExtension(Extension.authorityKeyIdentifier, false,
                getAuthorityKeyId(parentKeypair.getPublic()));
            builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
            builder.addExtension(Extension.keyUsage, true, new KeyUsage(
                KeyUsage.nonRepudiation | KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
            ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(
                new KeyPurposeId[] {KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth});
            builder.addExtension(Extension.extendedKeyUsage, true, extendedKeyUsage);
            if (machineIPAddress != null) {
                GeneralName generalName = new GeneralName(GeneralName.iPAddress, machineIPAddress);
                GeneralNames generalNames = GeneralNames.getInstance(new DERSequence(generalName));
                builder.addExtension(Extension.subjectAlternativeName, false, generalNames);
            }
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
            .build(parentKeypair == null ? keyPair.getPrivate() : parentKeypair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);

        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(new BouncyCastleProvider());
        X509Certificate certificate = converter.getCertificate(holder);

        writeCertificate(certificate, getProject().file(".tmp/ssl/" + service + ".cert"));

        writePrivateKeyPEM(keyPair, getProject().file(".tmp/ssl/" + service + ".key"));

        writePrivateKeyDER(keyPair, getProject().file(".tmp/ssl/" + service + ".der.key"));

        return Pair.of(certificate, keyPair);
    }

    private BigInteger getSecureRandomSerial() {
        SecureRandom random = new SecureRandom();
        byte[] id = new byte[20];
        random.nextBytes(id);
        return new BigInteger(160, random);
    }

    private AuthorityKeyIdentifier getAuthorityKeyId(PublicKey pub) {
        SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pub.getEncoded());
        return new BcX509ExtensionUtils().createAuthorityKeyIdentifier(info);
    }

    private SubjectKeyIdentifier getSubjectKeyId(PublicKey pub) {
        SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(pub.getEncoded());
        return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info);
    }

}
