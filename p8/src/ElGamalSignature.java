import java.math.BigInteger;

/**
 * Instead of putting signature to one byte array and then parsing it, in this task I am using this class to store it
 */
public class ElGamalSignature {
    private BigInteger r,s;

    public ElGamalSignature(BigInteger r, BigInteger s) {
        this.r = r;
        this.s = s;
    }

    public BigInteger getR() {
        return r;
    }

    public BigInteger getS() {
        return s;
    }

    public void printSignatureInfo() {
        System.out.println("Signature:");
        System.out.println("R: "+Utils.b2hex(getR().toByteArray()));
        System.out.println("S: "+Utils.b2hex(getS().toByteArray()));
    }
}
