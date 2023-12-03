import java.math.BigInteger;

public class Utils {
    public static byte[] toUnsignedByteArray(BigInteger bi) {
        byte[] ba = bi.toByteArray();
        if (ba[0] != 0) {
            return ba;
        } else {
            byte[] ba2 = new byte[ba.length - 1];
            System.arraycopy(ba, 1, ba2, 0, ba.length - 1);
            return ba2;
        }
    }

    public static String b2hex(byte[] bytes) {
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
}
