package se.nbis.lega.deployment.common;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.Pair;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

public class GenerateCertificatesTask extends CommonTask {

    @TaskAction public void run() throws Exception {
        getProject().file(".tmp/ssl/").mkdirs();
        Pair<X509Certificate, KeyPair> root = generateCertificate(null, null, "CA");
        generateCertificate(root.left, root.right, "vaultS3");
        generateCertificate(root.left, root.right, "inboxS3");
        generateCertificate(root.left, root.right, "cegaMQ");
        generateCertificate(root.left, root.right, "cegaUsers");
        generateCertificate(root.left, root.right, "db");
        generateCertificate(root.left, root.right, "finalize");
        generateCertificate(root.left, root.right, "inbox");
        generateCertificate(root.left, root.right, "ingest");
        generateCertificate(root.left, root.right, "keys");
        generateCertificate(root.left, root.right, "privateMQ");
        generateCertificate(root.left, root.right, "publicMQ");
        generateCertificate(root.left, root.right, "verify");
    }

    private Pair<X509Certificate, KeyPair> generateCertificate(X509Certificate rootCertificate,
        KeyPair parentKeypair, String service)
        throws IOException, GeneralSecurityException, OperatorCreationException {
        KeyPair keyPair = KeyUtils.generateKeyPair("ssh-rsa", 2048);

        X500Name subject = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.C, "NO")
            .addRDN(BCStyle.ST, "Norway").addRDN(BCStyle.L, "Oslo").addRDN(BCStyle.O, "UiO")
            .addRDN(BCStyle.OU, "IFI").addRDN(BCStyle.CN, service)
            .addRDN(BCStyle.EmailAddress, "nels-developers@googlegroups.com").build();

        X509v3CertificateBuilder builder = rootCertificate == null ?
            new JcaX509v3CertificateBuilder(
                subject,
                getSecureRandomSerial(),
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
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
            .build(parentKeypair == null ? keyPair.getPrivate() : parentKeypair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);

        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(new BouncyCastleProvider());
        X509Certificate certificate = converter.getCertificate(holder);

        writeCertificate(certificate, getProject().file(".tmp/ssl/" + service + ".cert"));

        writePrivateKey(keyPair, getProject().file(".tmp/ssl/" + service + ".key"));

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
