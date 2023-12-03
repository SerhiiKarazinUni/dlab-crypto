import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Instead of putting ciphertext to one byte array and then parsing it, in this task I am using this class to store it
 */
public class ElGamalCiphertext {
    private BigInteger a;
    private LinkedList<BigInteger> m;
    public ElGamalCiphertext(BigInteger a){
        this.a = a;
        m = new LinkedList<>();
    }

    public ElGamalCiphertext add(BigInteger m){
        this.m.add(m);
        return this;
    }

    public BigInteger getA(){
        return a;
    }

    public int getLength(){
        return m.size();
    }

    public LinkedList<BigInteger> getData(){
        return m;
    }
}
