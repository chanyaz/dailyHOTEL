/**
 * Copyright (c) 2014 Daily Co., Ltd. All rights reserved.
 *
 * PaymentActivity (결제화면)
 * 
 * 웹서버에서 이용하는 KCP 결제 모듈을 이용하는 화면이다. WebView를 이용
 * 해서 KCP 결제를 진행하는 웹서버 API에 POST 방식으로 요청한다. 요청 시
 * 요청 파라미터에 사용자 정보를 담는다. 이는 서버 사이드에서 Facbook 계정
 * 임인지를 확인하기 위해서이다.
 *
 * @since 2014-02-24
 * @version 1
 * @author Mike Han(mike@dailyhotel.co.kr)
 */
package com.twoheart.dailyhotel.activity;

import java.util.ArrayList;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.Pay;
import com.twoheart.dailyhotel.util.Constants;
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
		setActionBarHide();
		setContentView(R.layout.activity_payment);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPay = (Pay) bundle
					.getParcelable(NAME_INTENT_EXTRA_DATA_PAY);
		}
		
		CookieManager.getInstance().setAcceptCookie(true);
		CookieSyncManager.getInstance().sync();
		
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setSavePassword(false);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.addJavascriptInterface(new KCPPayBridge(), "KCPPayApp");
		// 하나SK 카드 선택시 User가 선택한 기본 정보를 가지고 오기위해 사용
		webView.addJavascriptInterface(new KCPPayCardInfoBridge(),
				"KCPPayCardInfo");
		webView.addJavascriptInterface(new JavaScriptExtention(), "android");
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new mWebViewClient());
		
		if (mPay == null) {
			Toast.makeText(this, "결제 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
		}
		
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
			
		}
		
		for (int i=0; i<sizeOfResult.length; i++) {
			size += sizeOfResult[i];
			
		}
		
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
	
//	private class mWebChromeClient extends WebChromeClient {
//
//		@Override
//		public boolean onJsAlert(WebView view, String url, String message,
//				JsResult result) {
//			LoadingDialog.hideLoading();
//			return super.onJsAlert(view, url, message, result);
//		}
//
//		@Override
//		public boolean onJsBeforeUnload(WebView view, String url,
//				String message, JsResult result) {
//			LoadingDialog.hideLoading();
//			return super.onJsBeforeUnload(view, url, message, result);
//		}
//
//		@Override
//		public boolean onJsConfirm(WebView view, String url, String message,
//				JsResult result) {
//			LoadingDialog.hideLoading();
//			return super.onJsConfirm(view, url, message, result);
//		}
//
//		@Override
//		public boolean onJsPrompt(WebView view, String url, String message,
//				String defaultValue, JsPromptResult result) {
//			LoadingDialog.hideLoading();
//			return super.onJsPrompt(view, url, message, defaultValue, result);
//		}
//
//		@Override
//		public void onProgressChanged(WebView view, int newProgress) {
//			super.onProgressChanged(view, newProgress);
//			
//			if (newProgress < 100)
//				LoadingDialog.showLoading(PaymentActivity.this);
//			else
//				LoadingDialog.hideLoading();
//				
//		}
//		
//		
//	}

	private class mWebViewClient extends WebViewClient {
		
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

		// error 처리
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			webView.loadUrl("about:blank");
			setResult(CODE_RESULT_ACTIVITY_PAYMENT_NETWORK_ERROR);
			finish();
		}
		
//		@Override
//		public void onPageStarted(WebView view, String url, Bitmap favicon) {
//			super.onPageStarted(view, url, favicon);
//			LoadingDialog.showLoading(PaymentActivity.this);
//		}
//
//
//		@Override
//		public void onPageFinished(WebView view, String url) {
//			super.onPageFinished(view, url);
//			LoadingDialog.hideLoading();
//		}

		
	}

	// 하나SK 카드 선택시 User가 선택한 기본 정보를 가지고 오기위해 사용
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

			dlgBuilder.setMessage("HANA SK 모듈이 설이 되어있지 않습니다.\n설치 하시겠습니까?");
			dlgBuilder.setCancelable(false);
			dlgBuilder.setPositiveButton("예",
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
			dlgBuilder.setNegativeButton("아니오",
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

		// 하나 SK 모듈로 결제 이후 해당 카드 정보를 가지고 오기위해 사용
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

				// 하나 SK 모듈로 인증 이후 승인을 하기위해 결제 함수를 호출 (주문자 페이지)
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
					finishActivity("ISP 결제 오류");

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
					finishActivity("ISP 결제 사용자 취소");
				} else {
					finishActivity("ISP 결제 기타 오류");
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

		dlgBuilder.setTitle("취소");
		dlgBuilder.setMessage("결제가 진행중입니다.\n취소하시겠습니까?");
		dlgBuilder.setCancelable(false);
		dlgBuilder.setPositiveButton("예",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finishActivity("사용자 취소");
					}
				});
		dlgBuilder.setNegativeButton("아니오",
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
			} else if (p_strFinishMsg.contains("취소")) {
				resultCode = RESULT_CANCELED;
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
		alertDialog.setTitle("결제알림").setMessage("결제를 취소하시겠습니까?")
				.setCancelable(false)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
		AlertDialog alert = alertDialog.create();
		alert.show();
	}
	
}
