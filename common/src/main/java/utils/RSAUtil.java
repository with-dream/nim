package utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 是非对称的密码算法，密钥分公钥和私钥，公钥用来加密，私钥用于解密
 */

public class RSAUtil {
    public static final String CHARSET = "UTF-8";

    /**
     * 生成密钥对：密钥对中包含公钥和私钥
     *
     * @return 包含 RSA 公钥与私钥的 keyPair
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static KeyPair getKeyPair() {
        KeyPairGenerator keyPairGenerator = null;    // 获得RSA密钥对的生成器实例
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom secureRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes("utf-8")); // 说的一个安全的随机数
            keyPairGenerator.initialize(1024, secureRandom);    // 这里可以是1024、2048 初始化一个密钥对
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();   // 获得密钥对
        return keyPair;
    }

    /**
     * 获取公钥 (并进行Base64编码，返回一个 Base64 编码后的字符串)
     *
     * @param keyPair
     * @return 返回一个 Base64 编码后的公钥字符串
     */
    public static String getPublicKey(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return base64;
    }

    /**
     * 获取私钥(并进行Base64编码，返回一个 Base64 编码后的字符串)
     *
     * @param keyPair
     * @return 返回一个 Base64 编码后的私钥字符串
     */
    public static String getPrivateKey(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将Base64编码后的公钥转换成 PublicKey 对象
     *
     * @param pubStr
     * @return PublicKey
     */
    public static PublicKey string2PublicKey(String pubStr) {
        byte[] bytes = Base64.getDecoder().decode(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将Base64编码后的私钥转换成 PrivateKey 对象
     *
     * @param priStr
     * @return PrivateKey
     */
    public static PrivateKey string2Privatekey(String priStr) {
        byte[] bytes = Base64.getDecoder().decode(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 公钥加密
     *
     * @param content   待加密的内容 byte[]
     * @param publicKey 加密所需的公钥对象 PublicKey
     * @return 加密后的字节数组 byte[]
     */
    public static byte[] publicEncrytype(byte[] content, PublicKey publicKey) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            return cipher.doFinal(content);
            return rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, content, 128);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 私钥解密
     *
     * @param content    待解密的内容 byte[]
     * @param privateKey 解密需要的私钥对象 PrivateKey
     * @return 解密后的字节数组 byte[]
     */
    public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//            return cipher.doFinal(content);
            return rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, content, 128);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
        int maxBlock = 0;  //最大块
        if (opmode == Cipher.DECRYPT_MODE) {
            maxBlock = keySize;
        } else {
            maxBlock = keySize - 11;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try {
            while (datas.length > offSet) {
                if (datas.length - offSet > maxBlock) {
                    //可以调用以下的doFinal（）方法完成加密或解密数据：
                    buff = cipher.doFinal(datas, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(datas, offSet, datas.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
        } catch (Exception e) {
            throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
        }
        byte[] resultDatas = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultDatas;
    }

    public static void main(String[] args) {
//        String text = "abcdefg456+-=";   // 明文内容
        String text = "jIMPrizRW+wCZOy4zE8R/8x+DTwXh2eik+a/jYHHxUL3uzH8SX9v8WXmqUec+RPRqPN9V7wkEByFIL5QcBlPU97x8f9COGZIxaPqnDSX3puMvTCMk0sqPYw31HXjejJM6MvaONcQAdWuRexrwEAiCVLTqvaTBFOs+gOzvrs3yJ8MLdz+xakuyl6lKyARbGtGToiOzdSr/jqPW/SLnV2h70bQzLK3kGOLR08wJiaDANOMZ59jlJEqAUR58aargcF6hdBZQmn5DKXNGQvA5r2gBP6Gk0utd6b7Sfdgfnsqb+gfGL8XW+ueKCHB7AovVjWgjt1ntjxYhOAyh0nGiZ16jQ==";
        byte[] content = Base64.getEncoder().encode(text.getBytes());
        System.out.println("原始字符串是：" + text);
        try {
            // 获得密钥对
            KeyPair keyPair = RSAUtil.getKeyPair();
            // 获得进行Base64 加密后的公钥和私钥 String
            String privateKeyStr = RSAUtil.getPrivateKey(keyPair);
            String publicKeyStr = RSAUtil.getPublicKey(keyPair);
            System.out.println("Base64处理后的私钥：" + privateKeyStr + "\n"
                    + "Base64处理后的公钥：" + publicKeyStr);

            // 获得原始的公钥和私钥，并以字符串形式打印出来
            PrivateKey privateKey = RSAUtil.string2Privatekey(privateKeyStr);
            PublicKey publicKey = RSAUtil.string2PublicKey(publicKeyStr);

            // 公钥加密/私钥解密
            byte[] publicEncryBytes = RSAUtil.publicEncrytype(content, publicKey);
            String baseContent = Base64.getEncoder().encodeToString(publicEncryBytes);
            System.out.println("公钥加密后的字符串(经BASE64处理)：" + Base64.getEncoder().encodeToString(publicEncryBytes));

            byte[] en = Base64.getDecoder().decode(baseContent);
            byte[] privateDecryBytes = RSAUtil.privateDecrypt(en, privateKey);
            System.out.println("私钥解密后的原始字符串：" + new String(privateDecryBytes));

            String privateDecryStr = new String(Base64.getDecoder().decode(privateDecryBytes), "utf-8");
            System.out.println("私钥解密后的原始字符串：" + privateDecryStr);
            if (text.equals(privateDecryStr)) {
                System.out.println("测试通过！");
            } else {
                System.out.println("测试未通过！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

