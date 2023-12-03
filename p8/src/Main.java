import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        String plaintext = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt " +
                "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
                "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit " +
                "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
                "culpa qui officia deserunt mollit anim id est laborum. Sed ut perspiciatis unde omnis iste natus " +
                "error sit voluptatem acc";

        System.out.println("Generating keys, this may take some time...");

        ElGamalEncryption cipher = new ElGamalEncryption();
        cipher.generateKeys();
        cipher.printKeyInfo();

        System.out.println("=== [ 1. SIGNATURE ] ===");

        ElGamalSign signer = new ElGamalSign();
        ElGamalSignature signature = signer.sign(plaintext.getBytes(StandardCharsets.UTF_8), cipher);
        signature.printSignatureInfo();

        boolean checkResult = signer.check(plaintext.getBytes(StandardCharsets.UTF_8), signature, cipher);

        System.out.println("Check message signature\n\t" + (checkResult ? "PASSED" : "NOT PASSED"));

        checkResult = signer.check(new byte[]{1,2,3,4,5}, signature, cipher);
        System.out.println("Check other message signature\n\t" + (!checkResult ? "PASSED" : "NOT PASSED"));

        System.out.println("=== [ 2. ENCRYPTION ] ===");

        System.out.println("Encrypting and decrypting the message:");
        ElGamalCiphertext ciphertext = cipher.encrypt(plaintext.getBytes(StandardCharsets.UTF_8), null);
        String decryptedText = new String(cipher.decrypt(ciphertext, null));

        System.out.println("\t"+(decryptedText.equals(plaintext) ? "PASSED" : "NOT PASSED"));

        // ElGamalEncryption generates new keys during instantiation. Let's see how it works with wrong keys
        System.out.println("Generating keys, this may take some time...");
        ElGamalEncryption otherCipher = new ElGamalEncryption();
        otherCipher.generateKeys();
        otherCipher.printKeyInfo();

        System.out.println("Decrypting the message with wrong keys:");
        decryptedText = new String(cipher.decrypt(ciphertext, otherCipher));

        System.out.println("\t"+(decryptedText.equals(plaintext) ? "NOT PASSED" : "PASSED"));
    }
}