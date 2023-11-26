import java.security.SecureRandom;

/**
 * Fake randomness source, needed to generate constant K values
 * Sustainable K values allows us to compare signatures between own implementation and BC's
 */
class MyRandomness extends SecureRandom {
    public static boolean useFakeRandomness = true;

    @Override
    public void nextBytes(byte[] bytes) {
        if(useFakeRandomness) {
            System.arraycopy(CommonConsts.K, 0, bytes, 0, 32); //32 bytes for K
        }else {
            super.nextBytes(bytes);
        }
    }
}
