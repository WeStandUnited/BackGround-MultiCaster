import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypo {

        public static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
            SecretKey keyTmp = keyFactory.generateSecret(keySpec);
            return new SecretKeySpec(keyTmp.getEncoded(), "AES");
        }

        public static byte [] encrypt(byte [] property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
            Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key);
            return  pbeCipher.doFinal(property);
        }


        public static byte [] decrypt(byte [] bytes, SecretKeySpec key) throws GeneralSecurityException, IOException {

            Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            pbeCipher.init(Cipher.DECRYPT_MODE, key);
            return pbeCipher.doFinal(bytes);
        }


}
