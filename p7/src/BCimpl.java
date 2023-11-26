import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.*;

/**
 * This class wraps Bouncy Castle ECDSA implementation to the DoesECDSA interface
 * Interesting points:
 * 1. it uses MyRandomness class as the source of randomness. It's a fake random values source, so the signatures
 *    are deterministic
 * 2. it's getKeys method is used in OwnImpl class as my implementation does not generate keys. Again, it's for being
 *    able to compare signatures
 */
class BCimpl implements DoesECDSA {
    private KeyPair keys;

    @Override
    public byte[] getSignature(byte[] plaintext) throws WTFException {
        byte[] signature = new byte[]{};
        Signature ecdsaSign = null;
        try {
            ecdsaSign = Signature.getInstance(CommonConsts.SHA256WithPlainECDSA, CommonConsts.BouncyCastleProvider);

            ecdsaSign.initSign(getKeys().getPrivate(), new MyRandomness());
            // NOTE: if you want to have really random K's, use this:
            //ecdsaSign.initSign(getKeys().getPrivate());

            ecdsaSign.update(plaintext);
            signature = ecdsaSign.sign();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            throw new WTFException();
        }
        return signature;
    }

    @Override
    public boolean checkSignature(byte[] plaintext, byte[] signature) throws WTFException {
        try {
            Signature ecdsaVerify = Signature.getInstance(CommonConsts.SHA256WithPlainECDSA, CommonConsts.BouncyCastleProvider);
            ecdsaVerify.initVerify(getKeys().getPublic());
            ecdsaVerify.update(plaintext);
            return ecdsaVerify.verify(signature);
        } catch (NoSuchAlgorithmException | WTFException | SignatureException | InvalidKeyException |
                 NoSuchProviderException e) {
            throw new WTFException();
        }
    }

    @Override
    public KeyPair getKeys() throws WTFException {
        if (keys == null) {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CommonConsts.SECP256K1);
            KeyPairGenerator g = null;
            try {
                g = KeyPairGenerator.getInstance(CommonConsts.ECDSA, CommonConsts.BouncyCastleProvider);
                g.initialize(ecSpec, new SecureRandom());
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                throw new WTFException();
            }
            keys = g.generateKeyPair();
        }
        return keys;
    }
}
