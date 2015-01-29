package com.twoheart.dailyhotel.util;

import com.google.analytics.tracking.android.Fields;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.content.Context;
import android.widget.Toast;

public class RenewalGaManager   {
	private static RenewalGaManager instance = null;
	private static int count = 0;
	private Tracker tracker;

	private RenewalGaManager(Context con) {
		GoogleAnalytics ga= GoogleAnalytics.getInstance(con);
		tracker = ga.getTracker(Constants.GA_PROPERTY_ID);
		Toast.makeText(con, Constants.GA_PROPERTY_ID+", count is " + ++count, Toast.LENGTH_LONG).show();
	}

	public static RenewalGaManager getInstance(Context con) {
		if (instance == null) { 
			instance = new RenewalGaManager(con);
		}
		return instance;
	}
	
	public static RenewalGaManager getInstance(Context con, String screen_name) {
        instance = new RenewalGaManager(con);
        instance.getTracker().set(Fields.SCREEN_NAME, screen_name);

        return instance;
	}
	
	private Tracker getTracker() {
		return this.tracker;
	}
	
	public void recordScreen(String screenName, String page) {
		tracker.send(MapBuilder
			    .createAppView()
			    .set(Fields.SCREEN_NAME, screenName)
			    .set(Fields.PAGE, page)
			    .build()
			);
		
	}
	
	public void recordEvent(String category, String action, String label, Long value) {
		tracker.send(MapBuilder.
                createEvent(
                		category, 
                		action, 
                		label, 
                		value).build());
	}
	
	
	/**
	 * ���� �Ϸ� �Ͽ����� ���� �ֳη�ƽ�� Ecommerce Tracking �� ���Ͽ� ���� ȣ���Ѵ�.
	 * ���� �츮 ���� ������ �ڵ����� �����Ͽ� �˱�����.
	 * @param trasId userId+YYMMDDhhmmss
	 * @param pName ȣ�ڸ�
	 * @param pCategory ȣ�� ī�װ�
	 * @param pPrice ȣ�� �ǸŰ�(�������� ��� �ϴ� ��� �������� ��� �����ϴ� �ݾ�)
	 */

	public void purchaseComplete(String trasId, 
			String pName, String pCategory, Double pPrice) {

		tracker.send(
				MapBuilder.createTransaction(
						trasId,
						"DailyHOTEL",
						pPrice,
						0d,
						0d,
						"KRW"
						).build()
				);

		tracker.send(
				MapBuilder.createItem(
						trasId,
						pName,
						"1",
						pCategory,
						pPrice,
						1L,
						"KRW"
						).build()
				);
		
		tracker.send(MapBuilder.
				createEvent(
						"Purchase", 
						"PurchaseComplete", 
						"PurchaseComplete", 
						1L).build());
	}
}
