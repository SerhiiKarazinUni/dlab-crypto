import java.security.KeyPair;

interface DoesECDSA {
    byte[] getSignature(byte[] plaintext) throws WTFException;

    boolean checkSignature(byte[] message, byte[] signature) throws WTFException;

    KeyPair getKeys() throws WTFException;
}
