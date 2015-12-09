package com.twoheart.dailyhotel.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.twoheart.dailyhotel.LauncherActivity;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

/**
 * 푸시가 왔는데 현재 단말이 켜진 상태일 경우 인텐트하는 다이얼로그형 액티비티
 *
 * @author jangjunho
 */
public class ScreenOnPushDialogActivity extends Activity implements OnClickListener, Constants
{
    private String mLink;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_screen_on_push_dialog);

        String msg = getIntent().getStringExtra(NAME_INTENT_EXTRA_DATA_PUSH_MSG);
        int type = getIntent().getIntExtra(NAME_INTENT_EXTRA_DATA_PUSH_TYPE, -1);
        mLink = getIntent().getStringExtra(NAME_INTENT_EXTRA_DATA_PUSH_LINK);

        TextView messageTextView = (TextView) findViewById(R.id.messageTextView);

        String title = null;
        switch (type)
        {
            case PUSH_TYPE_NOTICE:
            {
                msg = msg.replaceAll("\\\\n", "\n");

                // 공지 푸시
                messageTextView.setText(msg);
                title = getString(R.string.dialog_notice2);
                break;
            }

            case PUSH_TYPE_ACCOUNT_COMPLETE:
            {
                // 계좌이체 결제 완료 푸시
                String result = msg;

                if (result.contains("]"))
                {
                    // [호텔이름 [조식 포함]] 예약되었습니다. 과 같은 경우 마지막 ] 다음에서 개행하여 보기 좋도록 표시
                    int index = msg.lastIndexOf(']');
                    StringBuffer sb = new StringBuffer(msg);
                    result = sb.replace(index, index + 1, "]\n").toString();
                }

                messageTextView.setText(result);

                title = getString(R.string.dialog_title_payment);
                break;
            }
        }

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(title);

        View positiveTextView = findViewById(R.id.positiveTextView);
        positiveTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (Util.isTextEmpty(mLink) == false)
        {
            Intent intent = new Intent(this, LauncherActivity.class);
            intent.setData(Uri.parse(mLink));
            startActivity(intent);
        }

        finish();
    }
}
