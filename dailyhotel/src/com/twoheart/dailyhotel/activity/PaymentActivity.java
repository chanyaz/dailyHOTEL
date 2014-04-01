package com.twoheart.dailyhotel.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.kcp.android.payment.standard.ResultRcvActivity;
import kr.co.kcp.util.PackageState;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.obj.Pay;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ui.LoadingDialog;

public class PaymentActivity extends Activity implements Constants {

	public static final int PROGRESS_STAT_NOT_START = 1;
	public static final int PROGRESS_STAT_IN = 2;
	public static final int PROGRESS_DONE = 3;
	public static String CARD_CD = "";
	public static String QUOTA = "";
	public int m_nStat = PROGRESS_STAT_NOT_START;
	
	private WebView webView;
	private final Handler handler = new Handler();
	
	private Pay mPay;

	@JavascriptInterface
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_payment);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPay = (Pay) bundle
					.getParcelable(NAME_INTENT_EXTRA_DATA_PAY);
		}
		
		webView = (WebView) findViewById(R.id.webView);
		
	}
	
	@Override
	protected void onPause() {
		CookieSyncManager.getInstance().stopSync();
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
		
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
		
		String[] postParameterKey = new String[] { "email", "name", "phone", "accessToken" };
		String[] postParameterValue = new String[] { mPay.getCustomer().getEmail(), 
				mPay.getCustomer().getName(), mPay.getCustomer().getPhone(), mPay.getCustomer().getAccessToken() };
		
		String url = new StringBuilder(URL_DAILYHOTEL_SERVER)
				.append(URL_WEBAPI_RESERVE_PAYMENT)
				.append(mPay.getHotelDetail().getSaleIdx()).toString();
		
		if (mPay.isSaleCredit()) {
			url = new StringBuilder(URL_DAILYHOTEL_SERVER)
			.append(URL_WEBAPI_RESERVE_PAYMENT_DISCOUNT)
			.append(mPay.getHotelDetail().getSaleIdx()).append("/")
			.append(mPay.getCredit().getBonus()).toString();
			
		}
		
		webView.postUrl(url, parsePostParameter(postParameterKey, postParameterValue));
		
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
	private boolean url_scheme_intent(String url) {
		Log.d(ResultRcvActivity.m_strLogTag,
				"[PayDemoActivity] called__test - url=[" + url + "]");

		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			return true;
		}

		return true;
	}

	private class mWebViewClient extends WebViewClient {
		
		@JavascriptInterface
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(ResultRcvActivity.m_strLogTag,
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

		@JavascriptInterface
		// error ó��
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			webView.loadUrl("about:blank");
			setResult(CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR);
			finish();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			
			LoadingDialog.hideLoading();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
			LoadingDialog.showLoading(PaymentActivity.this);
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
										.getQueryParameter("realPan") + "', '"
								+ ResultRcvActivity.m_uriResult.getQueryParameter("cavv")
								+ "', '"
								+ ResultRcvActivity.m_uriResult.getQueryParameter("xid")
								+ "', '"
								+ ResultRcvActivity.m_uriResult.getQueryParameter("eci")
								+ "', '" + CARD_CD + "', '" + QUOTA + "');");

				// �ϳ� SK ���� ���� ���� ������ �ϱ����� ���� �Լ��� ȣ�� (�ֹ��� ������)
				webView.loadUrl("javascript:hanaSK('"
						+ ResultRcvActivity.m_uriResult.getQueryParameter("realPan")
						+ "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("cavv")
						+ "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("xid")
						+ "', '" + ResultRcvActivity.m_uriResult.getQueryParameter("eci")
						+ "', '" + CARD_CD + "', '" + QUOTA + "');");
			}

			if ((ResultRcvActivity.m_uriResult.getQueryParameter("res_cd") == null ? ""
					: ResultRcvActivity.m_uriResult.getQueryParameter("res_cd"))
					.equals("999")) {
				Log.d(ResultRcvActivity.m_strLogTag,
						"[PayDemoActivity] HANA SK Result = cancel");

				m_nStat = 9;
			}

			if ((ResultRcvActivity.m_uriResult.getQueryParameter("isp_res_cd") == null ? ""
					: ResultRcvActivity.m_uriResult.getQueryParameter("isp_res_cd"))
					.equals("0000")) {
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

				Log.d(ResultRcvActivity.m_strLogTag, "[PayDemoActivity] result=["
						+ strResultInfo + "]+" + "res_cd=[" + strResCD + "]");

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

	@JavascriptInterface
	@Override
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
		
		int resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;

		if (p_strFinishMsg != null) {
			if (p_strFinishMsg.equals("NOT_AVAILABLE")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_NOT_AVAILABLE;
			}
		}
		
		setResult(resultCode);
		finish();
	}

	private class JavaScriptExtention {

		JavaScriptExtention() {
		}

		@JavascriptInterface
		public void feed(final String msg) {
			
			int resultCode = CODE_RESULT_ACTIVITY_PAYMENT_FAIL;

			if (msg.equals("SUCCESS")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SUCCESS;
			} else if (msg.equals("INVALID_SESSION")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_SESSION;
			} else if (msg.equals("SOLD_OUT")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_SOLD_OUT;
			} else if (msg.equals("PAYMENT_COMPLETE")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_COMPLETE;
			} else if (msg.equals("INVALID_DATE")) {
				resultCode = CODE_RESULT_ACTIVITY_PAYMENT_INVALID_DATE;
			}
			
			setResult(resultCode);
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
