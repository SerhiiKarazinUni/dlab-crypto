import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Random;

class Lab6{
    private ECDomainParameters domainParams;

    public Lab6(String name) throws Exception {
        X9ECParameters ecP = ECNamedCurveTable.getByName(name);

        if (ecP == null)
            throw new Exception("unknown curve name: " + name);

        this.domainParams = new ECDomainParameters(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());
    }
    public ECPoint BasePointGGet(){
        return domainParams.getG().normalize();
    }

    public ECPoint ECPointGen(BigInteger x, BigInteger y){
        return domainParams.getCurve().createPoint(x, y);
    }

    public boolean IsOnCurveCheck(ECPoint point){
        return point.isValid();
    }

    public ECPoint AddECPoints(ECPoint a, ECPoint b){
        return a.add(b).normalize();
    }

    public ECPoint DoubleECPoints(ECPoint a){
        return a.twice().normalize();
    }

    public ECPoint ScalarMult(BigInteger arg, ECPoint a){
        return a.multiply(arg).normalize();
    }

    public String ECPointToString(ECPoint a){
        return Base64.getEncoder().encodeToString(a.getEncoded(true));
    }

    public ECPoint StringToECPoint(String s){
        return domainParams.getCurve().decodePoint(Base64.getDecoder().decode(s));
    }

    public void PrintECPoint(ECPoint a){
        System.out.print(a.getXCoord().toString() + ", "+ a.getYCoord().toString());
    }
}

public class Main {

    public static void main(String[] args) throws Exception {
        Lab6 lab = new Lab6("secp256k1");

        ECPoint G = lab.BasePointGGet();

        Random rand = new Random();
        BigInteger k = BigInteger.valueOf(rand.nextInt(256));
        BigInteger d = BigInteger.valueOf(rand.nextInt(256));

        ECPoint H1 = lab.ScalarMult(d, G);
        ECPoint H2 = lab.ScalarMult(k, H1);

        ECPoint H3 = lab.ScalarMult(k, G);
        ECPoint H4 = lab.ScalarMult(d, H3);

        boolean result = H2.equals(H4);

        System.out.print("H1 = (");
        lab.PrintECPoint(H1);
        System.out.println(")");

        System.out.print("H3 = (");
        lab.PrintECPoint(H2);
        System.out.println(")");

        System.out.print("H3 = (");
        lab.PrintECPoint(H3);
        System.out.println(")");

        System.out.print("H4 = (");
        lab.PrintECPoint(H4);
        System.out.println(")");

        System.out.println("Result: "+(result?"TRUE":"FALSE"));
    }
}