package com.twoheart.dailyhotel.util;

import android.app.Application;
import android.content.Context;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class GaManager extends Application implements Constants{

	// Placeholder property ID.


	private static GaManager instance = null;

	private Tracker tracker;

	private GaManager(Context con) {
		GoogleAnalytics ga= GoogleAnalytics.getInstance(con);
		tracker = ga.getTracker(GA_PROPERTY_ID);
	}

	public static GaManager getInstance(Context con) {
		if (instance == null) { 
			instance = new GaManager(con);
		}
		return instance;
	}

	/**
	 * ���� �Ϸ� �Ͽ����� ���� �ֳη�ƽ�� Ecommerce Tracking �� ���Ͽ� ���� ȣ���Ѵ�.
	 * ���� �츮 ���� ������ �ڵ����� �����Ͽ� �˱�����.
	 * @param trasId saleIdx
	 * @param pName ȣ�ڸ�
	 * @param pCategory ȣ�� ī�װ�
	 * @param pPrice ȣ�� �ǸŰ�(�������� ��� �ϴ� ��� �������� ��� �����ϴ� �ݾ�)
	 */

	public void purchaseComplete(String trasId, 
			String pName, String pCategory, Double pPrice) {

		tracker.send(
				MapBuilder.createTransaction(
						trasId,
						GA_COMMERCE_DEFAULT_AFFILIATION,
						pPrice,
						GA_COMMERCE_DEFAULT_TAX,
						GA_COMMERCE_DEFAULT_SHIPPING,
						GA_COMMERCE_DEFAULT_CURRENCY_CODE
						).build()
				);

		tracker.send(
				MapBuilder.createItem(
						trasId,
						pName,
						GA_COMMERCE_DEFAULT_SKU,
						pCategory,
						pPrice,
						GA_COMMERCE_DEFAULT_QUANTITY,
						GA_COMMERCE_DEFAULT_CURRENCY_CODE
						).build()
				);
		tracker.send(MapBuilder.
				createEvent(
						"Purchase", 
						"PurchaseComplete", 
						"Purchase", 
						1L).build());
	}

	/**
	 * ȸ������ ���ڸ� ���� �ֳη�ƽ������ Ȯ���ϱ����Ͽ�, ȸ�� ���Կ� �����Ͽ����� ���� ȣ�� �Ѵ�.
	 */
	public void signupComplete() {
		tracker.send(MapBuilder.
				createEvent(
						"Signup", 
						"SignupComplete", 
						"SignupComplete", 
						1L).build());
	}
}
