package com.twoheart.dailyhotel.screen.mydaily.member;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daily.base.util.ScreenUtils;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.PhoneNumberKoreaFormattingTextWatcher;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyToast;

import java.util.Locale;

public class InputMobileNumberDialogActivity extends BaseActivity
{
    public static final String INTENT_EXTRA_MOBILE_NUMBER = "mobileNumber";

    private static final int REQUEST_CODE_COUNTRYCODE_LIST_ACTIVITY = 1;

    private Dialog mMobileDialog;
    String mCountryCode;
    String mMobileNumber;

    public static Intent newInstance(Context context, String mobileNumber)
    {
        Intent intent = new Intent(context, InputMobileNumberDialogActivity.class);
        intent.putExtra(INTENT_EXTRA_MOBILE_NUMBER, mobileNumber);

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null)
        {
            String mobileNumber = intent.getStringExtra(INTENT_EXTRA_MOBILE_NUMBER);

            initLayout(mobileNumber);
        } else
        {
            finish();
        }
    }

    private void initLayout(String mobileNumber)
    {
        setMobileNumber(mobileNumber);

        showInputMobileDialog(mCountryCode, mMobileNumber);
    }

    @Override
    protected void onDestroy()
    {
        if (mMobileDialog != null)
        {
            if (mMobileDialog.isShowing() == true)
            {
                mMobileDialog.dismiss();
            }

            mMobileDialog = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        releaseUiComponent();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_COUNTRYCODE_LIST_ACTIVITY)
        {
            if (resultCode == RESULT_OK && data != null)
            {
                mCountryCode = data.getStringExtra(CountryCodeListActivity.INTENT_EXTRA_COUNTRY_CODE);
            }

            showInputMobileDialog(mCountryCode, mMobileNumber);
        }
    }

    private void setMobileNumber(String number)
    {
        String[] countryMobile = null;

        if (com.daily.base.util.TextUtils.isTextEmpty(number) == false)
        {
            number = number.replace("-", "");

            countryMobile = Util.getValidatePhoneNumber(number);
        }

        if (countryMobile == null)
        {
            mCountryCode = Util.DEFAULT_COUNTRY_CODE;
            mMobileNumber = null;
        } else
        {
            mCountryCode = countryMobile[0];

            if (countryMobile.length > 1)
            {
                mMobileNumber = countryMobile[1];
            } else
            {
                mMobileNumber = null;
            }
        }
    }

    private void showInputMobileDialog(final String countryCode, String mobileNumber)
    {
        if (isFinishing() == true)
        {
            return;
        }

        if (mMobileDialog != null && mMobileDialog.isShowing() == true)
        {
            mMobileDialog.setOnDismissListener(null);
            mMobileDialog.cancel();
        }

        mMobileDialog = null;
        mMobileDialog = new Dialog(this);

        mMobileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMobileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mMobileDialog.setCanceledOnTouchOutside(false);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_mobiledialog_layout, null, false);

        final EditText mobileEditText = (EditText) view.findViewById(R.id.mobileTextView);

        if (Util.DEFAULT_COUNTRY_CODE.equalsIgnoreCase(countryCode) == true)
        {
            mobileEditText.addTextChangedListener(new PhoneNumberKoreaFormattingTextWatcher(this));
        } else
        {
            mobileEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }

        if (com.daily.base.util.TextUtils.isTextEmpty(mobileNumber) == false)
        {
            mobileNumber = mobileNumber.replaceAll("\\(|\\)|-|\\s", "");
        }

        mobileEditText.setText(mobileNumber);
        mobileEditText.setSelection(mobileEditText.length());

        final TextView countryTextView = (TextView) view.findViewById(R.id.countryTextView);
        countryTextView.setText(countryCode.substring(0, countryCode.indexOf('\n')));
        countryTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (lockUiComponentAndIsLockUiComponent() == true)
                {
                    return;
                }

                mMobileNumber = mobileEditText.getText().toString();

                Intent intent = CountryCodeListActivity.newInstance(InputMobileNumberDialogActivity.this, countryCode);
                startActivityForResult(intent, REQUEST_CODE_COUNTRYCODE_LIST_ACTIVITY);
            }
        });

        final View button = view.findViewById(R.id.buttonLayout);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 전화번호를 읽어서 넣기
                String countryCode = mCountryCode.substring(mCountryCode.indexOf('\n') + 1);
                mMobileNumber = mobileEditText.getText().toString();

                String phoneNumber = String.format(Locale.KOREA, "%s %s", countryCode, mMobileNumber);

                if (Util.isValidatePhoneNumber(phoneNumber) == true)
                {
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_EXTRA_MOBILE_NUMBER, String.format(Locale.KOREA, "%s %s", countryCode, mMobileNumber));

                    setResult(RESULT_OK, intent);
                    finish();
                } else
                {
                    DailyToast.showToast(InputMobileNumberDialogActivity.this, R.string.toast_msg_input_error_phonenumber, Toast.LENGTH_SHORT);
                }
            }
        });

        mobileEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    button.performClick();
                }

                return false;
            }
        });

        mMobileDialog.setContentView(view);
        mMobileDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                finish();
            }
        });

        try
        {
            WindowManager.LayoutParams layoutParams = ScreenUtils.getDialogWidthLayoutParams(this, mMobileDialog);

            mMobileDialog.show();

            mMobileDialog.getWindow().setAttributes(layoutParams);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        mobileEditText.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(mobileEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 500);
    }
}
