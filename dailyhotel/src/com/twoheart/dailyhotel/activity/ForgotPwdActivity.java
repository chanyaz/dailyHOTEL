package com.twoheart.dailyhotel.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.network.OnCompleteListener;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
import com.twoheart.dailyhotel.util.ui.LoadingDialog;

public class ForgotPwdActivity extends BaseActivity implements OnClickListener{
	
	private static final String TAG = "ForgotPwdActivity";
	
	private Button btn_forgot;
	private EditText et_forgot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBar("��й�ȣ ã��");
		setContentView(R.layout.activity_forgot_pwd);
		
		loadResource();
		
	}
	
	public void loadResource() {
		et_forgot = (EditText) findViewById(R.id.et_forgot_pwd);
		btn_forgot = (Button) findViewById(R.id.btn_forgot_pwd);
		btn_forgot.setOnClickListener(this);
		et_forgot.setId(EditorInfo.IME_ACTION_DONE);
		et_forgot.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				switch(actionId) {
                case EditorInfo.IME_ACTION_DONE:
                	btn_forgot.performClick();
                    break;
                }
				return false;
			}
		});
	}
	
	// Jason | Fix send email api
	@Override
	public void onClick(View v) {

		if(v.getId() == btn_forgot.getId()) {
			
			String strEmail = et_forgot.getText().toString();
			
			if(strEmail.equals("")) {
				Toast.makeText(this, "�̸��� �ּҸ� �Է����ּ���", Toast.LENGTH_SHORT).show();
				return;
			}
			else if(!isValidEmail(strEmail)) {
				Toast.makeText(this, "�ùٸ� �̸��� ������ �Է����ּ���.", Toast.LENGTH_SHORT).show();
				return;
			}
			
			LoadingDialog.showLoading(this);
			
//			new GeneralHttpTask(forgotListener, getApplicationContext()).execute(REST_URL + "user/sendpw/" + strEmail + "/trim");
		}
	}
	
	public boolean isValidEmail(String inputStr) {
		Pattern p = Pattern.compile("^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$");
		Matcher m = p.matcher(inputStr);
		return m.matches();
	 }
	
	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
		super.onBackPressed();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected OnCompleteListener forgotListener = new OnCompleteListener() {
		
		@Override
		public void onTaskFailed() {
			Log.d(TAG, "onTaskFailed");
			LoadingDialog.hideLoading();
			Toast.makeText(getApplicationContext(), "��Ʈ��ũ ���°� ���� �ʽ��ϴ�.\n��Ʈ��ũ ������ �ٽ� Ȯ�����ּ���.", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onTaskComplete(String result) {
			LoadingDialog.hideLoading();
			AlertDialog.Builder alert = new AlertDialog.Builder(ForgotPwdActivity.this);
			alert.setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();     //�ݱ�
			    }
			});
			
			// Jason | Clear input form
			et_forgot.setText("");
			
			alert.setMessage("�̸����� �߼۵Ǿ����ϴ�.");
			alert.show();
		}
	};
}
