package com.twoheart.dailyhotel.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.kcp.android.payment.standard.KcpApplication;
import kr.co.kcp.util.PackageState;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;

//import com.google.android.gcm.GCMRegistrar;
public class PaymentActivity extends Activity {

	public static final String ACTIVITY_RESULT = "ActivityResult";
	public static final int PROGRESS_STAT_NOT_START = 1;
	public static final int PROGRESS_STAT_IN = 2;
	public static final int PROGRESS_DONE = 3;
	public static String CARD_CD = "";
	public static String QUOTA = "";
	public WebView webView;
	private final Handler handler = new Handler();
	public int m_nStat = PROGRESS_STAT_NOT_START;

	String url;
	byte[] postData;
//	String postData;
	
	private String email;
	private String phone;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_payment);

		Intent intent = getIntent();
		String booking_idx = intent.getStringExtra("booking_idx");
		boolean isBonus = intent.getBooleanExtra("isBonus", false);
		boolean isFullBonus = intent.getBooleanExtra("isFullBonus", false);
		String bonus = intent.getStringExtra("credit");
		
		email = intent.getStringExtra("email");
		phone = intent.getStringExtra("phone");
		name = intent.getStringExtra("name");
		
//		if (!isBonus) {
//			url = REST_URL + PAYMENT + booking_idx;
//			Log.d("url", url);
//		} else {
//			if (isFullBonus) {
//				url = REST_URL + PAYMENT_DISCOUNT + booking_idx;// + "/" +
//																// bonus;
//				Log.d("url", url);
//			} else {
//				url = REST_URL + PAYMENT_DISCOUNT + booking_idx + "/" + bonus;
//				Log.d("url", url);
//			}
//		}
		
//		url = "http://1.234.22.96/goodnight/nulltest.jsp";
		
//		postData = "AppUrl=dailyHOTEL://card_pay";
		
		String[] postDataKey = new String[] { "email", "name", "phone" };
		String[] postDataValue = new String[] { email, name, phone };
//		
		postData = parsePostParameter(postDataKey, postDataValue);
		
//		byte[] postDataKcp = new String("AppUrl=dailyHOTEL://card_pay").getBytes();
//		
//		StringBuilder postDataParameterStr = new StringBuilder();
//		
//		for (int i=0; i<postDataKey.length; i++) {
//			
////			if (postDataParameter.length() != 0) {
////				postDataParameter.append("&");
////			}
//			postDataParameterStr.append("&");
//			postDataParameterStr.append(postDataKey[i]);
//			postDataParameterStr.append("=");
//			postDataParameterStr.append(postDataValue[i]);
//			
//		}
//		
//		byte[] postDataParameter = postDataParameterStr.toString().getBytes();
//		
//		postData = new byte[postDataKcp.length + postDataParameter.length];
//		System.arraycopy(postDataKcp, 0, postData, 0, postDataKcp.length);
//		System.arraycopy(postDataParameter, 0, postData, postDataKcp.length, postDataParameter.length);
		
		loadResource();
	}
	
	private byte[] parsePostParameter(String[] key, String[] value) {
		
		List<byte[]> resultList = new ArrayList<byte[]>();
		HashMap<String, byte[]> postParameters = new HashMap<String, byte[]>();
		
		if (key.length != value.length)
			throw new IllegalArgumentException("The length of the key arguments and " +
					"the length of the value arguments must be same.");
		
		for (int i=0; i<key.length; i++)
			postParameters.put(key[i], EncodingUtils.getBytes(value[i], "BASE64"));
		
		for (int i=0; i<postParameters.size(); i++) {
			
			if (resultList.size() != 0)
				resultList.add("&".getBytes());
			
			resultList.add(key[i].getBytes());
			resultList.add("=".getBytes());
			resultList.add(postParameters.get(key[i]));
		}
		
		int size = 0;
		int[] sizeOfResult = new int[resultList.size()];
		
		for (int i=0; i<resultList.size(); i++) {
			sizeOfResult[i] = resultList.get(i).length;
			
			if (Constants.DEBUG)
				Log.d("sizeOfResult", Integer.toString(sizeOfResult[i]));
		}
		
		for (int i=0; i<sizeOfResult.length; i++) {
			size += sizeOfResult[i];
			
			if (Constants.DEBUG)
				Log.d("size", Integer.toString(size));
		}
		
		if (Constants.DEBUG)
			Log.d("final size", Integer.toString(size));
			
		byte[] result = new byte[size];
		
		int currentSize = 0;
		for (int i=0; i<resultList.size(); i++) {
			
			System.arraycopy(resultList.get(i), 0, result, 
					currentSize, resultList.get(i).length);
			
			currentSize += resultList.get(i).length;
			
		}
		
		return result;
	}
	@JavascriptInterface
	public void loadResource() {
		webView = (WebView) findViewById(R.id.webview);

		webView.getSettings().setSavePassword(false);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.addJavascriptInterface(new KCPPayBridge(), "KCPPayApp");
		// �ϳ�SK ī�� ���ý� User�� ������ �⺻ ������ ������ �������� ���
		webView.addJavascriptInterface(new KCPPayCardInfoBridge(),
				"KCPPayCardInfo");
		webView.addJavascriptInterface(new JavaScriptExtention(), "android");

		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new mWebViewClient());
//		 webView.getSettings().setPluginState(PluginState.ON);
		
		Log.d("test", new String(postData));
		
		webView.postUrl(url, postData);
//		webView.postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));
	}

	private boolean url_scheme_intent(String url) {
		Log.d(KcpApplication.m_strLogTag,
				"[PayDemoActivity] called__test - url=[" + url + "]");

		Uri uri = Uri.parse(url);
		Log.d("urllllllllll", uri.toString());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			return true;
		}

		return true;
	}

	private class mWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(KcpApplication.m_strLogTag,
					"[PayDemoActivity] called__shouldOverrideUrlLoading - url=["
							+ url + "]");

			if (url != null && !url.equals("about:blank")) {
				String url_scheme_nm = url.substring(0, 10);

				if (url_scheme_nm.contains("http://")
						|| url_scheme_nm.contains("https://")) {
					if (url.contains("http://market.android.com")
							|| url.contains("http://m.ahnlab.com/kr/site/download")
							|| url.endsWith(".apk")) {
						return url_scheme_intent(url);
					} else {
						view.loadUrl(url);
						return false;
					}
				}

				else if (url_scheme_nm.contains("mailto:")) {
					return false;
				} else if (url_scheme_nm.contains("tel:")) {
					return false;
				} else {
					return url_scheme_intent(url);
				}
			}

			return true;
		}

		// error ó��
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			webView.loadUrl("about:blank");
			Intent intent = new Intent();
			intent.putExtra(ACTIVITY_RESULT, "NEWORK_ERROR");
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	// �ϳ�SK ī�� ���ý� User�� ������ �⺻ ������ ������ �������� ���
	private class KCPPayCardInfoBridge {
		@JavascriptInterface
		public void getCardInfo(final String card_cd, final String quota) {
			handler.post(new Runnable() {
				public void run() {
					Log.d(KcpApplication.m_strLogTag,
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

		private void alertToNext() {
			AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(
					PaymentActivity.this);
			AlertDialog alertDlg;

			dlgBuilder.setMessage("HANA SK ����� ���� �Ǿ����� �ʽ��ϴ�.\n��ġ �Ͻðڽ��ϱ�?");
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
							// TODO Auto-generated method stub
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

	@JavascriptInterface
	@Override
	protected void onRestart() {
		super.onResume();

		Log.d(KcpApplication.m_strLogTag,
				"[PayDemoActivity] called__onResume + INPROGRESS=[" + m_nStat
						+ "]");

		KcpApplication myApp = (KcpApplication) getApplication();

		// �ϳ� SK ���� ���� ���� �ش� ī�� ������ ������ �������� ���
		if (myApp.m_uriResult != null) {
			if (myApp.m_uriResult.getQueryParameter("realPan") != null
					&& myApp.m_uriResult.getQueryParameter("cavv") != null
					&& myApp.m_uriResult.getQueryParameter("xid") != null
					&& myApp.m_uriResult.getQueryParameter("eci") != null) {
				Log.d(KcpApplication.m_strLogTag,
						"[PayDemoActivity] HANA SK Result = javascript:hanaSK('"
								+ myApp.m_uriResult
										.getQueryParameter("realPan") + "', '"
								+ myApp.m_uriResult.getQueryParameter("cavv")
								+ "', '"
								+ myApp.m_uriResult.getQueryParameter("xid")
								+ "', '"
								+ myApp.m_uriResult.getQueryParameter("eci")
								+ "', '" + CARD_CD + "', '" + QUOTA + "');");

				// �ϳ� SK ���� ���� ���� ������ �ϱ����� ���� �Լ��� ȣ�� (�ֹ��� ������)
				webView.loadUrl("javascript:hanaSK('"
						+ myApp.m_uriResult.getQueryParameter("realPan")
						+ "', '" + myApp.m_uriResult.getQueryParameter("cavv")
						+ "', '" + myApp.m_uriResult.getQueryParameter("xid")
						+ "', '" + myApp.m_uriResult.getQueryParameter("eci")
						+ "', '" + CARD_CD + "', '" + QUOTA + "');");
			}

			if ((myApp.m_uriResult.getQueryParameter("res_cd") == null ? ""
					: myApp.m_uriResult.getQueryParameter("res_cd"))
					.equals("999")) {
				Log.d(KcpApplication.m_strLogTag,
						"[PayDemoActivity] HANA SK Result = cancel");

				m_nStat = 9;
			}

			if ((myApp.m_uriResult.getQueryParameter("isp_res_cd") == null ? ""
					: myApp.m_uriResult.getQueryParameter("isp_res_cd"))
					.equals("0000")) {
				Log.d(KcpApplication.m_strLogTag,
						"[PayDemoActivity] ISP Result = 0000");

				webView.loadUrl("http://pggw.kcp.co.kr/lds/smart_phone_linux_jsp/sample/card/samrt_res.jsp?result=OK&a="
						+ myApp.m_uriResult.getQueryParameter("a"));
				// webView.loadUrl(
				// "https://pggw.kcp.co.kr/app.do?ActionResult=app&approval_key="
				// + strApprovalKey );
			} else {
				Log.d(KcpApplication.m_strLogTag,
						"[PayDemoActivity] ISP Result = cancel");
			}
		}

		if (m_nStat == PROGRESS_STAT_IN) {
			checkFrom();
		}

		myApp.m_uriResult = null;
	}

	@JavascriptInterface
	public void checkFrom() {
		try {
			KcpApplication myApp = (KcpApplication) getApplication();

			if (myApp.m_uriResult != null) {
				m_nStat = PROGRESS_DONE;
				String strResultInfo = myApp.m_uriResult
						.getQueryParameter("approval_key");

				if (strResultInfo == null || strResultInfo.length() <= 4)
					finishActivity("ISP ���� ����");

				String strResCD = strResultInfo.substring(strResultInfo
						.length() - 4);

				Log.d(KcpApplication.m_strLogTag, "[PayDemoActivity] result=["
						+ strResultInfo + "]+" + "res_cd=[" + strResCD + "]");

				if (strResCD.equals("0000") == true) {

					String strApprovalKey = "";

					strApprovalKey = strResultInfo.substring(0,
							strResultInfo.length() - 4);

					Log.d(KcpApplication.m_strLogTag,
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
	protected Dialog onCreateDialog(int id) {
		Log.d(KcpApplication.m_strLogTag,
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
						// TODO Auto-generated method stub
						dialog.dismiss();

						finishActivity("����� ���");
					}
				});
		dlgBuilder.setNegativeButton("�ƴϿ�",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});

		alertDlg = dlgBuilder.create();

		return alertDlg;
	}

	public void finishActivity(String p_strFinishMsg) {
		Intent intent = new Intent();

		if (p_strFinishMsg != null) {
			intent.putExtra(ACTIVITY_RESULT, p_strFinishMsg);
			setResult(RESULT_OK, intent);
		} else {
			setResult(RESULT_CANCELED);
		}

		finish();
	}

	private class JavaScriptExtention {

		JavaScriptExtention() {
		}

		@JavascriptInterface
		public void feed(final String msg) {
			Intent intent = new Intent();

			if (msg.equals("SUCCESS")) {
				intent.putExtra(ACTIVITY_RESULT, "SUCCESS");
				setResult(RESULT_OK, intent);
			} else if (msg.equals("INVALID_SESSION")) {
				intent.putExtra(ACTIVITY_RESULT, "INVALID_SESSION");
				setResult(RESULT_OK, intent);
			} else if (msg.equals("SOLD_OUT")) {
				intent.putExtra(ACTIVITY_RESULT, "SOLD_OUT");
				setResult(RESULT_OK, intent);
			} else if (msg.equals("PAYMENT_COMPLETE")) {
				intent.putExtra(ACTIVITY_RESULT, "PAYMENT_COMPLETE");
				setResult(RESULT_OK, intent);
			} else if (msg.equals("INVALID_DATE")) {
				intent.putExtra(ACTIVITY_RESULT, "INVALID_DATE");
				setResult(RESULT_OK, intent);
			}
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				PaymentActivity.this);
		alertDialog.setTitle("�����˸�").setMessage("������ ����Ͻðڽ��ϱ�?")
				.setCancelable(false)
				.setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("���", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
		AlertDialog alert = alertDialog.create();
		alert.show();
	}
}
