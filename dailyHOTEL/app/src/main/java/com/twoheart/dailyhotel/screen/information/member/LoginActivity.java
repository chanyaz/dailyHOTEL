package com.twoheart.dailyhotel.screen.information.member;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.information.ForgotPasswordActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Crypto;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Action;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Label;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager.Screen;
import com.twoheart.dailyhotel.widget.DailyAutoCompleteEditText;
import com.twoheart.dailyhotel.widget.DailyEditText;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;
import com.twoheart.dailyhotel.widget.FontManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements Constants, OnClickListener, View.OnFocusChangeListener
{
    public CallbackManager mCallbackManager;
    private ScrollView mScrollView;
    private DailyAutoCompleteEditText mEmailEditText;
    private DailyEditText mPasswordEditText;
    private TextView mLoginView, mFindPasswordView;
    private View mEmailView, mPasswordView, mSnsLoginLayout;
    private com.facebook.login.widget.LoginButton mFacebookLoginView;

    private Map<String, String> mStoreParams;

    // 카카오톡
    private com.kakao.usermgmt.LoginButton mKakaoLoginView;
    private SessionCallback mKakaoSessionCallback;
    private boolean mIsSocialSignUp;
    private boolean mCertifyingTermination;

    //
    private boolean mIsKeypadOpend;
    private boolean mScrollToEmailView;

    private String mCallByScreen;

    public static Intent newInstance(Context context)
    {
        return newInstance(context, null);
    }

    public static Intent newInstance(Context context, String callByScreen)
    {
        Intent intent = new Intent(context, LoginActivity.class);

        if (Util.isTextEmpty(callByScreen) == false)
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

        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(NAME_INTENT_EXTRA_DATA_CALL_BY_SCREEN) == true)
        {
            mCallByScreen = getIntent().getStringExtra(NAME_INTENT_EXTRA_DATA_CALL_BY_SCREEN);
        }

        initToolbar();
        initTopLayout();
        initEditTextsLayout();
        initButtonsLayout();

        //        Intent intent = PermissionManagerActivity.newInstance(this, PermissionManagerActivity.PermissionType.READ_PHONE_STATE);
        //        startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);
    }

    private void initToolbar()
    {
        View toolbar = findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(this, toolbar);
        dailyToolbarLayout.initToolbar(getString(R.string.actionbar_title_login_activity), new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void initTopLayout()
    {
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        EdgeEffectColor.setEdgeGlowColor(mScrollView, getResources().getColor(R.color.default_over_scroll_edge));

        if (Util.isOverAPI16() == true)
        {
            mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        } else
        {
            mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
        }

        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

        TextView signUpView = (TextView) findViewById(R.id.signUpView);
        signUpView.setOnClickListener(this);

        String signupMessage = DailyPreference.getInstance(this).getRemoteConfigTextLoginText01();

        if (Util.isTextEmpty(signupMessage) == false)
        {
            TextView signUpTextView = (TextView) findViewById(R.id.signUpTextView);
            signUpTextView.setText(signupMessage);
        }
    }

    private void initEditTextsLayout()
    {
        mEmailView = findViewById(R.id.emailView);
        mEmailEditText = (DailyAutoCompleteEditText) findViewById(R.id.emailEditText);
        mEmailEditText.setDeleteButtonVisible(true, null);
        mEmailEditText.setOnFocusChangeListener(this);
        mEmailEditText.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_UP:
                        if (mIsKeypadOpend == false)
                        {
                            mScrollToEmailView = true;
                        }
                        break;
                }

                return false;
            }
        });

        EmailCompleteAdapter emailCompleteAdapter = new EmailCompleteAdapter(this, Arrays.asList(getResources().getStringArray(R.array.company_email_postfix_array)));
        mEmailEditText.setAdapter(emailCompleteAdapter);

        mPasswordView = findViewById(R.id.passwordView);
        mPasswordEditText = (DailyEditText) findViewById(R.id.passwordEditText);
        mPasswordEditText.setDeleteButtonVisible(true, null);
        mPasswordEditText.setOnFocusChangeListener(this);
        mPasswordEditText.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_UP:
                        if (mIsKeypadOpend == false)
                        {
                            mScrollToEmailView = true;
                        }
                        break;
                }

                return false;
            }
        });

        mPasswordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mPasswordEditText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                switch (actionId)
                {
                    case EditorInfo.IME_ACTION_DONE:
                        mLoginView.performClick();
                        break;
                }
                return false;
            }
        });
    }

    private void initButtonsLayout()
    {
        mLoginView = (TextView) findViewById(R.id.signinView);

        mFindPasswordView = (TextView) findViewById(R.id.findPasswordView);
        mFindPasswordView.setPaintFlags(mFindPasswordView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mFindPasswordView.setOnClickListener(this);

        mSnsLoginLayout = findViewById(R.id.snsLoginLayout);

        mFacebookLoginView = (com.facebook.login.widget.LoginButton) mSnsLoginLayout.findViewById(R.id.facebookLoginButton);
        mFacebookLoginView.setReadPermissions(Collections.singletonList("public_profile"));

        View facebookLoginView = mSnsLoginLayout.findViewById(R.id.facebookLoginView);
        facebookLoginView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mFacebookLoginView.performClick();

                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOGIN_CLICKED, Label.FACEBOOK_LOGIN, null);
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginView.registerCallback(mCallbackManager, mFacebookCallback);

        FontManager.apply(mFacebookLoginView, FontManager.getInstance(getApplicationContext()).getRegularTypeface());

        mKakaoLoginView = (com.kakao.usermgmt.LoginButton) mSnsLoginLayout.findViewById(R.id.kakaoLoginButton);
        View kakaoLoginView = mSnsLoginLayout.findViewById(R.id.kakaoLoginView);
        kakaoLoginView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mKakaoLoginView.performClick();

                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOGIN_CLICKED, Label.KAKAO_LOGIN, null);
            }
        });

        mKakaoSessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(mKakaoSessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();

        mLoginView.setOnClickListener(this);
        mFacebookLoginView.setOnClickListener(this);
    }

    @Override
    protected void onStart()
    {
        AnalyticsManager.getInstance(LoginActivity.this).recordScreen(Screen.SIGNIN);

        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Session.getCurrentSession().removeCallback(mKakaoSessionCallback);

        if (Util.isOverAPI16() == true)
        {
            mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        } else
        {
            mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
        }
    }

    private void registerFacebookUser(String id, String name, String email, String gender)
    {
        if (mStoreParams == null)
        {
            mStoreParams = new HashMap<>();
        }

        mStoreParams.clear();
        HashMap<String, String> params = new HashMap<>();

        if (Util.isTextEmpty(email) == false)
        {
            params.put("email", email);
        }

        if (Util.isTextEmpty(id) == false)
        {
            params.put("social_id", id);
        }

        params.put("pw", Crypto.encrypt(id));
        params.put("user_type", Constants.FACEBOOK_USER);

        mStoreParams.putAll(params);

        if (Util.isTextEmpty(name) == false)
        {
            mStoreParams.put("name", name);
        }

        if (Util.isTextEmpty(gender) == false)
        {
            mStoreParams.put("gender", gender);
        }

        mStoreParams.put("market_type", RELEASE_STORE.getName());

        DailyMobileAPI.getInstance(this).requestFacebookUserLogin(mNetworkTag, params, mSocialUserLoginCallback);
    }

    private void registerKakaoUser(long id)
    {
        String index = String.valueOf(id);

        if (mStoreParams == null)
        {
            mStoreParams = new HashMap<>();
        }

        mStoreParams.clear();
        HashMap<String, String> params = new HashMap<>();

        if (Util.isTextEmpty(index) == false)
        {
            params.put("social_id", index);
        }

        params.put("pw", index);
        params.put("user_type", Constants.KAKAO_USER);

        mStoreParams.putAll(params);
        mStoreParams.put("market_type", RELEASE_STORE.getName());

        DailyMobileAPI.getInstance(this).requestKakaoUserLogin(mNetworkTag, params, mSocialUserLoginCallback);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.signUpView:
            {
                Intent intent = SignupStep1Activity.newInstance(this, mCallByScreen);
                startActivityForResult(intent, CODE_REQEUST_ACTIVITY_SIGNUP);

                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.REGISTRATION_CLICKED, Label.REGISTER_ACCOUNT, null);
                break;
            }

            case R.id.findPasswordView:
            {
                Intent intent = new Intent(this, ForgotPasswordActivity.class);
                startActivity(intent);

                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOST_PASSWORD_CLICKED, null, null);
                break;
            }

            case R.id.signinView:
            {
                processSignin();
                break;
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch (v.getId())
        {
            case R.id.emailEditText:
                setFocusLabelView(mEmailView, mEmailEditText, hasFocus);
                break;

            case R.id.passwordEditText:
                setFocusLabelView(mPasswordView, mPasswordEditText, hasFocus);
                break;
        }
    }

    private void processSignin()
    {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (Util.isTextEmpty(email) == true)
        {
            DailyToast.showToast(this, R.string.toast_msg_please_input_email, Toast.LENGTH_SHORT);
            return;
        }

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() == false)
        {
            DailyToast.showToast(this, R.string.toast_msg_wrong_email_address, Toast.LENGTH_SHORT);
            return;
        }

        if (Util.isTextEmpty(email) == true)
        {
            DailyToast.showToast(this, R.string.toast_msg_please_input_id, Toast.LENGTH_SHORT);
            return;
        }

        if (Util.isTextEmpty(password) == true)
        {
            DailyToast.showToast(this, R.string.toast_msg_please_input_passwd, Toast.LENGTH_SHORT);
            return;
        }

        if (password.length() < 4)
        {
            DailyToast.showToast(this, R.string.toast_msg_please_input_password_more_than_4chars, Toast.LENGTH_SHORT);
            return;
        }

        lockUI();

        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("pw", password);
        params.put("social_id", "0");
        params.put("user_type", Constants.DAILY_USER);

        if (mStoreParams == null)
        {
            mStoreParams = new HashMap<>();
        }

        mStoreParams.clear();
        mStoreParams.putAll(params);

        DailyMobileAPI.getInstance(this).requestDailyUserLogin(mNetworkTag, params, mDailyUserLoginCallback);

        AnalyticsManager.getInstance(getApplicationContext()).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOGIN_CLICKED, Label.EMAIL_LOGIN, null);
    }

    public String storeLoginInformation(JSONObject jsonObject) throws JSONException
    {
        JSONObject dataJSONObject = jsonObject.getJSONObject("data");
        JSONObject tokenJSONObject = jsonObject.getJSONObject("token");
        String accessToken = tokenJSONObject.getString("access_token");
        String tokenType = tokenJSONObject.getString("token_type");

        JSONObject userJSONObject = dataJSONObject.getJSONObject("user");
        String userIndex = userJSONObject.getString("idx");
        String email = userJSONObject.getString("email");
        String name = userJSONObject.getString("name");
        String recommender = userJSONObject.getString("rndnum");
        String userType = userJSONObject.getString("userType");
        //        String phoneNumber = userJSONObject.getString("phone");
        String birthday = userJSONObject.getString("birthday");

        DailyPreference.getInstance(this).setAuthorization(String.format("%s %s", tokenType, accessToken));
        DailyPreference.getInstance(this).setUserInformation(userType, email, name, birthday, recommender);

        if (Util.isTextEmpty(userIndex) == true || Util.isTextEmpty(name) == true)
        {
            if (Constants.DEBUG == true)
            {
                ExLog.w(jsonObject.toString());
            } else
            {
                Crashlytics.logException(new RuntimeException("JSON USER Check : " + jsonObject.toString(1)));
            }
        }

        AnalyticsManager.getInstance(this).setUserInformation(userIndex, userType);
        return userIndex;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        releaseUiComponent();

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case Constants.CODE_REQEUST_ACTIVITY_SIGNUP:
            {
                if (resultCode == RESULT_OK)
                {
                    setResult(RESULT_OK);
                    finish();
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

            default:
            {
                lockUI();

                try
                {
                    if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data) == true)
                    {
                        return;
                    }
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                }

                unLockUI();

                if (mCallbackManager != null)
                {
                    mCallbackManager.onActivityResult(requestCode, resultCode, data);
                }
                break;
            }
        }
    }

    @Override
    public void finish()
    {
        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.slide_out_right);
    }

    private void loginAndFinish()
    {
        unLockUI();

        if (mCertifyingTermination == true)
        {
            // 인증이 해지된 경우 알림 팝업을 띄운다.
            showSimpleDialog(null, getString(R.string.message_invalid_verification), getString(R.string.dialog_btn_text_confirm), new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    DailyToast.showToast(LoginActivity.this, R.string.toast_msg_logoined, Toast.LENGTH_SHORT);
                    setResult(RESULT_OK);
                    finish();
                }
            }, new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    DailyToast.showToast(LoginActivity.this, R.string.toast_msg_logoined, Toast.LENGTH_SHORT);
                    setResult(RESULT_OK);
                    finish();
                }
            });
        } else
        {
            // 소셜 신규 가입인 경우
            if (mIsSocialSignUp == true)
            {
                AnalyticsManager.getInstance(LoginActivity.this).signUpSocialUser(//
                    mStoreParams.get("user_idx"), mStoreParams.get("email"), mStoreParams.get("name")//
                    , mStoreParams.get("gender"), null, mStoreParams.get("user_type"), mCallByScreen);
            }

            DailyToast.showToast(LoginActivity.this, R.string.toast_msg_logoined, Toast.LENGTH_SHORT);
            setResult(RESULT_OK);
            finish();
        }
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

    private class SessionCallback implements ISessionCallback
    {
        @Override
        public void onSessionOpened()
        {
            lockUI();

            UserManagement.requestMe(new MeResponseCallback()
            {
                @Override
                public void onSuccess(UserProfile result)
                {
                    // id값은 특별함. kakao login
                    registerKakaoUser(result.getId());
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult)
                {
                    unLockUI();
                }

                @Override
                public void onNotSignedUp()
                {
                    unLockUI();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception)
        {
            unLockUI();
        }
    }

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener()
    {
        @Override
        public void onGlobalLayout()
        {
            final Rect rect = new Rect();
            mScrollView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = mScrollView.getRootView().getHeight();
            int keypadHeight = screenHeight - rect.bottom;

            if (keypadHeight > screenHeight * 0.15)
            {
                mIsKeypadOpend = true;

                if (mScrollToEmailView == true)
                {
                    mScrollToEmailView = false;

                    mSnsLoginLayout.setPadding(0, Util.dpToPx(LoginActivity.this, 40), 0, 0);

                    mScrollView.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mScrollView.scrollTo(0, Math.abs(mFindPasswordView.getBottom() - mScrollView.getHeight()));
                        }
                    });
                }
            } else
            {
                mIsKeypadOpend = false;

                mSnsLoginLayout.setPadding(0, 0, 0, 0);
            }
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private FacebookCallback mFacebookCallback = new FacebookCallback<com.facebook.login.LoginResult>()
    {
        @Override
        public void onSuccess(LoginResult result)
        {
            lockUI();

            GraphRequest request = GraphRequest.newMeRequest(result.getAccessToken(), new GraphRequest.GraphJSONObjectCallback()
            {
                @Override
                public void onCompleted(JSONObject jsonObject, GraphResponse response)
                {
                    try
                    {
                        String email = null;

                        if (jsonObject.has("email") == true)
                        {
                            email = jsonObject.getString("email");
                        }

                        String name = null;

                        if (jsonObject.has("name") == true)
                        {
                            name = jsonObject.getString("name");
                        }

                        String gender = null;

                        if (jsonObject.has("gender") == true)
                        {
                            gender = jsonObject.getString("gender");
                        }

                        String id = jsonObject.getString("id");

                        registerFacebookUser(id, name, email, gender);
                    } catch (Exception e)
                    {
                        ExLog.d(e.toString());
                    }
                }
            });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "name, email, gender");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel()
        {
        }

        @Override
        public void onError(FacebookException error)
        {
            showSimpleDialog(null, getString(R.string.message_error_facebook_login), getString(R.string.dialog_btn_text_confirm), null);
        }
    };

    private retrofit2.Callback mSocialUserSignupCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msg_code");

                    if (msgCode == 0)
                    {
                        JSONObject jsonObject = responseJSONObject.getJSONObject("data");

                        boolean isSignup = jsonObject.getBoolean("is_signup");

                        if (isSignup == true)
                        {
                            // 회원가입에 성공하면 이제 로그인 절차
                            mIsSocialSignUp = true;

                            DailyPreference.getInstance(LoginActivity.this).setUserBenefitAlarm(false);
                            DailyPreference.getInstance(LoginActivity.this).setShowBenefitAlarm(false);
                            DailyPreference.getInstance(LoginActivity.this).setShowBenefitAlarmFirstBuyer(false);
                            DailyPreference.getInstance(LoginActivity.this).setLastestCouponTime("");
                            AnalyticsManager.getInstance(LoginActivity.this).setPushEnabled(false, null);

                            HashMap<String, String> analyticsParams = new HashMap<>();

                            if (mStoreParams.containsKey("email") == true)
                            {
                                analyticsParams.put("email", mStoreParams.get("email"));
                            }

                            if (mStoreParams.containsKey("pw") == true)
                            {
                                analyticsParams.put("pw", mStoreParams.get("pw"));
                            }

                            if (mStoreParams.containsKey("social_id") == true)
                            {
                                analyticsParams.put("social_id", mStoreParams.get("social_id"));
                            }

                            mStoreParams.put("new_user", "1");

                            if (Constants.FACEBOOK_USER.equalsIgnoreCase(mStoreParams.get("user_type")) == true)
                            {
                                DailyMobileAPI.getInstance(LoginActivity.this).requestFacebookUserLogin(mNetworkTag, analyticsParams, mSocialUserLoginCallback);
                                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.SIGN_UP, AnalyticsManager.UserType.FACEBOOK, null);
                            } else if (Constants.KAKAO_USER.equalsIgnoreCase(mStoreParams.get("user_type")) == true)
                            {
                                DailyMobileAPI.getInstance(LoginActivity.this).requestKakaoUserLogin(mNetworkTag, analyticsParams, mSocialUserLoginCallback);
                                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.SIGN_UP, AnalyticsManager.UserType.KAKAO, null);
                            }

                            AnalyticsManager.getInstance(LoginActivity.this).recordScreen(Screen.MENU_REGISTRATION_CONFIRM);
                            AnalyticsManager.getInstance(LoginActivity.this).setUserName(mStoreParams.get("name"));
                            return;
                        }
                    }

                    unLockUI();
                    mStoreParams.clear();
                    mIsSocialSignUp = false;

                    String msg = responseJSONObject.getString("msg");

                    if (Util.isTextEmpty(msg) == true)
                    {
                        msg = getString(R.string.toast_msg_failed_to_signup);
                    }

                    DailyToast.showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);

                } catch (Exception e)
                {
                    unLockUI();
                    onError(e);
                }
            } else
            {
                LoginActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            LoginActivity.this.onError(t);
        }
    };

    private retrofit2.Callback mDailyUserLoginCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    int msgCode = responseJSONObject.getInt("msg_code");

                    if (msgCode == 0)
                    {
                        JSONObject jsonObject = responseJSONObject.getJSONObject("data");

                        boolean isSignin = jsonObject.getBoolean("is_signin");

                        if (isSignin == true)
                        {
                            DailyPreference.getInstance(LoginActivity.this).setLastestCouponTime("");

                            storeLoginInformation(responseJSONObject);

                            DailyPreference.getInstance(LoginActivity.this).setCollapsekey(null);
                            DailyMobileAPI.getInstance(LoginActivity.this).requestUserProfile(mNetworkTag, mUserProfileCallback);

                            AnalyticsManager.getInstance(LoginActivity.this).recordScreen(Screen.MENU_LOGIN_COMPLETE);
                            AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOGIN_COMPLETE, AnalyticsManager.UserType.EMAIL, null);
                            return;
                        }
                    }

                    unLockUI();

                    // 로그인이 실패한 경우
                    String msg = responseJSONObject.getString("msg");

                    if (Util.isTextEmpty(msg) == true)
                    {
                        msg = getString(R.string.toast_msg_failed_to_login);
                    }

                    DailyToast.showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
                } catch (Exception e)
                {
                    unLockUI();
                    ExLog.d(e.toString());
                }
            } else
            {
                LoginActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            LoginActivity.this.onError(t);
        }
    };

    private retrofit2.Callback mSocialUserLoginCallback = new retrofit2.Callback<JSONObject>()
    {
        @Override
        public void onResponse(Call<JSONObject> call, Response<JSONObject> response)
        {
            if (response != null && response.isSuccessful() && response.body() != null)
            {
                try
                {
                    JSONObject responseJSONObject = response.body();

                    // TODO :  추후에 msgCode결과를 가지고 구분하는 코드가 필요할듯.
                    int msgCode = responseJSONObject.getInt("msg_code");
                    JSONObject dataJSONObject = responseJSONObject.getJSONObject("data");
                    boolean isSignin = dataJSONObject.getBoolean("is_signin");

                    String userType = mStoreParams.get("user_type");

                    if (isSignin == true)
                    {
                        DailyPreference.getInstance(LoginActivity.this).setLastestCouponTime("");

                        String userIndex = storeLoginInformation(responseJSONObject);

                        DailyPreference.getInstance(LoginActivity.this).setCollapsekey(null);
                        DailyMobileAPI.getInstance(LoginActivity.this).requestUserProfile(mNetworkTag, mUserProfileCallback);

                        // 소셜 신규 가입인 경우
                        if (mIsSocialSignUp == true)
                        {
                            mStoreParams.put("user_idx", userIndex);
                            mStoreParams.put("user_type", userType);
                        } else
                        {
                            AnalyticsManager.getInstance(LoginActivity.this).recordScreen(Screen.MENU_LOGIN_COMPLETE);

                            if (Constants.KAKAO_USER.equalsIgnoreCase(userType) == true)
                            {
                                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOGIN_COMPLETE, AnalyticsManager.UserType.KAKAO, null);
                            } else if (Constants.FACEBOOK_USER.equalsIgnoreCase(userType) == true)
                            {
                                AnalyticsManager.getInstance(LoginActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION, Action.LOGIN_COMPLETE, AnalyticsManager.UserType.FACEBOOK, null);
                            }
                        }
                    } else
                    {
                        mIsSocialSignUp = false;

                        // 페이스북, 카카오톡 로그인 정보가 없는 경우 회원 가입으로 전환한다
                        if (Constants.FACEBOOK_USER.equalsIgnoreCase(userType) == true)
                        {
                            DailyMobileAPI.getInstance(LoginActivity.this).requestFacebookUserSignup(mNetworkTag, mStoreParams, mSocialUserSignupCallback);
                        } else if (Constants.KAKAO_USER.equalsIgnoreCase(userType) == true)
                        {
                            DailyMobileAPI.getInstance(LoginActivity.this).requestKakaoUserSignup(mNetworkTag, mStoreParams, mSocialUserSignupCallback);
                        }
                    }
                } catch (Exception e)
                {
                    unLockUI();
                    ExLog.d(e.toString());
                }
            } else
            {
                LoginActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            LoginActivity.this.onError(t);
        }
    };

    private retrofit2.Callback mUserProfileCallback = new retrofit2.Callback<JSONObject>()
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
                        JSONObject jsonObject = responseJSONObject.getJSONObject("data");

                        boolean isAgreedBenefit = jsonObject.getBoolean("agreedBenefit");

                        DailyPreference.getInstance(LoginActivity.this).setUserBenefitAlarm(isAgreedBenefit);
                        AnalyticsManager.getInstance(LoginActivity.this).setPushEnabled(isAgreedBenefit, null);

                        String userIndex = jsonObject.getString("userIdx");
                        boolean isVerified = jsonObject.getBoolean("verified");
                        boolean isPhoneVerified = jsonObject.getBoolean("phoneVerified");

                        if (isVerified == true && isPhoneVerified == true)
                        {
                            DailyPreference.getInstance(LoginActivity.this).setVerification(true);
                        } else if (isVerified == true && isPhoneVerified == false)
                        {
                            // 로그인시에 인증이 해지된 경우 알림 팝업을 띄운다.
                            mCertifyingTermination = true;
                        }

                        loginAndFinish();
                    } else
                    {
                        String msg = responseJSONObject.getString("msg");
                        DailyToast.showToast(LoginActivity.this, msg, Toast.LENGTH_SHORT);
                        finish();
                    }
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                }
            } else
            {
                LoginActivity.this.onErrorResponse(call, response);
            }
        }

        @Override
        public void onFailure(Call<JSONObject> call, Throwable t)
        {
            LoginActivity.this.onError(t);
        }
    };
}
