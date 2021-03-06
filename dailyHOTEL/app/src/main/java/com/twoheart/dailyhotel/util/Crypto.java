/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 * <p>
 * Crypto
 * <p>
 * 암호화 유틸 클래스이다. MD5로 암호화한 뒤 이것을 다시 BASE64 인코딩을
 * 수행하여 암호화된 String 값을 반환한다.
 *
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 * @since 2014-02-24
 */
package com.twoheart.dailyhotel.util;

import android.util.Base64;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;

import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto
{
    private final static String HEX = "0123456789ABCDEF";
    private static final int SEED_LENGTH = 16;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // AES암호
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static byte[] digest(String alg, byte[] input)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(alg);
            return md.digest(input);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static String encrypt(String inputValue)
    {
        try
        {
            if (inputValue == null)
            {
                throw new Exception("Can't conver to Message Digest 5 String value!!");
            }
            byte[] ret = digest("MD5", inputValue.getBytes());
            return Base64.encodeToString(ret, Base64.NO_WRAP);
        } catch (Exception e)
        {
            return null;
        }
    }

    public static String encrypt(String seed, String text) throws Exception
    {
        if (DailyTextUtils.isTextEmpty(text) == true)
        {
            return null;
        }

        byte[] rawKey = seed.getBytes("UTF-8");
        byte[] result = encrypt(rawKey, text.getBytes());
        String fromHex = toHex(result);

        return Base64.encodeToString(fromHex.getBytes(), Base64.NO_WRAP);
    }

    public static String decrypt(String seed, String encrypted) throws Exception
    {
        if (DailyTextUtils.isTextEmpty(encrypted) == true)
        {
            return null;
        }

        String base64 = new String(Base64.decode(encrypted, Base64.NO_WRAP));
        byte[] rawKey = seed.getBytes("UTF-8");
        byte[] enc = toByte(base64);
        byte[] result = decrypt(rawKey, enc);

        return new String(result);
    }

    public static String getUrlEncoder(final String url)
    {
        StringBuilder encodeUrl = new StringBuilder();
        StringBuilder seedLocationNumber = new StringBuilder();

        try
        {
            String alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

            Random random = new Random(System.currentTimeMillis());
            StringBuilder seed = new StringBuilder();

            for (int i = 0; i < SEED_LENGTH; i++)
            {
                int number = random.nextInt(alphas.length());
                seed.append(alphas.charAt(number));
            }

            String firstUrl = Crypto.encrypt(seed.toString(), url);
            encodeUrl.append(firstUrl);

            for (int i = 0; i < SEED_LENGTH; i++)
            {
                int number = random.nextInt(encodeUrl.length());

                encodeUrl.insert(number, seed.charAt(i));
                seedLocationNumber.append(number).append('$');
            }

            String base64LocationNumber = Base64.encodeToString(seedLocationNumber.toString().getBytes(), Base64.NO_WRAP);
            encodeUrl.insert(0, base64LocationNumber + "$");
            encodeUrl.append('$');

            ExLog.d("encoderUrl : " + encodeUrl.toString());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return encodeUrl.toString();
    }

    public static String getUrlDecoderEx(String url, Map<String, String> urlParameters)
    {
        if (urlParameters == null || urlParameters.size() == 0)
        {
            return getUrlDecoderEx(url);
        } else
        {
            StringBuilder decodeUrl = new StringBuilder(getUrlDecoderEx(url));
            ArrayList<String> keyArrayList = new ArrayList<>(urlParameters.keySet());

            for (String key : keyArrayList)
            {
                String value = urlParameters.get(key);

                if (DailyTextUtils.isTextEmpty(key, value) == false)
                {
                    int startIndex = decodeUrl.indexOf(key);
                    if (startIndex >= 0)
                    {
                        decodeUrl.replace(startIndex, startIndex + key.length(), value);
                    } else
                    {
                        throw new StringIndexOutOfBoundsException("getUrlDecoderEx - Failed decoding : " //
                            + decodeUrl + " , key : " + key + ", value : " + value + " , index : " + startIndex);
                    }
                } else
                {
                    throw new InvalidParameterException("Invalid url parameter : key : " + key + ", value : " + value);
                }
            }

            return decodeUrl.toString();
        }
    }

    public static String getUrlDecoderEx(String url)
    {
        if (Constants.UNENCRYPTED_URL == true)
        {
            return url;
        }

        String param = null;
        String encoderUrl;

        if (url.contains("/") == true)
        {
            int index = url.indexOf('/');
            param = url.substring(index);
            encoderUrl = url.substring(0, index);
        } else if (url.contains("?") == true)
        {
            int index = url.indexOf('?');
            param = url.substring(index);
            encoderUrl = url.substring(0, index);
        } else
        {
            encoderUrl = url;
        }

        StringBuilder decodeUrl = new StringBuilder();
        String[] seperateUrl = encoderUrl.split("\\$");

        int count = seperateUrl.length / 2;

        // 앞의것 2개는 Url, 뒤의것 2개는 API
        for (int i = 0; i < count; i++)
        {
            String locatinoNumber = new String(Base64.decode(seperateUrl[i * 2], Base64.NO_WRAP));
            decodeUrl.append(getUrlDecoder(locatinoNumber + seperateUrl[i * 2 + 1]));
        }

        if (param != null)
        {
            decodeUrl.append(param);
        }

        return decodeUrl.toString();
    }

    private static String getUrlDecoder(String url)
    {
        String decodeUrl = null;
        String[] text = url.split("\\$");

        StringBuilder seed = new StringBuilder();
        StringBuilder base64Url = new StringBuilder(text[SEED_LENGTH]);
        char[] alpha = new char[1];

        for (int i = SEED_LENGTH - 1; i >= 0; i--)
        {
            try
            {
                int location = Integer.parseInt(text[i]);

                base64Url.getChars(location, location + 1, alpha, 0);
                base64Url.delete(location, location + 1);

                seed.insert(0, alpha);
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        try
        {
            decodeUrl = Crypto.decrypt(seed.toString(), base64Url.toString());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return decodeUrl;
    }

    public static String urlEncrypt(final String url)
    {
        StringBuilder encodeUrl = new StringBuilder();
        StringBuilder seedLocationNumber = new StringBuilder();

        try
        {
            String alphas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

            Random random = new Random(System.currentTimeMillis());
            StringBuilder seed = new StringBuilder();

            for (int i = 0; i < SEED_LENGTH; i++)
            {
                int number = random.nextInt(alphas.length());
                seed.append(alphas.charAt(number));
            }

            String firstUrl = Crypto.encrypt(seed.toString(), url);
            encodeUrl.append(firstUrl);

            for (int i = 0; i < SEED_LENGTH; i++)
            {
                int number = random.nextInt(encodeUrl.length());

                encodeUrl.insert(number, seed.charAt(i));
                seedLocationNumber.append(number).append('$');
            }

            String base64LocationNumber = Base64.encodeToString(seedLocationNumber.toString().getBytes(), Base64.NO_WRAP);
            encodeUrl.insert(0, base64LocationNumber + "$");
            encodeUrl.append('$');

            ExLog.d(url + " : " + encodeUrl.toString());
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        return encodeUrl.toString();
    }

    public static String urlDecrypt(String url)
    {
        if (DailyTextUtils.isTextEmpty(url) == true)
        {
            return null;
        }

        String param = null;
        String encoderUrl;

        if (url.contains("/") == true)
        {
            int index = url.indexOf('/');
            param = url.substring(index);
            encoderUrl = url.substring(0, index);
        } else if (url.contains("?") == true)
        {
            int index = url.indexOf('?');
            param = url.substring(index);
            encoderUrl = url.substring(0, index);
        } else
        {
            encoderUrl = url;
        }

        StringBuilder decodeUrl = new StringBuilder();
        String[] seperateUrl = encoderUrl.split("\\$");

        int count = seperateUrl.length / 2;

        // 앞의것 2개는 Url, 뒤의것 2개는 API
        for (int i = 0; i < count; i++)
        {
            String locatinoNumber = new String(Base64.decode(seperateUrl[i * 2], Base64.NO_WRAP));
            decodeUrl.append(getUrlDecoder(locatinoNumber + seperateUrl[i * 2 + 1]));
        }

        if (param != null)
        {
            decodeUrl.append(param);
        }

        return decodeUrl.toString();
    }

    private static byte[] encrypt(byte[] key, byte[] value) throws Exception
    {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(value);
    }

    private static byte[] decrypt(byte[] key, byte[] value) throws Exception
    {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(value);
    }

    private static byte[] toByte(String hexString)
    {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
        {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    private static String toHex(byte[] buffers)
    {
        if (buffers == null)
        {
            return "";
        }

        StringBuffer result = new StringBuffer(2 * buffers.length);

        for (byte buffer : buffers)
        {
            appendHex(result, buffer);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b)
    {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}
