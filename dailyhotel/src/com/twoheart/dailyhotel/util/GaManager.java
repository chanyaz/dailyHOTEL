package com.twoheart.dailyhotel.util;

import android.app.Activity;
import android.app.Application;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Logger.LogLevel;

public class GaManager extends Application{

	// Placeholder property ID.
	private static final String GA_PROPERTY_ID = "UA-43721645-1";
	private static GaManager instance = null;
	
	public static GaManager getInstance() {
		if (instance == null) { 
			instance = new GaManager();
		}
		return instance;
	}

	/**
	 * ���� �Ϸ� �Ͽ����� ���� �ֳη�ƽ�� Ecommerce Tracking �� ���Ͽ� ���� ȣ���Ѵ�.
	 * @param trasId �츮 �ŷ� ������
	 * @param pName ȣ�ڸ�
	 * @param pCategory ȣ�� ī�װ�
	 * @param pPrice ȣ�� �ǸŰ�
	 */
	public void purchaseComplete(String trasId, 
			String pName, String pCategory, Double pPrice) {
		
		// affiliation = 'DailyHOTEL',Tax = 0, Shipping = 0 ,SKU = 1, Quantitiy = 1 ,Currency = 'KRW'
		// price = revenue
		
		
		GoogleAnalytics ga= GoogleAnalytics.getInstance(getApplicationContext());
		Tracker track = ga.getTracker(GA_PROPERTY_ID);
		
		
//		track.send(
//				MapBuilder.createTransaction(
//						trasId,
//		
//						)
//				);
	}
}
