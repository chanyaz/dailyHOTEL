package com.twoheart.dailyhotel.screen.mydaily.member;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.util.StringFilter;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyEditText;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class EditProfilePasswordActivity extends BaseActivity implements OnClickListener, View.OnFocusChangeListener
{
    private View mPasswordView, mConfirmPasswordView;
    DailyEditText mPasswordEditText, mConfirmPasswordEditText;
    View mConfirmView;

    public static Intent newInstance(Context context)
    {
        Intent intent = new Intent(context, EditProfilePasswordActivity.class);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.slide_in_right, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_password);

        initToolbar();
        initLayout();
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_edit_password), new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void initLayout()
    {
        mPasswordView = findViewById(R.id.passwordView);
        mPasswordEditText = (DailyEditText) findViewById(R.id.passwordEditText);
        mPasswordEditText.setDeleteButtonVisible(null);
        mPasswordEditText.setOnFocusChangeListener(this);

        StringFilter stringFilter1 = new StringFilter(this);
        InputFilter[] allowPassword1 = new InputFilter[2];
        allowPassword1[0] = stringFilter1.allowPassword;
        allowPassword1[1] = new InputFilter.LengthFilter(getResources().getInteger(R.integer.max_password) + 1);

        mPasswordEditText.setFilters(allowPassword1);

        mPasswordEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (mPasswordEditText.length() > 0 && mConfirmPasswordEditText.length() > 0)
                {
                    mConfirmView.setEnabled(true);
                } else
                {
                    mConfirmView.setEnabled(false);
                }

                if (s.length() > getResources().getInteger(R.integer.max_password))
                {
                    s.delete(s.length() - 1, s.length());

                    DailyToast.showToast(EditProfilePasswordActivity.this, getString(R.string.toast_msg_wrong_max_password_length), Toast.LENGTH_SHORT);
                }
            }
        });

        mConfirmPasswordView = findViewById(R.id.confirmPasswordView);
        mConfirmPasswordEditText = (DailyEditText) findViewById(R.id.confirmPasswordEditText);
        mConfirmPasswordEditText.setDeleteButtonVisible(null);
        mConfirmPasswordEditText.setOnFocusChangeListener(this);

        StringFilter stringFilter2 = new StringFilter(this);
        InputFilter[] allowPassword2 = new InputFilter[2];
        allowPassword2[0] = stringFilter2.allowPassword;
        allowPassword2[1] = new InputFilter.LengthFilter(getResources().getInteger(R.integer.max_password) + 1);

        mConfirmPasswordEditText.setFilters(allowPassword2);

        mConfirmPasswordEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (mPasswordEditText.length() > 0 && mConfirmPasswordEditText.length() > 0)
                {
                    mConfirmView.setEnabled(true);
                } else
                {
                    mConfirmView.setEnabled(false);
                }

                if (s.length() > getResources().getInteger(R.integer.max_password))
                {
                    s.delete(s.length() - 1, s.length());

                    DailyToast.showToast(EditProfilePasswordActivity.this, getString(R.string.toast_msg_wrong_max_password_length), Toast.LENGTH_SHORT);
                }
            }
        });

        mConfirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                switch (actionId)
                {
                    case EditorInfo.IME_ACTION_DONE:
                        mConfirmView.performClick();
                        return true;

                    default:
                        return false;
                }
            }
        });

        mConfirmView = findViewById(R.id.confirmView);
        mConfirmView.setEnabled(false);
        mConfirmView.setOnClickListener(this);
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(EditProfilePasswordActivity.this).recordScreen(this, AnalyticsManager.Screen.MENU_SETPROFILE_PASSWORD, null);

        super.onStart();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.confirmView:
                if (v.isEnabled() == false)
                {
                    return;
                }

                String password = mPasswordEditText.getText().toString();
                String confirmPassword = mConfirmPasswordEditText.getText().toString();

                // 패스워드 유효성 체크
                if (Util.isTextEmpty(password) == true || password.length() < SignupStep1Activity.PASSWORD_MIN_COUNT)
                {
                    DailyToast.showToast(EditProfilePasswordActivity.this, R.string.toast_msg_please_input_password_more_than_8chars, Toast.LENGTH_SHORT);
                    return;
                }

                if (Util.isTextEmpty(password, confirmPassword) == true)
                {
                    DailyToast.showToast(EditProfilePasswordActivity.this, R.string.toast_msg_please_input_required_infos, Toast.LENGTH_SHORT);
                    return;
                }

                // 패스워드가 동일하게 입력되어있는지 확인
                if (password.equals(confirmPassword) == false)
                {
                    DailyToast.showToast(EditProfilePasswordActivity.this, R.string.message_please_enter_the_same_password, Toast.LENGTH_SHORT);
                    return;
                }

                // 패스워드 검증
                if (Util.verifyPassword(null, password) == false)
                {
                    DailyToast.showToast(EditProfilePasswordActivity.this, R.string.toast_msg_failed_paswword_verify, Toast.LENGTH_SHORT);
                    return;
                }

                Map<String, String> params = Collections.singletonMap("pw", password);
                DailyMobileAPI.getInstance(this).requestUserInformationUpdate(mNetworkTag, params, mDailyUserUpdateCallback);
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch (v.getId())
        {
            case R.id.passwordEditText:
                setFocusLabelView(mPasswordView, mPasswordEditText, hasFocus);
                break;

            case R.id.confirmPasswordEditText:
                setFocusLabelView(mConfirmPasswordView, mConfirmPasswordEditText, hasFocus);
                break;
        }
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private void setFocusLabelView(View labelView, EditText editText, boolean hasFocus)
    {
        if (hasFocus == true)
        {
            labelView.setActivated(false);
            labelView.setSelected(true);
        } else
        {
            if (editText.length() > 0)
            {
                labelView.setActivated(true);
            }

            labelView.setSelected(false);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private retrofit2.Callback mDailyUserUpdateCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msgCode");

                    if (msgCode == 100)
                    {
                        showSimpleDialog(null, getString(R.string.toast_msg_profile_success_edit_password), getString(R.string.dialog_btn_text_confirm), new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                finish();
                            }
                        }, new DialogInterface.OnCancelListener()
                        {
                            @Override
                            public void onCancel(DialogInterface dialog)
                            {
                                finish();
                            }
                        });

                        setResult(RESULT_OK);
                    } else
                    {
                        String message = responseJSONObject.getString("msg");
                        showSimpleDialog(null, message, getString(R.string.dialog_btn_text_confirm), new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                mPasswordEditText.setText(null);
                                mConfirmPasswordEditText.setText(null);
                            }
                        }, new DialogInterface.OnCancelListener()
                        {
                            @Override
                            public void onCancel(DialogInterface dialog)
                            {
                                mPasswordEditText.setText(null);
                                mConfirmPasswordEditText.setText(null);
                            }
                        });
                    }
                } catch (Exception e)
                {
                    onError(e);
                } finally
                {
                    unLockUI();
                }
            } else
            {
                EditProfilePasswordActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            EditProfilePasswordActivity.this.onError(t);
        }
    };
}
