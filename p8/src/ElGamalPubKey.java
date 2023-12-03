import java.math.BigInteger;

/**
 * This class holds the public key
 */
class ElGamalPubKey {
    private final BigInteger y, g, p;

    public ElGamalPubKey(BigInteger y, BigInteger g, BigInteger p) {
        this.y = y;
        this.g = g;
        this.p = p;
    }

    public BigInteger getY() {
        return y;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getN(){
        return getP().subtract(BigInteger.ONE);
    }
}
