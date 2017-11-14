package com.twoheart.dailyhotel.screen.mydaily.member;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.ScreenUtils;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.Setting;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.information.terms.CollectPersonInformationActivity;
import com.twoheart.dailyhotel.screen.information.terms.TermActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class SignupStep1Activity extends BaseActivity
{
    public static final int PASSWORD_MIN_COUNT = 8;
    private static final int REQUEST_CODE_ACTIVITY = 100;

    SignupStep1Layout mSignupStep1Layout;
    Map<String, String> mSignupParams;
    String mCallByScreen;

    public static Intent newInstance(Context context, String callByScreen)
    {
        Intent intent = new Intent(context, SignupStep1Activity.class);

        if (DailyTextUtils.isTextEmpty(callByScreen) == false)
        {
            intent.putExtra(NAME_INTENT_EXTRA_DATA_CALL_BY_SCREEN, callByScreen);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(NAME_INTENT_EXTRA_DATA_CALL_BY_SCREEN) == true)
        {
            mCallByScreen = intent.getStringExtra(NAME_INTENT_EXTRA_DATA_CALL_BY_SCREEN);
        }

        mSignupStep1Layout = new SignupStep1Layout(this, mOnEventListener);

        setContentView(mSignupStep1Layout.onCreateView(R.layout.activity_signup_step1));

        String signUpText = DailyRemoteConfigPreference.getInstance(this).getRemoteConfigTextSignUpText01();

        if (DailyTextUtils.isTextEmpty(signUpText) == false)
        {
            mSignupStep1Layout.signUpBalloonsTextView(signUpText);
        }

        //        Intent intentPermission = PermissionManagerActivity.newInstance(this, PermissionManagerActivity.PermissionType.READ_PHONE_STATE);
        //        startActivityForResult(intentPermission, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(SignupStep1Activity.this).recordScreen(this, Screen.MENU_REGISTRATION_GETINFO, null);

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockUI();

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case CODE_REQEUST_ACTIVITY_SIGNUP:
            {
                if (resultCode == RESULT_OK)
                {
                    setResult(RESULT_OK);
                    finish();
                } else
                {
                    removeUserInformation();
                }
                break;
            }

            case Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER:
            {
                if (resultCode == RESULT_CANCELED)
                {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            }
        }
    }

    public void removeUserInformation()
    {
        DailyUserPreference.getInstance(SignupStep1Activity.this).clear();
    }

    private SignupStep1Layout.OnEventListener mOnEventListener = new SignupStep1Layout.OnEventListener()
    {
        @Override
        public void onValidation(final String email, final String name, final String password //
            , final String confirmPassword, final String recommender, final String birthday, final boolean isBenefit, int privacyValidMonth)
        {
            if (DailyTextUtils.isTextEmpty(email, name, password, confirmPassword) == true)
            {
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_please_input_required_infos, Toast.LENGTH_SHORT);
                return;
            }

            // email 유효성 체크
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() == false)
            {
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_wrong_email_address, Toast.LENGTH_SHORT);
                return;
            }

            // 패스워드 유효성 체크
            if (password.length() < PASSWORD_MIN_COUNT)
            {
                mSignupStep1Layout.requestPasswordFocus();
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_please_input_password_more_than_8chars, Toast.LENGTH_SHORT);
                return;
            }

            // 패스워드가 동일하게 입력되어있는지 확인
            if (password.equals(confirmPassword) == false)
            {
                mSignupStep1Layout.requestPasswordFocus();
                DailyToast.showToast(SignupStep1Activity.this, R.string.message_please_enter_the_same_password, Toast.LENGTH_SHORT);
                return;
            }

            if (DailyTextUtils.verifyPassword(email, password) == false)
            {
                mSignupStep1Layout.requestPasswordFocus();
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_failed_paswword_verify, Toast.LENGTH_SHORT);
                return;
            }

            // 만 14세 이상
            if (mSignupStep1Layout.isCheckedFourteen() == false)
            {
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_terms_fourteen, Toast.LENGTH_SHORT);
                return;
            }

            // 동의 체크 확인
            if (mSignupStep1Layout.isCheckedTermsOfService() == false)
            {
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_terms_agreement, Toast.LENGTH_SHORT);
                return;
            }

            if (mSignupStep1Layout.isCheckedTermsOfPrivacy() == false)
            {
                DailyToast.showToast(SignupStep1Activity.this, R.string.toast_msg_personal_agreement, Toast.LENGTH_SHORT);
                return;
            }

            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            if (mSignupParams == null)
            {
                mSignupParams = new HashMap<>();
            }

            mSignupParams.clear();
            mSignupParams.put("email", email);
            mSignupParams.put("pw", password);
            mSignupParams.put("name", name);

            if (DailyTextUtils.isTextEmpty(recommender) == false)
            {
                mSignupParams.put("recommender", recommender);

                AnalyticsManager.getInstance(SignupStep1Activity.this).recordEvent(AnalyticsManager.Category.INVITE_FRIEND//
                    , AnalyticsManager.Action.REFERRAL_CODE, AnalyticsManager.Label.TRY, null);
            }

            if (DailyTextUtils.isTextEmpty(birthday) == false)
            {
                mSignupParams.put("birthday", birthday);
            }

            mSignupParams.put("market_type", Setting.getStore().getName());
            mSignupParams.put("isAgreedBenefit", isBenefit == true ? "true" : "false");

            if (privacyValidMonth < 12)
            {
                privacyValidMonth = 12;
            }

            mSignupParams.put("dataRetentionInMonth", Integer.toString(privacyValidMonth));

            DailyMobileAPI.getInstance(SignupStep1Activity.this).requestSignupValidation(mNetworkTag, mSignupParams, mSignupValidationCallback);

            AnalyticsManager.getInstance(SignupStep1Activity.this).recordEvent(AnalyticsManager.Category.NAVIGATION_, //
                AnalyticsManager.Action.NOTIFICATION_SETTING_CLICKED, isBenefit ? AnalyticsManager.Label.SIGNUP_ON : AnalyticsManager.Label.SIGNUP_OFF, null);
        }

        @Override
        public void showTermOfService()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            Intent intent = new Intent(SignupStep1Activity.this, TermActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
        }

        @Override
        public void showTermOfPrivacy()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            startActivityForResult(CollectPersonInformationActivity.newInstance(SignupStep1Activity.this), REQUEST_CODE_ACTIVITY);
        }

        @Override
        public void showBirthdayDatePicker(int year, int month, int day)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = layoutInflater.inflate(R.layout.view_dialog_birthday_layout, null, false);

            final Dialog dialog = new Dialog(SignupStep1Activity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);

            final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);

            if (year < 0 || month < 0 || day < 0)
            {
                year = 2000;
                month = 0;
                day = 1;
            }

            datePicker.init(year, month, day, new DatePicker.OnDateChangedListener()
            {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                {

                }
            });

            datePicker.setMaxDate(DailyCalendar.getInstance().getTimeInMillis());

            // 상단
            TextView titleTextView = (TextView) dialogView.findViewById(R.id.titleTextView);
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText("생일 선택");

            // 버튼
            View buttonLayout = dialogView.findViewById(R.id.buttonLayout);
            View twoButtonLayout = buttonLayout.findViewById(R.id.twoButtonLayout);

            TextView negativeTextView = (TextView) twoButtonLayout.findViewById(R.id.negativeTextView);
            TextView positiveTextView = (TextView) twoButtonLayout.findViewById(R.id.positiveTextView);

            negativeTextView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (dialog != null && dialog.isShowing())
                    {
                        dialog.dismiss();
                    }
                }
            });

            positiveTextView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (dialog != null && dialog.isShowing())
                    {
                        dialog.dismiss();
                    }

                    mSignupStep1Layout.setBirthdayText(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                }
            });

            dialog.setCancelable(true);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialog)
                {
                }
            });

            // 생일 화면 부터는 키패드를 나오지 않게 한다.
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            try
            {
                dialog.setContentView(dialogView);

                WindowManager.LayoutParams layoutParams = ScreenUtils.getDialogWidthLayoutParams(SignupStep1Activity.this, dialog);

                dialog.show();

                dialog.getWindow().setAttributes(layoutParams);
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }

            AnalyticsManager.getInstance(SignupStep1Activity.this).recordScreen(SignupStep1Activity.this, AnalyticsManager.Screen.MENU_SET_MY_BIRTHDAY, null);
        }

        @Override
        public void finish()
        {
            SignupStep1Activity.this.finish();
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    retrofit2.Callback mSignupValidationCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                JSONObject responseJSONObject = response.body();

                try
                {
                    int msgCode = responseJSONObject.getInt("msgCode");
                    String message = responseJSONObject.getString("msg");

                    if (msgCode == 100)
                    {
                        JSONObject dataJSONObject = responseJSONObject.getJSONObject("data");
                        String signupKey = dataJSONObject.getString("signup_key");
                        String serverDate = dataJSONObject.getString("serverDate");

                        Intent intent = SignupStep2Activity.newInstance(SignupStep1Activity.this, //
                            signupKey, mSignupParams.get("email"), mSignupParams.get("pw"), serverDate, //
                            mSignupParams.get("recommender"), mCallByScreen);
                        startActivityForResult(intent, CODE_REQEUST_ACTIVITY_SIGNUP);
                    } else
                    {
                        SignupStep1Activity.this.onErrorPopupMessage(msgCode, message, null);
                    }
                } catch (Exception e)
                {
                    SignupStep1Activity.this.onError(e);
                }
            } else
            {
                SignupStep1Activity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            SignupStep1Activity.this.onError(t);
        }
    };
}
