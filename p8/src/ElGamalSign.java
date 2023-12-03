import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * ElGamal signature implementation
 */
public class ElGamalSign extends ElGamalBase {
    private SecureRandom random;

    public ElGamalSign() {
        random = new SecureRandom();
    }

    @Override
    SecureRandom getRandom() {
        return random;
    }

    /**
     * @param message plaintext message
     * @param elGamalSpec you can provide null to use current keys, or an instance of ElGamalBase to use its keys
     * @return instance of ElGamalSignature
     */
    public ElGamalSignature sign(byte[] message, ElGamalBase elGamalSpec){
        if (elGamalSpec == null) {
            elGamalSpec = this;
        }

        BigInteger p = elGamalSpec.getPubKey().getP();
        BigInteger g = elGamalSpec.getPubKey().getG();
        BigInteger a = elGamalSpec.getPrivKey().getX();

        //BigInteger k = new BigInteger("257");
        BigInteger k;
        do
        {
            k = BigIntegers.createRandomInRange(p.divide(BigInteger.TWO), p.subtract(BigInteger.ONE), getRandom());
        }while( k.gcd( p.subtract(BigInteger.ONE) ).compareTo(BigInteger.ONE) != 0 );

        BigInteger r = g.modPow(k, p);

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(CommonConsts.SHA256);
        } catch (NoSuchAlgorithmException e) {
            //pass
        }
        BigInteger h = new BigInteger(1, digest.digest(message));
        BigInteger s = h.subtract(a.multiply(r)).multiply(k.modInverse(p.subtract(BigInteger.ONE))).mod(p.subtract(BigInteger.ONE));

        return new ElGamalSignature(r,s);
    }

    /**
     * @param message plaintext message
     * @param signature instance of ElGamalSignature returned by sign() call
     * @param elGamalSpec you can provide null to use current keys, or an instance of ElGamalBase to use its keys
     * @return true, if the signature is valid
     */
    public boolean check(byte[] message, ElGamalSignature signature, ElGamalBase elGamalSpec){
        if (elGamalSpec == null) {
            elGamalSpec = this;
        }

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(CommonConsts.SHA256);
        } catch (NoSuchAlgorithmException e) {
            //pass
        }
        BigInteger h = new BigInteger(1, digest.digest(message));

        BigInteger r = signature.getR();
        BigInteger s = signature.getS();
        BigInteger p = elGamalSpec.getPubKey().getP();
        BigInteger y = elGamalSpec.getPubKey().getY();
        BigInteger g = elGamalSpec.getPubKey().getG();

        if(r.compareTo(BigInteger.ZERO) <= 0
                || s.compareTo(BigInteger.ZERO) <= 0
                || r.compareTo(p) >= 1
                || s.compareTo(p.subtract(BigInteger.ONE)) >= 1){
            return false;
        }

        BigInteger right = g.modPow(h, p).mod(p);
        BigInteger left = y.modPow(r, p).multiply(r.modPow(s, p)).mod(p);

        return right.equals(left);

    }
}
