/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * Crypto
 * 
 * ��ȣȭ ��ƿ Ŭ�����̴�. MD5�� ��ȣȭ�� �� �̰��� �ٽ� BASE64 ���ڵ���
 * �����Ͽ� ��ȣȭ�� String ���� ��ȯ�Ѵ�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util;

import java.security.MessageDigest;

import android.util.Base64;

public class Crypto {
	/**
     * byte[] ret = HashUtil.digest("MD5", "abcd".getBytes());
     *  ó�� ȣ��
     */
    public static byte[] digest(String alg, byte[] input) {
    	try {
	        MessageDigest md = MessageDigest.getInstance(alg);
	        return md.digest(input);
    	} catch (Exception e){
    		return null;
    	}
    }
	
	public static String encrypt(String inputValue) {
        
		try {
			if( inputValue == null ) throw new Exception("Can't conver to Message Digest 5 String value!!");
	        byte[] ret = digest("MD5", inputValue.getBytes());
	        String result = Base64.encodeToString(ret, 0);    
	        return result;
		} catch (Exception e){
			return null;
		}
    }
}
