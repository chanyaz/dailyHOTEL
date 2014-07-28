/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * VolleyHttpClient
 * 
 * ��Ʈ��ũ �̹��� ó�� �� ��Ʈ��ũ ó�� �۾��� ����ϴ� �ܺ� ���̺귯�� Vol
 * ley�� ��Ʈ��ũ ó�� �۾��� �������� ����ϱ� ���� �����ϴ� ��ƿ Ŭ�����̴�. 
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.util.network;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;
import com.twoheart.dailyhotel.util.AvailableNetwork;
import com.twoheart.dailyhotel.util.Constants;

public class VolleyHttpClient implements Constants {

	private static final String KEY_DAILYHOTEL_COOKIE = "JSESSIONID";

	public static final int TIME_OUT = 5000;
	public static final int MAX_RETRY = 2;

	private static RequestQueue sRequestQueue;
	private static Context sContext;
	private static HttpClient sHttpClient;

	public static void init(Context context) {

		HttpParams params = new BasicHttpParams();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				registry);
		sHttpClient = new DefaultHttpClient(cm, params);

		sContext = context;
		sRequestQueue = Volley.newRequestQueue(sContext, new HttpClientStack(
				sHttpClient));
		// sRequestQueue = Volley.newRequestQueue(sContext);
		
	}

	public static RequestQueue getRequestQueue() {
		if (sRequestQueue == null) init(sContext);
		return sRequestQueue;
		
	}
	
	public static Boolean isAvailableNetwork() {
		boolean result = false;

		AvailableNetwork availableNetwork = AvailableNetwork.getInstance();

		switch (availableNetwork.getNetType(sContext)) {
		case AvailableNetwork.NET_TYPE_WIFI:
			// WIFI �������
			result = true;
			break;
		case AvailableNetwork.NET_TYPE_3G:
			// 3G Ȥ�� LTE���� ����
			result = true;
			break;
		case AvailableNetwork.NET_TYPE_NONE:
			result = false;
			break;
		}
		return result;
	}

	// ���� response�κ��� cookie�� ������ �����.
	// �α��� ��û �� ���������� ������ �޾��� ��� �ݵ�� �� �޼��带 ����ؾ� ��.
	public static void createCookie() {
		
//		if (CookieManager.getInstance().getCookie(URL_DAILYHOTEL_SERVER) != null)
//			Log.e("Common: " + CookieManager.getInstance().getCookie(URL_DAILYHOTEL_SERVER));

		List<Cookie> cookies = ((DefaultHttpClient) sHttpClient)
				.getCookieStore().getCookies();

		
		if (cookies != null) {
			for (int i = 0; i < cookies.size(); i++) {
				Cookie newCookie = cookies.get(i);
				
				
				if (newCookie.getName().equals(KEY_DAILYHOTEL_COOKIE)) {
					
					StringBuilder cookieString = new StringBuilder();
					cookieString.append(newCookie.getName()).append("=")
							.append(newCookie.getValue());
					CookieManager.getInstance().setAcceptCookie(true);
					
					CookieManager.getInstance().setCookie(newCookie.getDomain(),
							cookieString.toString());
					
					CookieSyncManager.getInstance().sync();
				}
			}
		}
	}
	
	// �α׾ƿ� �� �ݵ�� �� �޼��带 ����ؾ� ��.
	public static void destroyCookie() {
		CookieManager.getInstance()
				.removeAllCookie();
		CookieSyncManager.getInstance().sync();
	}

}
