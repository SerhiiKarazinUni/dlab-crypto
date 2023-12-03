import java.math.BigInteger;

/**
 * This class holds the private key
 */
class ElGamalPrivKey {
    private final BigInteger x;

    public ElGamalPrivKey(BigInteger x) {
        this.x = x;
    }

    public BigInteger getX() {
        return x;
    }
}
