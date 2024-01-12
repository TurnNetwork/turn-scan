package com.turn.browser.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-128 ECB encryption.<br>
 *
 * <pre>
 *Character set:UTF-8
 * Algorithm mode: ECB
 * Data block: 128 bits
 * Complement method: PKCS5Padding
 * Encryption result encoding method: Base64
 * </pre>
 *
 */
public class AESUtil {
    private static final String UTF8 = "UTF-8";
    private static final String ALGORITHM = "AES";
    /**Default encryption algorithm */
    private static final String ALGORITHM_CIPHER = "AES/ECB/PKCS5Padding";

    private static final int LIMIT_LEN = 16;

    /**
     * Generate a SecretKey
     * @param password length must be less than or equal to 16
     * @return Return SecretKey
     */
    public static SecretKey getSecretKey(String password) {
        byte[] passwordData = password.getBytes();
        if(passwordData.length > LIMIT_LEN) {
            throw new IllegalArgumentException("password length must be less than or equal to 16");
        }
        // Create an empty 16-bit byte array (default value is 0), 16byte (128bit)
        byte[] keyData = new byte[16];
        System.arraycopy(passwordData, 0, keyData, 0, passwordData.length);

        return new SecretKeySpec(keyData, ALGORITHM);
    }

    /**
     * Encryption
     * @param data data to be encrypted
     * @param password password
     * @return Returns the data after successful encryption
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, String password) throws Exception {
        SecretKey secretKey = getSecretKey(password);
        // Ciphr completes the encryption or decryption work class
        Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
        //Initialize Cipher, decryption mode
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        //encrypt data
        return cipher.doFinal(data);
    }

    /**
     * Decrypt
     * @param data data to be decrypted
     * @param password password
     * @return Returns the decrypted data
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String password) throws Exception {
        SecretKey secretKey = getSecretKey(password);
        // Cipher completes the encryption or decryption work class
        Cipher cipher = Cipher.getInstance(ALGORITHM_CIPHER);
        //Initialize Cipher, decryption mode
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        // Decrypt data
        return cipher.doFinal(data);
    }

    /**
     * Text encryption
     * @param content plain text
     * @param password password
     * @return returns base64 content
     * @throws Exception
     */
    public static String encryptToBase64String(String content, String password) throws Exception {
        byte[] data = content.getBytes(UTF8);
        byte[] result = encrypt(data, password);
        return Base64.encodeBase64String(result);
    }

    /**
     * Text decryption
     * @param base64String text to be decrypted
     * @param password password
     * @return Return plain text
     * @throws Exception
     */
    public static String decryptFromBase64String(String base64String, String password) throws Exception {
        byte[] data = Base64.decodeBase64(base64String);
        byte[] contentData = decrypt(data, password);
        return new String(contentData, UTF8);
    }

    /**
     * Text encryption
     * @param content plain text
     * @param password password
     * @return Returns hexadecimal content
     * @throws Exception
     */
    public static String encryptToHex(String content, String password) throws Exception {
        byte[] data = content.getBytes(UTF8);
        byte[] result = encrypt(data, password);
        return Hex.encodeHexString(result);
    }

    /**
     * Text decryption
     * @param hex text to be decrypted
     * @param password password
     * @return Return plain text
     * @throws Exception
     */
    public static String decryptFromHex(String hex, String password) throws Exception {
        byte[] data = Hex.decodeHex(hex);
        byte[] contentData = decrypt(data, password);
        return new String(contentData,UTF8);
    }

}

