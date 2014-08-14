/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * PaymentActivity (����ȭ��)
 * 
 * ���������� �̿��ϴ� KCP ���� ����� �̿��ϴ� ȭ���̴�. WebView�� �̿�
 * �ؼ� KCP ������ �����ϴ� ������ API�� POST ������� ��û�Ѵ�. ��û ��
 * ��û �Ķ���Ϳ� ����� ������ ��´�. �̴� ���� ���̵忡�� Facbook ����
 * �������� Ȯ���ϱ� ���ؼ��̴�.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.activity;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import kr.co.kcp.android.payment.standard.ResultRcvActivity;
import kr.co.kcp.util.PackageState;

import org.apache.http.util.EncodingUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.facebook.widget.WebDialog.FeedDialogBuilder;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Pay;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Log;
import com.twoheart.dailyhotel.util.network.VolleyHttpClient;
import com.twoheart.dailyhotel.util.ui.BaseActivity;

public class PaymentActivity extends BaseActivity implements Constants {

	public static final int PROGRESS_STAT_NOT_START = 1;
	public static final int PROGRESS_STAT_IN = 2;
	public static final int PROGRESS_DONE = 3;
	public static String CARD_CD = "";
	public static String QUOTA = "";
	public int m_nStat = PROGRESS_STAT_NOT_START;

	private WebView webView;
	private final Handler handler = new Handler();

	private Pay mPay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarProgressBar();

		setContentView(R.layout.activity_payment);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) mPay = (Pay) bundle.getParcelable(NAME_INTENT_EXTRA_DATA_PAY);

		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setSavePassword(false);
		webView.getSettings().setAppCacheEnabled(false); // 7.4 ĳ�� ��å ��Ȱ��ȭ.
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.addJavascriptInterface(new KCPPayBridge(), "KCPPayApp");
		// �ϳ�SK ī�� ���ý� User�� ������ �⺻ ������ ������ �������� ���
		webView.addJavascriptInterface(new KCPPayCardInfoBridge(),
				"KCPPayCardInfo");
		webView.addJavascriptInterface(new KCPPayPinInfoBridge(),
				"KCPPayPinInfo"); // ������ ��� �߰�
		webView.addJavascriptInterface(new KCPPayPinReturn(), "KCPPayPinRet"); // ������
		// ���
		// �߰�
		webView.addJavascriptInterface(new JavaScriptExtention(), "android");
		
		webView.addJavascriptInterface(new TeleditBridge(), "TeleditApp");

		webView.setWebChromeClient(new mWebChromeClient());
		webView.setWebViewClient(new mWebViewClient());

		webView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		}); // ��Ŭ�� ���� ����.

		if (mPay == null) {
			showToast(getString(R.string.toast_msg_failed_to_get_payment_info), Toast.LENGTH_SHORT, false);
			finish();
		}

		String userAccessToken = mPay.getCustomer().getAccessToken();

		ArrayList<String> postParameterKey = new ArrayList<String>(Arrays.asList("email", "name", "phone"));
		ArrayList<String> postParameterValue = new ArrayList<String>(Arrays.asList(mPay.getCustomer().getEmail(),
				mPay.getCustomer().getName(),
				mPay.getCustomer().getPhone()));
		
		if (mPay.getPayType() != null) {
			postParameterKey.add("payType");
			postParameterValue.add(mPay.getPayType());
		}
		

		if ((userAccessToken != null)) {
			if ((userAccessToken.equals("")) || !(userAccessToken.equals("null"))) {
				postParameterKey.add("accessToken");
				postParameterValue.add(userAccessToken);
			}
		}

		String url = new StringBuilder(URL_DAILYHOTEL_SERVER)
		.append(URL_WEBAPI_RESERVE_PAYMENT)
		.append(mPay.getHotelDetail().getSaleIdx()).toString();

		if (mPay.isSaleCredit()) {
			url = new StringBuilder(URL_DAILYHOTEL_SERVER)
			.append(URL_WEBAPI_RESERVE_PAYMENT_DISCOUNT)
			.append(mPay.getHotelDetail().getSaleIdx()).append("/")
			.append(mPay.getCredit().getBonus()).toString();
		}
		
		android.util.Log.e("POST_URL",url);
		webView.postUrl(url,
				parsePostParameter(postParameterKey.toArray(new String[postParameterKey.size()]),
						postParameterValue.toArray(new String[postParameterValue.size()])));

	}

	private byte[] parsePostParameter(String[] key, String[] value) {

		List<byte[]> resultList = new ArrayList<byte[]>();
		HashMap<String, byte[]> postParameters = new HashMap<String, byte[]>();

		if (key.length != value.length)
			throw new IllegalArgumentException(
					"The length of the key arguments and "
							+ "the length of the value arguments must be same.");

		for (int i = 0; i < key.length; i++)
			postParameters.put(key[i],
					EncodingUtils.getBytes(value[i], "BASE64"));

		for (int i = 0; i < postParameters.size(); i++) {

			if (resultList.size() != 0)
				resultList.add("&".getBytes());

			resultList.add(key[i].getBytes());
			resultList.add("=".getBytes());
			resultList.add(postParameters.get(key[i]));
		}

		int size = 0;
		int[] sizeOfResult = new int[resultList.size()];

		for (int i = 0; i < resultList.size(); i++) {
			sizeOfResult[i] = resultList.get(i).length;

		}

		for (int i = 0; i < sizeOfResult.length; i++) {
			size += sizeOfResult[i];

		}

		byte[] result = new byte[size];

		int currentSize = 0;
		for (int i = 0; i < resultList.size(); i++) {

			System.arraycopy(resultList.get(i), 0, result, currentSize,
					resultList.get(i).length);

			currentSize += resultList.get(i).length;

		}

		return result;
	}

	@JavascriptInterface
	private boolean url_scheme_intent(WebView view, String url) {
		Log.d(ResultRcvActivity.m_strLogTag,
				"[PayDemoActivity] called__test - url=[" + url + "]");

//		android.util.Log.e("SHOULD_OVERRIDE",url+"");
		
//		if (url.contains("ACCOUNT_DUPLICATE")) 
//		FeedDialogBuilder
//		else resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
//		
//		Intent payData = new Intent();
//		payData.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);
//
//		setResult(resultCode, payData);
//		finish();

		// chrome ���� ��� : 2014.01 �߰�
		if (url.startsWith("intent")) {
			// ILK ��
			if (url.contains("com.lotte.lottesmartpay")) {
				try {
					startActivity(Intent
							.parseUri(url, Intent.URI_INTENT_SCHEME));
				} catch (URISyntaxException e) {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] URISyntaxException=["
									+ e.getMessage() + "]");
					return false;
				} catch (ActivityNotFoundException e) {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] ActivityNotFoundException=["
									+ e.getMessage() + "]");
					return false;
				}
			}
			// ILK ��
			else if (url.contains("com.ahnlab.v3mobileplus")) {
				try {
					view.getContext().startActivity(Intent.parseUri(url, 0));
				} catch (URISyntaxException e) {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] URISyntaxException=["
									+ e.getMessage() + "]");
					return false;
				} catch (ActivityNotFoundException e) {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] ActivityNotFoundException=["
									+ e.getMessage() + "]");
					return false;
				}
			}
			// ���󸮽� ��
			else {
				Intent intent = null;

				try {
					intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				} catch (URISyntaxException ex) {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] URISyntaxException=["
									+ ex.getMessage() + "]");
					return false;
				}

				// �ۼ�ġ üũ�� �մϴ�.
				if (getPackageManager().resolveActivity(intent, 0) == null) {
					String packagename = intent.getPackage();

					if (packagename != null) {
						startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse("market://search?q=pname:"
										+ packagename)));

						return true;
					}
				}

				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent
						.getDataString()));

				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] ActivityNotFoundException=["
									+ e.getMessage() + "]");
					return false;
				}
			}
		}

		// ���� ���
		else {

			if ( url.startsWith( "ispmobile" ) ) { // 7.4 ISP ��� ���� �׽�Ʈ
				if( !new PackageState(this).getPackageDownloadInstallState( PACKAGE_NAME_ISP ) ) { 
					startActivity( new Intent(
							Intent.ACTION_VIEW, 
							Uri.parse(URL_STORE_PAYMENT_ISP)));
					view.goBack();
					return true; 
				} 
			} else if ( url.startsWith( "kftc-bankpay" ) ) { // 7.9 �̴Ͻý� ��� ���� �׽�Ʈ
				if( !new PackageState(this).getPackageDownloadInstallState( PACKAGE_NAME_KFTC ) ) { 
					startActivity( new Intent(
							Intent.ACTION_VIEW, 
							Uri.parse(URL_STORE_PAYMENT_KFTC)));
					view.goBack();
					return true; 
				} 
			} else if (url.startsWith("mpocket.online.ansimclick")) {
				if (!new PackageState(this).getPackageDownloadInstallState(PACKAGE_NAME_MPOCKET)) {
					showToast(getString(R.string.toast_msg_retry_payment_after_install_app), Toast.LENGTH_LONG, false);
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(URL_STORE_PAYMENT_MPOCKET)));

					return true;
				}
			/*  else if ( url.startsWith( "paypin" ) ) { if(
			 * !new PackageState( this ).getPackageDownloadInstallState(
			 * "com.skp.android.paypin" ) ) { if( !url_scheme_intent(
			 * "tstore://PRODUCT_VIEW/0000284061/0" ) ) { url_scheme_intent(
			 * "market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.k"
			 * ); }
			 * 
			 * return true; } }
			 */
			} 
			
			// ���� ��� ����.
			try {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				
				int requestCode = 0;
				if (url.startsWith("kftc-bankpay")) {
					requestCode = CODE_REQUEST_KFTC_BANKPAY;
				} else if (url.startsWith("ispmobile")) {
					requestCode = CODE_REQUEST_ISPMOBILE;
				}

				startActivityForResult(intent, requestCode);
			} catch (ActivityNotFoundException e) {
				return true;
			}
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		String scriptForSkip = "javascript:";
		if (requestCode == CODE_REQUEST_ISPMOBILE) {
			scriptForSkip+="submitIspAuthInfo('RUNSCHEME');"; // ISP Ȯ�� ��ư ��
		} else if (requestCode == CODE_REQUEST_KFTC_BANKPAY) {
			scriptForSkip+="returnUrltoMall();"; //KTFC Ȯ�� ��ư ��
		}
		webView.loadUrl(scriptForSkip);
	}

	private class mWebChromeClient extends WebChromeClient {

		boolean isActionBarProgressBarShowing = false;

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (newProgress != 100)
					setActionBarProgressBar(true);
				else
					setActionBarProgressBar(false);
			}
		}

		void setActionBarProgressBar(boolean show) {
			if (show != isActionBarProgressBarShowing) {
				setSupportProgressBarIndeterminateVisibility(show);
				isActionBarProgressBarShowing = show;	
			}
		}
	}

	private class mWebViewClient extends WebViewClient {

		public mWebViewClient() {
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(ResultRcvActivity.m_strLogTag,
					"[PayDemoActivity] called__shouldOverrideUrlLoading - url=["
							+ url + "]");

			if (url != null && !url.equals("about:blank")) {

				if (url.startsWith("http://") || url.startsWith("https://")) {
					if (url.contains("http://market.android.com")
							|| url.contains("http://m.ahnlab.com/kr/site/download")
							|| url.endsWith(".apk")) {
						return url_scheme_intent(view, url);
					} else {
						view.loadUrl(url);
						return false;
					}
				} else if (url.startsWith("mailto:")) {
					return false;
				} else if (url.startsWith("tel:")) {
					return false;
				} else {
					return url_scheme_intent(view, url);
				}
			}
			// else if isp�Ǵ� , ������ isp ��ġ�������� �̵�.

			return true;
		}

		// error ó��
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			android.util.Log.e("ErrorCode / Description / failingUrl",errorCode+" / "+description + " / " + failingUrl);
			webView.loadUrl("about:blank");
			if(VolleyHttpClient.isAvailableNetwork()) {
				setResult(CODE_RESULT_ACTIVITY_PAYMENT_FAIL);
			} else{
				setResult(CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR);
			}
			finish();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			CookieSyncManager.getInstance().sync();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				setSupportProgressBarIndeterminateVisibility(false);
		}

	}
	/**
	 * �ٳ� ����� ���� ���� �긴��.
	 * @author jangjunho
	 *
	 */
	private class TeleditBridge {
		/**
		 * 
		 * @param val �޴��� ���� �Ϸ� �� �����.
		 */
		@JavascriptInterface
		public void Result(final String val) {
			android.util.Log.e("RES", val);
			setResult(CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE);
			finish();
		}
		
		/**
		 * web���� �ݱ⸦ �� ������ ȣ��.
		 */
		@JavascriptInterface
		public void BestClose() {
			setResult(CODE_RESULT_ACTIVITY_PAYMENT_CANCELED);
			finish();
		}
	}

	private class KCPPayPinReturn {
		@JavascriptInterface
		public String getConfirm() {
			if (ResultRcvActivity.b_type) {
				ResultRcvActivity.b_type = false;

				return "true";
			} else {
				return "false";
			}
		}
	}

	private class KCPPayPinInfoBridge {
		@JavascriptInterface
		public void getPaypinInfo(final String url) {
			handler.post(new Runnable() {
				public void run() {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] KCPPayPinInfoBridge=[getPaypinInfo]");

					PackageState ps = new PackageState(PaymentActivity.this);

					if (!ps.getPackageAllInstallState("com.skp.android.paypin")) {
						paypinConfim();
					} else {
						url_scheme_intent(null, url);
					}
				}
			});
		}

		@JavascriptInterface
		private void paypinConfim() {
			AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(
					PaymentActivity.this);
			AlertDialog alertDlg;

			dlgBuilder.setTitle("Ȯ��");
			dlgBuilder
			.setMessage(getString(R.string.dialog_msg_install_paypin));
			dlgBuilder.setCancelable(false);
			dlgBuilder.setPositiveButton("��ġ",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					if (
							// url_scheme_intent(
							// "https://play.google.com/store/apps/details?id=com.skp.android.paypin&feature=nav_result#?t=W10."
							// );
							// url_scheme_intent(
							// "market://details?id=com.skp.android.paypin&feature=nav_result#?t=W10."
							// );
							!url_scheme_intent(null,
									"tstore://PRODUCT_VIEW/0000284061/0")) {
						url_scheme_intent(
								null,
								"market://details?id=com.skp.android.paypin&feature=search_result#?t=W251bGwsMSwxLDEsImNvbS5za3AuYW5kcm9pZC5wYXlwaW4iXQ.k");
					}
				}
			});
			dlgBuilder.setNegativeButton("���",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					showToast(getString(R.string.toast_msg_cancel_payment), Toast.LENGTH_SHORT, false);

				}
			});

			alertDlg = dlgBuilder.create();
			alertDlg.show();
		}
	}

	// �ϳ�SK ī�� ���ý� User�� ������ �⺻ ������ ������ �������� ���
	private class KCPPayCardInfoBridge {
		@JavascriptInterface
		public void getCardInfo(final String card_cd, final String quota) {
			handler.post(new Runnable() {
				public void run() {
					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] KCPPayCardInfoBridge=["
									+ card_cd + ", " + quota + "]");

					CARD_CD = card_cd;
					QUOTA = quota;

					PackageState ps = new PackageState(PaymentActivity.this);

					if (!ps.getPackageDownloadInstallState("com.skt.at")) {
						alertToNext();
					}
				}
			});
		}

		@JavascriptInterface
		private void alertToNext() {
			AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(
					PaymentActivity.this);
			AlertDialog alertDlg;

			dlgBuilder.setMessage(getString(R.string.dialog_msg_install_hana_sk));
			dlgBuilder.setCancelable(false);
			dlgBuilder.setPositiveButton("��",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					Intent intent = new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("http://cert.hanaskcard.com/Ansim/HanaSKPay.apk"));

					m_nStat = PROGRESS_STAT_IN;

					startActivity(intent);
				}
			});
			dlgBuilder.setNegativeButton("�ƴϿ�",
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			alertDlg = dlgBuilder.create();
			alertDlg.show();
		}
	}

	private class KCPPayBridge {
		@JavascriptInterface
		public void launchMISP(final String arg) {
			handler.post(new Runnable() {
				public void run() {
					boolean isp_app = true;
					String strUrl;
					String argUrl;

					PackageState ps = new PackageState(PaymentActivity.this);

					argUrl = arg;

					if (!arg.equals("Install")) {
						if (!ps.getPackageDownloadInstallState("kvp.jjy.MispAndroid")) {
							argUrl = "Install";
						}
					}

					strUrl = (argUrl.equals("Install") == true) ? "market://details?id=kvp.jjy.MispAndroid320" // "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp"
							: argUrl;

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(strUrl));

					m_nStat = PROGRESS_STAT_IN;
					Log.d("m_nStat", Integer.toString(m_nStat));
					startActivity(intent);
				}
			});
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.d(ResultRcvActivity.m_strLogTag,
				"[PayDemoActivity] called__onResume + INPROGRESS=[" + m_nStat
				+ "]");

		// �ϳ� SK ���� ���� ���� �ش� ī�� ������ ������ �������� ���
		if (ResultRcvActivity.m_uriResult != null) {
			if (ResultRcvActivity.m_uriResult.getQueryParameter("realPan") != null
					&& ResultRcvActivity.m_uriResult.getQueryParameter("cavv") != null
					&& ResultRcvActivity.m_uriResult.getQueryParameter("xid") != null
					&& ResultRcvActivity.m_uriResult.getQueryParameter("eci") != null) {
				Log.d(ResultRcvActivity.m_strLogTag,
						"[PayDemoActivity] HANA SK Result = javascript:hanaSK('"
								+ ResultRcvActivity.m_uriResult
								.getQueryParameter("realPan")
								+ "', '"
								+ ResultRcvActivity.m_uriResult
								.getQueryParameter("cavv")
								+ "', '"
								+ ResultRcvActivity.m_uriResult
								.getQueryParameter("xid")
								+ "', '"
								+ ResultRcvActivity.m_uriResult
								.getQueryParameter("eci") + "', '"
								+ CARD_CD + "', '" + QUOTA + "');");

				// �ϳ� SK ���� ���� ���� ������ �ϱ����� ���� �Լ��� ȣ�� (�ֹ��� ������)
				webView.loadUrl("javascript:hanaSK('"
						+ ResultRcvActivity.m_uriResult
						.getQueryParameter("realPan")
						+ "', '"
						+ ResultRcvActivity.m_uriResult
						.getQueryParameter("cavv")
						+ "', '"
						+ ResultRcvActivity.m_uriResult
						.getQueryParameter("xid")
						+ "', '"
						+ ResultRcvActivity.m_uriResult
						.getQueryParameter("eci") + "', '" + CARD_CD
						+ "', '" + QUOTA + "');");
			}

			if ((ResultRcvActivity.m_uriResult.getQueryParameter("res_cd") == null ? ""
					: ResultRcvActivity.m_uriResult.getQueryParameter("res_cd"))
					.equals("999")) {
				Log.d(ResultRcvActivity.m_strLogTag,
						"[PayDemoActivity] HANA SK Result = cancel");

				m_nStat = 9;
			}

			if ((ResultRcvActivity.m_uriResult.getQueryParameter("isp_res_cd") == null ? ""
					: ResultRcvActivity.m_uriResult
					.getQueryParameter("isp_res_cd")).equals("0000")) {
				Log.d(ResultRcvActivity.m_strLogTag,
						"[PayDemoActivity] ISP Result = 0000");

				webView.loadUrl("http://pggw.kcp.co.kr/lds/smart_phone_linux_jsp/sample/card/samrt_res.jsp?result=OK&a="
						+ ResultRcvActivity.m_uriResult.getQueryParameter("a"));
				// webView.loadUrl(
				// "https://pggw.kcp.co.kr/app.do?ActionResult=app&approval_key="
				// + strApprovalKey );
			} else {
				Log.d(ResultRcvActivity.m_strLogTag,
						"[PayDemoActivity] ISP Result = cancel");
			}
		}

		if (m_nStat == PROGRESS_STAT_IN) {
			checkFrom();
		}

		ResultRcvActivity.m_uriResult = null;
	}

	@JavascriptInterface
	public void checkFrom() {
		try {

			if (ResultRcvActivity.m_uriResult != null) {
				m_nStat = PROGRESS_DONE;
				String strResultInfo = ResultRcvActivity.m_uriResult
						.getQueryParameter("approval_key");

				if (strResultInfo == null || strResultInfo.length() <= 4)
					finishActivity("ISP ���� ����");

				String strResCD = strResultInfo.substring(strResultInfo
						.length() - 4);

				Log.d(ResultRcvActivity.m_strLogTag,
						"[PayDemoActivity] result=[" + strResultInfo + "]+"
								+ "res_cd=[" + strResCD + "]");

				if (strResCD.equals("0000") == true) {

					String strApprovalKey = "";

					strApprovalKey = strResultInfo.substring(0,
							strResultInfo.length() - 4);

					Log.d(ResultRcvActivity.m_strLogTag,
							"[PayDemoActivity] approval_key=[" + strApprovalKey
							+ "]");

					webView.loadUrl("https://pggw.kcp.co.kr/app.do?ActionResult=app&approval_key="
							+ strApprovalKey);

				} else if (strResCD.equals("3001") == true) {
					finishActivity("ISP ���� ����� ���");
				} else {
					finishActivity("ISP ���� ��Ÿ ����");
				}
			}
		} catch (Exception e) {
		} finally {
		}
	}

	@Override
	@JavascriptInterface
	protected Dialog onCreateDialog(int id) {
		Log.d(ResultRcvActivity.m_strLogTag,
				"[PayDemoActivity] called__onCreateDialog - id=[" + id + "]");

		super.onCreateDialog(id);

		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
		AlertDialog alertDlg;

		dlgBuilder.setTitle("���");
		dlgBuilder.setMessage("������ �������Դϴ�.\n����Ͻðڽ��ϱ�?");
		dlgBuilder.setCancelable(false);
		dlgBuilder.setPositiveButton("��",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finishActivity("����� ���");
			}
		});
		dlgBuilder.setNegativeButton("�ƴϿ�",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alertDlg = dlgBuilder.create();

		return alertDlg;
	}

	@JavascriptInterface
	public void finishActivity(String p_strFinishMsg) {

		int resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;

		if (p_strFinishMsg != null) {
			if (p_strFinishMsg.equals("NOT_AVAILABLE")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE;
			} else if (p_strFinishMsg.contains("���")) {
				resultCode = RESULT_CANCELED;
			}
		}
		Intent payData = new Intent();
		payData.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);

		setResult(resultCode, payData);
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_right);

	}

	private class JavaScriptExtention {

		JavaScriptExtention() {
		}
		// ���� �ҽ��� ���� ������ �ּҿ� �� �ڹٽ�ũ��Ʈ ���� �ٿ��� �������Ѵ�.
		@JavascriptInterface
		public void showHTML(String html) {
			for(int i=0;i<html.length()/100;i++) { 
				android.util.Log.e("HTML",html.substring(i*100,(i+1)*100));
			}
		}


		@JavascriptInterface
		public void feed(final String msg) {
			int resultCode = 0;
			android.util.Log.e("FEED",msg);

			if (msg.equals("SUCCESS")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS;
			else if (msg.equals("INVALID_SESSION")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION;
			else if (msg.equals("SOLD_OUT")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT;
			else if (msg.equals("PAYMENT_COMPLETE")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE;
			else if (msg.equals("INVALID_DATE")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE;
			else if (msg.equals("PAYMENT_CANCELED")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_CANCELED;
			else if (msg.equals("ACCOUNT_READY")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY;
			else if (msg.equals("ACCOUNT_TIME_ERROR")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_TIME_ERROR;
			else if (msg.equals("ACCOUNT_DUPLICATE")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_DUPLICATE;
			else if (msg.equals("NOT_AVAILABLE")) resultCode = CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE;
			else resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;
			
			Intent payData = new Intent();
			payData.putExtra(NAME_INTENT_EXTRA_DATA_PAY, mPay);

			setResult(resultCode, payData);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				PaymentActivity.this);
		alertDialog.setTitle("�����˸�").setMessage("������ ����Ͻðڽ��ϱ�?")
		.setCancelable(false)
		.setPositiveButton("��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		})
		.setNegativeButton("�ƴϿ�", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		AlertDialog alert = alertDialog.create();
		alert.show();
	}

}
