package com.turn.browser.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 * jasypt encryption tool class
 */
public class JasyptUtil {

    /**
     * Encryption
     *
     * @param salt: salt
     * @param encrypt: The string that needs to be encrypted
     */
    public static String encryptor(String salt, String encrypt) {
        if (StrUtil.isBlank(salt)) {
            salt = "my123456";
        }
        if (StrUtil.isBlank(encrypt)) {
            return "";
        }
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(salt);
        return textEncryptor.encrypt(encrypt);
    }

    /**
     * Decrypt
     *
     * @param salt: salt
     * @param encrypt: the string to be decrypted
     * @return: java.lang.String
     */
    public static String decryptor(String salt, String encrypt) {
        if (StrUtil.isBlank(salt)) {
            salt = "my123456";
        }
        if (StrUtil.isBlank(encrypt)) {
            return "";
        }
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(salt);
        return textEncryptor.decrypt(encrypt);
    }

    public static void main(String[] args) {
        Console.log("========{}", encryptor("", "platscan"));
        Console.log("========{}", encryptor("", "elastic"));
        Console.log("========{}", encryptor("", "changeme"));
        Console.log("========{}", encryptor("", "GOJ4hui834hGIhHIh33984dG3DER4Gh784u9dh"));
        Console.log("========{}", encryptor("", "f982e1a8f14444c7a484dc16bcad7741"));
    }

}