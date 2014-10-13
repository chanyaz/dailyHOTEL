package com.twoheart.dailyhotel.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.ui.BaseActivity;
/**
 * 
 * Ǫ�ð� �Դµ� ���� �ܸ��� ���� ������ ��� ����Ʈ�ϴ� ���̾�α��� ��Ƽ��Ƽ
 * @author jangjunho
 *
 */
public class ScreenOnPushDialogActivity extends Activity implements OnClickListener, Constants{

	private TextView tvMsg;
	private ImageView btnClose;
	private TextView tvTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_screen_on_push_dialog);

		String msg = getIntent().getStringExtra(NAME_INTENT_EXTRA_DATA_PUSH_MSG);
		int type = getIntent().getIntExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, -1);
		msg = msg.replace("]", "]\n");

		tvMsg = (TextView)findViewById(R.id.tv_screen_on_push_dialog_msg);
		tvMsg.setText(msg);

		btnClose = (ImageView)findViewById(R.id.iv_screen_on_push_dialog_close);
		btnClose.setOnClickListener(this);

		String title = null;
		switch (type) {
			case PUSH_TYPE_NOTICE:
				title = "�˸�";
				break;
			case PUSH_TYPE_ACCOUNT_COMPLETE:
				title = "�����˸�";
				break;
		}
		
		tvTitle = (TextView)findViewById(R.id.tv_screen_on_push_dialog_title);
		tvTitle.setText(title);
	}

	@Override
	public void onClick(View v) { 
		android.util.Log.e("ACCOUNT_COMPLETE_SCREEN_ON_DIALOG", "closed");
		finish(); 
	}
}
