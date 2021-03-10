package com.ibnux.nuxwallet.kripto;


import com.ibnux.nuxwallet.utils.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class Crypto {

    private Crypto() {}

    public static MessageDigest getMessageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static MessageDigest sha256() {
        return getMessageDigest("SHA-256");
    }


    public static byte[] getKeySeed(String secretPhrase, byte[]... nonces) {
        MessageDigest digest = Crypto.sha256();
        digest.update(Utils.toBytes(secretPhrase));
        for (byte[] nonce : nonces) {
            digest.update(nonce);
        }
        return digest.digest();
    }

    public static byte[] getPublicKey(byte[] keySeed) {
        byte[] publicKey = new byte[32];
        Curve25519.keygen(publicKey, null, Arrays.copyOf(keySeed, keySeed.length));
        return publicKey;
    }

    public static byte[] getPublicKey(String secretPhrase) {
        byte[] publicKey = new byte[32];
        Curve25519.keygen(publicKey, null, Crypto.sha256().digest(Utils.toBytes(secretPhrase)));
        return publicKey;
    }

    public static byte[] getPrivateKey(byte[] keySeed) {
        byte[] s = Arrays.copyOf(keySeed, keySeed.length);
        Curve25519.clamp(s);
        return s;
    }

    public static byte[] getPrivateKey(String secretPhrase) {
        byte[] s = Crypto.sha256().digest(Utils.toBytes(secretPhrase));
        Curve25519.clamp(s);
        return s;
    }

    public static void curve(byte[] Z, byte[] k, byte[] P) {
        Curve25519.curve(Z, k, P);
    }

    public static byte[] sign(byte[] message, String secretPhrase) {
        byte[] P = new byte[32];
        byte[] s = new byte[32];
        MessageDigest digest = Crypto.sha256();
        Curve25519.keygen(P, s, digest.digest(Utils.toBytes(secretPhrase)));

        byte[] m = digest.digest(message);

        digest.update(m);
        byte[] x = digest.digest(s);

        byte[] Y = new byte[32];
        Curve25519.keygen(Y, null, x);

        digest.update(m);
        byte[] h = digest.digest(Y);

        byte[] v = new byte[32];
        Curve25519.sign(v, h, x, s);

        byte[] signature = new byte[64];
        System.arraycopy(v, 0, signature, 0, 32);
        System.arraycopy(h, 0, signature, 32, 32);
        return signature;
    }

    public static boolean verify(byte[] signature, byte[] message, byte[] publicKey) {
        try {
            if (signature.length != 64) {
                return false;
            }
            if (!Curve25519.isCanonicalSignature(signature)) {
                return false;
            }

            if (!Curve25519.isCanonicalPublicKey(publicKey)) {
                return false;
            }

            byte[] Y = new byte[32];
            byte[] v = new byte[32];
            System.arraycopy(signature, 0, v, 0, 32);
            byte[] h = new byte[32];
            System.arraycopy(signature, 32, h, 0, 32);
            Curve25519.verify(Y, v, h, publicKey);

            MessageDigest digest = Crypto.sha256();
            byte[] m = digest.digest(message);
            digest.update(m);
            byte[] h2 = digest.digest(Y);

            return Arrays.equals(h, h2);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static byte[] getSharedKey(byte[] myPrivateKey, byte[] theirPublicKey) {
        return sha256().digest(getSharedSecret(myPrivateKey, theirPublicKey));
    }

    public static byte[] getSharedKey(byte[] myPrivateKey, byte[] theirPublicKey, byte[] nonce) {
        byte[] dhSharedSecret = getSharedSecret(myPrivateKey, theirPublicKey);
        for (int i = 0; i < 32; i++) {
            dhSharedSecret[i] ^= nonce[i];
        }
        return sha256().digest(dhSharedSecret);
    }

    private static byte[] getSharedSecret(byte[] myPrivateKey, byte[] theirPublicKey) {
        try {
            byte[] sharedSecret = new byte[32];
            Curve25519.curve(sharedSecret, myPrivateKey, theirPublicKey);
            return sharedSecret;
        } catch (RuntimeException e) {
            throw e;
        }
    }


    public static boolean isCanonicalPublicKey(byte[] publicKey) {
        return Curve25519.isCanonicalPublicKey(publicKey);
    }

    public static boolean isCanonicalSignature(byte[] signature) {
        return Curve25519.isCanonicalSignature(signature);
    }

}
