package com.example.rahat_pdm.encryptiondemo;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class VincentFileCrypto
{


    Context c;

    private static String DELIMITER = "]";
    private static int ITERATION_COUNT = 1000;
    private static int KEY_LENGTH = 256;
    public static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static SecureRandom random = new SecureRandom();
    private static final int PKCS5_SALT_LENGTH = 8;

    public VincentFileCrypto( Context context) {
        this.c = context;
    }

    /*public VincentFileCrypto(View.OnClickListener onClickListener, Context context) {
        this.c = context;
    }*/

    public static byte[] fromBase64(String base64)
    {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
    public static String toBase64(byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);

    }

    /*public  byte[] decrypt(byte[] cleartext) throws Exception
    {
        String password  = "password";
        String ciphertext = toBase64(cleartext);
        String[] fields = ciphertext.split("]");

        if (fields.length != 3) {
            Toast.makeText(c, "exception", Toast.LENGTH_SHORT).show();
        }
        byte[] salt = fromBase64(fields[0]);
        byte[] iv = fromBase64(fields[1]);
        Toast.makeText( c, "dec1 ", Toast.LENGTH_SHORT).show();
        byte[] cipherBytes = fromBase64(fields[2]);

        Toast.makeText( c, "dec2 ", Toast.LENGTH_SHORT).show();
// as above
        SecretKey key = deriveKeyPbkdf2(salt, password);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        byte[] plaintext = cipher.doFinal(cipherBytes);

        return plaintext;
    }*/
    public  byte[] decrypt(byte[] cipherBytes, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            //Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
            byte[] plaintext = cipher.doFinal(cipherBytes);

            Toast.makeText(c, "Decrypt succeed", Toast.LENGTH_SHORT).show();

            return plaintext;
            /*String plainrStr = new String(plaintext, "UTF-8");

            return plainrStr;*/
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } /*catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }*/
    }
    public  byte[] decryptPbkdf2(byte[] cipherbytes, String password)
    //public  byte[] decryptPbkdf2(String field, String password)
    {
        //String ciphertt = toBase64(cipherbytes);   // toBase64 is the problem
        String ciphertext = null;
        try {
            ciphertext = new String(cipherbytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //String ciphertext = cipherbytes.g

        String[] fields = ciphertext.split(DELIMITER);
        //String[]  fields = field.split(DELIMITER);

        Toast.makeText( c, "fields length is " + fields.length , Toast.LENGTH_SHORT).show();
        if (fields.length != 3)
        {
            Toast.makeText( c, "Invalid encrypted text format", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Invalid encypted text format");

        }

        byte[] salt = fromBase64(fields[0]);
        byte[] iv = fromBase64(fields[1]);
        byte[] cipherBytes = fromBase64(fields[2]);

        SecretKey key = deriveKeyPbkdf2(salt, password);

        return decrypt(cipherBytes, key, iv);
    }

    public static SecretKey deriveKeyPbkdf2(byte[] salt, String password) {
        try {
            long start = System.currentTimeMillis();
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                    ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory
                    .getInstance(PBKDF2_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

            SecretKey result = new SecretKeySpec(keyBytes, "AES");
            long elapsed = System.currentTimeMillis() - start;

            //Toast.makeText(c, "PBKDF2 key derivation took " + elapsed, Toast.LENGTH_SHORT).show();

            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }





























   /* public  byte[] encrypt(byte[] cleartext) throws Exception
    {

        String password  = "password";
        int iterationCount = 1000;
        int keyLength = 256;
        int saltLength = keyLength / 8; // same size as key output

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        byte[] ciphertext = cipher.doFinal(cleartext);

        return ciphertext;
    }*/



    public static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);

        return b;
    }
   public String encrypt(byte[] plaintext, SecretKey key, byte[] salt)
   {

       try
       {
           Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

           byte[] iv = generateIv(cipher.getBlockSize());
           //Log.d(TAG, "IV: " + toHex(iv));
           IvParameterSpec ivParams = new IvParameterSpec(iv);
           cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
           //Log.d(TAG, "Cipher IV: "
                  // + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
           byte[] cipherText = cipher.doFinal(plaintext);

           //Toast.makeText(c, "tested ", Toast.LENGTH_SHORT).show();

           if (salt != null) {
               /*String nn = String.format("%s%s%s%s%s", toBase64(salt), DELIMITER,
                       toBase64(iv), DELIMITER, toBase64(cipherText));
               return fromBase64(nn);*/
               return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER,
                       toBase64(iv), DELIMITER, toBase64(cipherText));
           }

           /*String nn = String.format("%s%s%s", toBase64(iv), DELIMITER,
               toBase64(cipherText));
           return fromBase64(nn);*/
           return String.format("%s%s%s", toBase64(iv), DELIMITER,
                   toBase64(cipherText));
       }
       catch (GeneralSecurityException e) {
           throw new RuntimeException(e);
       } /*catch (UnsupportedEncodingException e) {
           throw new RuntimeException(e);
       }*/
   }
    public static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);

        return b;
    }











    public  byte[] encrypt(String seed, byte[] cleartext) throws Exception {



        byte[] rawKey = getRawKey(seed.getBytes());
        //byte[] rawKey = seed.getBytes();

        byte[] result = encrypt(rawKey, cleartext);

        //  return toHex(result);

        return result;

    }



    public  byte[] decrypt(String seed, byte[] encrypted) throws Exception {

        byte[] rawKey = getRawKey(seed.getBytes());
        //byte[] rawKey = seed.getBytes();

        byte[] enc = encrypted;

        byte[] result = decrypt(rawKey, enc);

        Toast.makeText( c, "dec ", Toast.LENGTH_SHORT).show();

        return result;

    }



//done

    private  byte[] getRawKey(byte[] seed) throws Exception {

        KeyGenerator kgen = KeyGenerator.getInstance("AES");

        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");

        sr.setSeed(seed);

        kgen.init(128, sr); // 192 and 256 bits may not be available

        SecretKey skey = kgen.generateKey();

        byte[] raw = skey.getEncoded();

        return raw;

    }





    private  byte[] encrypt(byte[] raw, byte[] clear) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(clear);

        return encrypted;

    }



    private  byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        Toast.makeText( c, "dec1 ", Toast.LENGTH_SHORT).show();

        byte[] decrypted = cipher.doFinal(encrypted); // problem
        Toast.makeText( c, "dec2 ", Toast.LENGTH_SHORT).show();



        return decrypted;

    }







}
