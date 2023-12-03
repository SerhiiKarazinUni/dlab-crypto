import org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Common routines for ElGamal algorithms
 */
abstract class ElGamalBase {
    private ElGamalPrivKey privKey;
    private ElGamalPubKey pubKey;

    abstract SecureRandom getRandom();

    public void generateKeys() {
        BigInteger p, g, x = BigInteger.ZERO, y;


        // Use pre-calculated p and g
        p = new BigInteger(1, Base64.getDecoder().decode("ANLONhvL4nWVcl+sLmsNWUi+UJSSzpMpVOj6btT2yakXcqe3rmtNPwiTuiZjykdb3urROsz4p8MkFRQT11YENOrJSe7L5pNVTLtl66EMmAlhjwY8TNar5JTDJ4zU8R/KaTiQ3BJrLy2GID1rRVjk6f6Hxmdep44KrhaEb4MKw2AFLb2XW17B+vSA2riTriL9nPT0hbedf7MU6hZEgTAmxo3Cx4CmlediSWxuWYEmOvftT3yyjE50x3uWl5Sg4r45r0BAfsl38C3hbNE9/4162eZ6MPtZKMpTlsxImpFQqj9MXOswZBqhqZI7EWGD4XfgZFAS7tIFXlaDb+TKJbmFmPM="));
        g = new BigInteger(1, Base64.getDecoder().decode("AL1IKM/RQWqr4eF3NfCnNQKnvSCwobQE6ZQ/ItMpQqZlhP78K41YPXs1MXqdPdKjN1ueczhqTZ9+uTP+Xlmhr5c+oYqCTkQA2D6bUc7GGrlHz89ac4LspQ3BGfH0QXYKwzQqE2hBrTYfebA4WnKCJfs7uCrqCXuHaiCUdCXolZh9Gtd/pTVwKcoGKsf2pnMHKhENVRR9i4oajZ2pW44f/2S6ocLGAQoutPlF5gALuYyvP5CyW2SAzaZV3Bs/5nLg9dGVWgfh9Cw8OrT9NTTueaiiY6XUekzanYfY93gOHeNZeKrYqvLpVdtvdpcyR5CTdH85S/l7HzJljJnRNXTIyhw="));


        /*
        // Option 1: using Bouncy Castle
        ElGamalParametersGenerator gen = new ElGamalParametersGenerator();
        gen.init(2048, 2, getRandom());
        ElGamalParameters params = gen.generateParameters();

        p = params.getP();
        BigInteger pMinusTwo = p.subtract(BigInteger.TWO);

        g = params.getG();
        */

        /*
        // Option 2: using own implementation
        // 1. generate p
        p = BigIntegers.createRandomPrime(2048, 2, getRandom());

        // 2. generate g
        BigInteger pMinusTwo = p.subtract(BigInteger.TWO);
        BigInteger o;
        BigInteger k;
        BigInteger pMinusOne = p.subtract(BigInteger.ONE);

        while(true) {
            o = BigInteger.ONE;
            g = BigIntegers.createRandomInRange(BigInteger.TWO, pMinusTwo, getRandom());

            k = g.modPow(BigInteger.ONE, p);
            while (k.compareTo(BigInteger.ONE) > 0)
            {
                o = o.add(BigInteger.ONE);
                k = k.multiply(g).mod(p);
            }
            if(o.compareTo(pMinusOne) == 0){
                break;
            }
        }
        */

        BigInteger pMinusTwo = p.subtract(BigInteger.TWO);

        // 3. choose x
        if(x.equals(BigInteger.ZERO)) {
            x = BigIntegers.createRandomInRange(BigInteger.TWO, pMinusTwo, getRandom());
        }

        // 4. compute y
        y = g.modPow(x, p);

        this.pubKey = new ElGamalPubKey(y, g, p);
        this.privKey = new ElGamalPrivKey(x);
    }

    public ElGamalPrivKey getPrivKey() {
        return privKey;
    }

    public ElGamalPubKey getPubKey() {
        return pubKey;
    }

    public void printKeyInfo(){
        System.out.println("Public key:");
        System.out.println("P: "+Utils.b2hex(getPubKey().getP().toByteArray()));
        System.out.println("G: "+Utils.b2hex(getPubKey().getG().toByteArray()));
        System.out.println("Y: "+Utils.b2hex(getPubKey().getY().toByteArray()));

        System.out.println("Private key:");
        System.out.println("X: "+Utils.b2hex(getPrivKey().getX().toByteArray()));
    }
}
