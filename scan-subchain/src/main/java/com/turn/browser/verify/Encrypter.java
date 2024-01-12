package com.turn.browser.verify;

/**
 * Responsible for encryption and decryption
 */
public interface Encrypter {

    /**
     * AES text encryption
     *
     * @param content plain text
     * @param password password
     * @return Returns hexadecimal content
     * @throws Exception
     */
    String aesEncryptToHex(String content, String password) throws Exception;

    /**
     * AES text decryption
     *
     * @param hex Text to be decrypted, hexadecimal content
     * @param password password
     * @return Return plain text
     * @throws Exception
     */
    String aesDecryptFromHex(String hex, String password) throws Exception;

    /**
     * AES text encryption
     *
     * @param content plain text
     * @param password password
     * @return returns base64 content
     * @throws Exception
     */
    String aesEncryptToBase64String(String content, String password) throws Exception;

    /**
     * AES text decryption
     *
     * @param base64String text to be decrypted, hexadecimal content
     * @param password password
     * @return Return plain text
     * @throws Exception
     */
    String aesDecryptFromBase64String(String base64String, String password) throws Exception;

    /**
     * RSA private key decryption
     *
     * @param data decrypted content
     * @param privateKey private key
     * @return Return plain text
     * @throws Exception
     */
    String rsaDecryptByPrivateKey(String data, String privateKey) throws Exception;

    /**
     * New version of rsa private key decryption
     * @param data decrypted content
     * @param privateKey private key
     * @return Return plain text
     * @throws Exception
     */
    String rsaDecryptByPrivateKeyNew(String data, String privateKey) throws Exception;

    /**
     * RSA private key encryption
     *
     * @param data plain text
     * @param privateKey private key
     * @return Return ciphertext
     * @throws Exception
     */
    String rsaEncryptByPrivateKey(String data, String privateKey) throws Exception;

    /**
     * New version of rsa private key encryption
     * @param data plain text
     * @param privateKey private key
     * @return Return ciphertext
     * @throwsException
     */
    String rsaEncryptByPrivateKeyNew(String data, String privateKey) throws Exception;

    /**
     * md5 encryption, all lowercase
     *
     * @param value
     * @return Return md5 content
     */
    String md5(String value);
}

