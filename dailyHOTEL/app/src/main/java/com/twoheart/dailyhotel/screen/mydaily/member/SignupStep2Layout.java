package com.twoheart.dailyhotel.screen.mydaily.member;

import android.content.Context;
import android.graphics.Rect;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.VersionUtils;
import com.daily.base.widget.DailyEditText;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.PhoneNumberKoreaFormattingTextWatcher;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import java.util.Locale;

public class SignupStep2Layout extends BaseLayout implements OnClickListener, View.OnFocusChangeListener
{
    private static final int VERIFICATION_NUMBER_LENGTH = 4;

    View mVerificationLayout, mSignUpView, mCertificationNumberView;
    private View mPhoneView, mVerificationView;
    DailyEditText mCountryEditText, mPhoneEditText, mVerificationEditText;
    private TextWatcher mTextWatcher;
    ScrollView mScrollView;

    public interface OnEventListener extends OnBaseEventListener
    {
        void showCountryCodeList();

        void doVerification(String phoneNumber);

        void doSignUp(String verificationNumber, String phoneNumber);
    }

    public SignupStep2Layout(Context context, OnEventListener mOnEventListener)
    {
        super(context, mOnEventListener);
    }

    @Override
    protected void initLayout(View view)
    {
        initToolbar(view);
        initLayoutForm(view);
    }

    private void initToolbar(View view)
    {
        View toolbar = view.findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(mContext, toolbar);
        dailyToolbarLayout.initToolbar(mContext.getString(R.string.actionbar_title_signup_2_activity), new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mOnEventListener.finish();
            }
        });
    }

    private void initLayoutForm(View view)
    {
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
        EdgeEffectColor.setEdgeGlowColor(mScrollView, mContext.getResources().getColor(R.color.default_over_scroll_edge));

        mCountryEditText = (DailyEditText) view.findViewById(R.id.countryEditText);
        mCountryEditText.setFocusable(false);
        mCountryEditText.setCursorVisible(false);
        mCountryEditText.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((OnEventListener) mOnEventListener).showCountryCodeList();
            }
        });

        mPhoneView = view.findViewById(R.id.phoneView);
        mPhoneEditText = (DailyEditText) view.findViewById(R.id.phoneEditText);
        mPhoneEditText.setDeleteButtonVisible(null);
        mPhoneEditText.setOnFocusChangeListener(this);
        mPhoneEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_NEXT)
                {
                    // 번호 검증 후에 인증번호 요청
                    String phoneNumber = getPhoneNumber();

                    if (Util.isValidatePhoneNumber(phoneNumber) == true)
                    {
                        ((OnEventListener) mOnEventListener).doVerification(phoneNumber);
                        return true;
                    }
                }

                return false;
            }
        });

        // 알단 테스트
        mPhoneEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (mVerificationLayout != null && mVerificationLayout.getVisibility() == View.VISIBLE)
                {
                    if (count > 0 || s.length() == 0)
                    {
                        hideVerificationVisible();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                provenCertificationButton(s.toString());
            }
        });

        mCertificationNumberView = view.findViewById(R.id.certificationNumberView);
        mCertificationNumberView.setOnClickListener(this);
        mCertificationNumberView.setEnabled(false);

        mVerificationLayout = view.findViewById(R.id.verificationLayout);
        mVerificationLayout.setVisibility(View.INVISIBLE);
        mVerificationView = mVerificationLayout.findViewById(R.id.verificationView);

        mVerificationEditText = (DailyEditText) mVerificationLayout.findViewById(R.id.verificationEditText);
        mVerificationEditText.setDeleteButtonVisible(null);
        mVerificationEditText.setOnFocusChangeListener(this);

        mVerificationEditText.addTextChangedListener(new TextWatcher()
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
                int length = s.length();

                if (length == 0)
                {
                    mSignUpView.setEnabled(false);
                } else if (length > 0)
                {
                    mSignUpView.setEnabled(true);
                }

                if (length >= VERIFICATION_NUMBER_LENGTH)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mVerificationEditText.getWindowToken(), 0);
                }
            }
        });

        mVerificationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    doSignUp();
                }

                return false;
            }
        });

        mSignUpView = view.findViewById(R.id.signUpView);
        mSignUpView.setOnClickListener(this);
        mSignUpView.setVisibility(View.INVISIBLE);
        mSignUpView.setEnabled(false);

        mPhoneEditText.requestFocus();
    }

    public void setCountryCode(String countryCode)
    {
        if (DailyTextUtils.isTextEmpty(countryCode) == true)
        {
            return;
        }

        String previousCountryCode = (String) mCountryEditText.getTag();

        // 지역이 변경되면 전화번호 초기화
        if (countryCode.equalsIgnoreCase(previousCountryCode) == false)
        {
            mPhoneEditText.setText(null);
        }

        mCountryEditText.setText(countryCode.substring(0, countryCode.indexOf('\n')));
        mCountryEditText.setTag(countryCode);

        if (mTextWatcher != null)
        {
            mPhoneEditText.removeTextChangedListener(mTextWatcher);
        }

        if (Util.DEFAULT_COUNTRY_CODE.equalsIgnoreCase(countryCode) == true)
        {
            mTextWatcher = new PhoneNumberKoreaFormattingTextWatcher(mContext);
        } else
        {
            mTextWatcher = new PhoneNumberFormattingTextWatcher();
        }

        mPhoneEditText.addTextChangedListener(mTextWatcher);

        provenCertificationButton(mPhoneEditText.getText().toString());
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.signUpView:
            {
                doSignUp();
                break;
            }

            case R.id.certificationNumberView:
            {
                if (v.isEnabled() == false)
                {
                    return;
                }

                String phoneNumber = getPhoneNumber();

                // SMS 인증 요청
                ((OnEventListener) mOnEventListener).doVerification(phoneNumber);
                break;
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch (v.getId())
        {
            case R.id.phoneEditText:
                setFocusLabelView(mPhoneView, mPhoneEditText, hasFocus);
                break;

            case R.id.verificationEditText:
                setFocusLabelView(mVerificationView, mVerificationEditText, hasFocus);
                break;
        }
    }

    public void showVerificationVisible()
    {
        mVerificationLayout.setVisibility(View.VISIBLE);
        mVerificationEditText.requestFocus();

        mSignUpView.setVisibility(View.VISIBLE);

        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Rect rect = new Rect();
                mScrollView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = mScrollView.getRootView().getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (keypadHeight > screenHeight * 0.15)
                {
                    mScrollView.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mScrollView.fullScroll(View.FOCUS_DOWN);

                            mVerificationEditText.requestFocus();
                        }
                    });

                    if (VersionUtils.isOverAPI16() == true)
                    {
                        mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else
                    {
                        mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

    public void hideVerificationVisible()
    {
        mVerificationLayout.setVisibility(View.INVISIBLE);
        mVerificationEditText.setText(null);

        mSignUpView.setVisibility(View.INVISIBLE);
        mSignUpView.setEnabled(false);
    }


    public void resetPhoneNumber()
    {
        mPhoneEditText.setText(null);
    }

    void doSignUp()
    {
        String verificationNumber = mVerificationEditText.getText().toString().trim();

        ((OnEventListener) mOnEventListener).doSignUp(verificationNumber, getPhoneNumber());
    }

    public String getPhoneNumber()
    {
        String tag = (String) mCountryEditText.getTag();

        if (DailyTextUtils.isTextEmpty(tag) == true)
        {
            tag = Util.DEFAULT_COUNTRY_CODE;
        }

        String countryCode = tag.substring(tag.indexOf('\n') + 1);
        String phoneNumber = String.format(Locale.KOREA, "%s %s", countryCode, mPhoneEditText.getText().toString().trim());

        return phoneNumber;
    }

    boolean provenCertificationButton(String phoneNumber)
    {
        String tag = (String) mCountryEditText.getTag();

        if (DailyTextUtils.isTextEmpty(tag) == true)
        {
            tag = Util.DEFAULT_COUNTRY_CODE;
        }

        // 입력한 전화번호가 이상이 없는 경우 인증번호 받기가 활성화 된다.
        String countryCode = tag.substring(tag.indexOf('\n') + 1);

        if (Util.isValidatePhoneNumber(countryCode + ' ' + phoneNumber) == true)
        {
            mCertificationNumberView.setEnabled(true);
            return true;
        } else
        {
            mCertificationNumberView.setEnabled(false);
            return false;
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
}
