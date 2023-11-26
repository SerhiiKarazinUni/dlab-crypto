import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class OwnImpl implements DoesECDSA {
    private ECPoint G, Q;
    private BigInteger N;
    private KeyPair keys;

    private byte[] toUnsignedByteArray(BigInteger bi) {
        byte[] ba = bi.toByteArray();
        if (ba[0] != 0) {
            return ba;
        } else {
            byte[] ba2 = new byte[ba.length - 1];
            System.arraycopy(ba, 1, ba2, 0, ba.length - 1);
            return ba2;
        }
    }

    @Override
    public byte[] getSignature(byte[] plaintext) throws WTFException {
        BigInteger r = BigInteger.ZERO;
        BigInteger s = BigInteger.ZERO;
        BigInteger k = BigInteger.ZERO;

        do {
            do {
                byte[] kBytes = new byte[32];
                new MyRandomness().nextBytes(kBytes);
                k = new BigInteger(kBytes);

                // multiply k*G
                ECPoint kG = G.multiply(k).normalize();
                r = kG.getAffineXCoord().toBigInteger().mod(N);
            } while (r.equals(BigInteger.ZERO));

            // compute e
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance(CommonConsts.SHA256);
            } catch (NoSuchAlgorithmException e) {
                throw new WTFException("cannot instantiate SHA256 digest algorithm");
            }
            BigInteger e = new BigInteger(1, digest.digest(plaintext));

            // compute s
            BigInteger d = ((ECPrivateKey) getKeys().getPrivate()).getD();
            s = BigIntegers.modOddInverse(N, k).multiply(e.add(d.multiply(r))).mod(N); // ( k^{-1} * (e + r*d) ) mod n
        } while (s.equals(BigInteger.ZERO));

        // pack (r,s) values as 64-byte array
        byte[] rArr = toUnsignedByteArray(r);
        byte[] sArr = toUnsignedByteArray(s);
        byte[] res = new byte[64];
        System.arraycopy(rArr, 0, res, 0, 32);
        System.arraycopy(sArr, 0, res, 32, 32);

        return res;
    }

    @Override
    public boolean checkSignature(byte[] plaintext, byte[] signature) throws WTFException {

        // read signature
        byte[] rArr = new byte[33];
        rArr[0] = 0;
        byte[] sArr = new byte[33];
        sArr[0] = 0;
        System.arraycopy(signature, 0, rArr, 1, 32);
        System.arraycopy(signature, 32, sArr, 1, 32);
        BigInteger r = new BigInteger(rArr);
        BigInteger s = new BigInteger(sArr);

        // check r, s < n-1
        if (r.compareTo(N) >= 0 || s.compareTo(N) >= 0) {
            return false;
        }

        // compute e
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(CommonConsts.SHA256);
        } catch (NoSuchAlgorithmException e) {
            throw new WTFException("cannot instantiate SHA256 digest algorithm");
        }
        BigInteger e = new BigInteger(1, digest.digest(plaintext));

        // calculate X
        BigInteger w = s.modInverse(N);

        BigInteger u1 = e.multiply(w).mod(N);
        BigInteger u2 = r.multiply(w).mod(N);

        ECPoint X = (G.multiply(u1).normalize().add(Q.multiply(u2).normalize())).normalize(); // X = u1G + u2Q

        // check v == r
        if (!X.isValid() || X.getAffineXCoord().isZero()) {
            return false;
        }

        BigInteger v = X.getAffineXCoord().toBigInteger().mod(N);

        return v.equals(r);
    }

    @Override
    public KeyPair getKeys() throws WTFException {
        if (this.keys == null) {
            // key generation is not implemented. We're relying on BCImpl.getKeys() here
            throw new WTFException("key generation is not implemented. Use BCImpl to generate keys and then OwnImpl.setKeys");
        }
        return this.keys;
    }

    public OwnImpl setKeys(KeyPair keys) {
        this.keys = keys;
        this.N = ((ECPublicKey) keys.getPublic()).getParameters().getN();
        this.G = ((ECPublicKey) keys.getPublic()).getParameters().getG();
        this.Q = ((ECPublicKey) keys.getPublic()).getQ().normalize();
        return this;
    }
}
