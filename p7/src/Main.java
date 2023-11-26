import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;


public class Main {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static String b2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ").append(System.lineSeparator()).append("\t");
        int k = 0;
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
            k++;
            if(k%16 == 0 && k<bytes.length) sb.append(System.lineSeparator()).append("\t");
        }
        sb.append(System.lineSeparator()).append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        String sMessage = "Hello, world";

        if(args.length >= 1){
            sMessage = args[0];
        }

        if(args.length >= 2){
            MyRandomness.useFakeRandomness = args[1].equals("1");
        }

        try {
            byte[] message = sMessage.getBytes(StandardCharsets.UTF_8);
            System.out.println("Message:\n" + b2hex(message));

            // test Bouncy Castle ECDSA
            BCimpl bouncyCastleECDSA = new BCimpl();
            byte[] bcSignature = bouncyCastleECDSA.getSignature(message);
            System.out.println("BC signature:\n" + b2hex(bcSignature));

            boolean bouncyCastleSmokeTestResult = bouncyCastleECDSA.checkSignature(message, bcSignature);
            System.out.println("Bouncy Castle ECDSA smoke test (valid signature): " + (bouncyCastleSmokeTestResult ? "PASSED" : "NOT PASSED"));

            byte[] brokenSignature = bouncyCastleECDSA.getSignature(Arrays.copyOf(message, 5));
            bouncyCastleSmokeTestResult = bouncyCastleECDSA.checkSignature(message, brokenSignature);
            System.out.println("Bouncy Castle ECDSA smoke test (invalid signature): " + (bouncyCastleSmokeTestResult ? "NOT PASSED" : "PASSED"));

            // test own implementation using bouncy castle
            OwnImpl ownECDSA = new OwnImpl();
            ownECDSA.setKeys(bouncyCastleECDSA.getKeys());

            byte[] ownSignature = ownECDSA.getSignature(message);
            System.out.println("Own signature (should be the same with BC's if you use 'MyRandomness'):\n" + b2hex(ownSignature));
            boolean ownSignatureSmokeTestResult = bouncyCastleECDSA.checkSignature(message, ownSignature);
            System.out.println("Own ECDSA smoke test using Bouncy Castle verify (valid signature): " + (ownSignatureSmokeTestResult ? "PASSED" : "NOT PASSED"));

            ownSignatureSmokeTestResult = ownECDSA.checkSignature(message, ownSignature);
            System.out.println("Own ECDSA smoke test using own verify (valid signature): " + (ownSignatureSmokeTestResult ? "PASSED" : "NOT PASSED"));

            ownSignatureSmokeTestResult = ownECDSA.checkSignature(message, brokenSignature);
            System.out.println("Own ECDSA smoke test using own verify (invalid signature): " + (ownSignatureSmokeTestResult ? "NOT PASSED" : "PASSED"));

        }catch (WTFException e){
            e.giveInformation();
        }
    }
}