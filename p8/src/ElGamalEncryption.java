import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Encryption algorithm implementation
 */
class ElGamalEncryption extends ElGamalBase {
    private SecureRandom random;

    public ElGamalEncryption() {
        random = new SecureRandom();
    }

    @Override
    SecureRandom getRandom() {
        return random;
    }

    /**
     * @param message plaintext message
     * @param elGamalSpec you can provide null to use current keys, or an instance of ElGamalBase to use its keys
     * @return ciphertext instance
     */
    public ElGamalCiphertext encrypt(byte[] message, ElGamalBase elGamalSpec) {
        if (elGamalSpec == null) {
            elGamalSpec = this;
        }

        BigInteger g = elGamalSpec.getPubKey().getG();
        BigInteger p = elGamalSpec.getPubKey().getP();
        BigInteger y = elGamalSpec.getPubKey().getY();

        //BigInteger k = new BigInteger("257");
        BigInteger k;
        do
        {
            k = BigIntegers.createRandomInRange(p.divide(BigInteger.TWO), p.subtract(BigInteger.ONE), getRandom());
        }while( k.gcd( p.subtract(BigInteger.ONE) ).compareTo(BigInteger.ONE) != 0 );

        BigInteger a = g.modPow(k, p);
        ElGamalCiphertext ciphertext = new ElGamalCiphertext(a);

        byte[] byteBlock;

        for (int i = 0; i < message.length; i += 256) {
            byteBlock = new byte[256];
            System.arraycopy(message, i, byteBlock, 0, Math.min(256, message.length - i));
            BigInteger block = new BigInteger(byteBlock);

            BigInteger b = block.multiply(y.modPow(k, p)).mod(p);

            ciphertext.add(b);
        }

        return ciphertext;
    }


    /**
     * @param ciphertext ciphertext instance
     * @param elGamalSpec you can provide null to use current keys, or an instance of ElGamalBase to use its keys
     * @return plaintext message
     */
    public byte[] decrypt(ElGamalCiphertext ciphertext, ElGamalBase elGamalSpec) {
        if (elGamalSpec == null) {
            elGamalSpec = this;
        }

        BigInteger p = elGamalSpec.getPubKey().getP();
        BigInteger x = elGamalSpec.getPrivKey().getX();
        BigInteger a = ciphertext.getA();

        byte[] message = new byte[ciphertext.getLength() * 256]; // this implementation has 2048-bit block size

        int i = 0;
        for (BigInteger b : ciphertext.getData()) {
            BigInteger plaintext = b.multiply(a.modPow(x, p).modInverse(p)).mod(p);
            byte[] plaintextBytes = Utils.toUnsignedByteArray(plaintext);
            System.arraycopy(plaintextBytes, 0, message, i, plaintextBytes.length);
            i += 256;
        }

        return message;

    }
}
