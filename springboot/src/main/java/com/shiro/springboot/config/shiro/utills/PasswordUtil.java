package com.shiro.springboot.config.shiro.utills;

import org.apache.shiro.crypto.hash.Md5Hash;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtil {

    public  static  String  generatePassword(String password,String salt)
    {
        String passwordMd5= new Md5Hash(password,salt,2).toHex();
        return  passwordMd5;
    }
    /**
     * 生成盐
     * @return
     */
    public static byte[] createSalt(){
        byte[] salt = new byte[16];
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
