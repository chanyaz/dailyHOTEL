package com.twoheart.dailyhotel.screen.information.member;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseLayout;
import com.twoheart.dailyhotel.place.base.OnBaseEventListener;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.StringFilter;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.widget.DailyToolbarLayout;

import java.util.Calendar;

public class SignupStep1Layout extends BaseLayout implements OnClickListener, View.OnFocusChangeListener, View.OnTouchListener
{
    private static final int MAX_OF_RECOMMENDER = 45;

    private View mEmailView, mNameView, mBirthdayView, mPasswordView, mConfirmPasswordView, mRecommenderView;
    private EditText mEmailEditText, mNameEditText, mPasswordEditText;
    private EditText mBirthdayEditText, mConfirmPasswordEditText, mRecommenderEditText;
    private CheckBox mAllAgreementCheckBox;
    private CheckBox mTermsOfServiceCheckBox;
    private CheckBox mTermsOfPrivacyCheckBox;
    private CheckBox mBenefitCheckBox;

    public interface OnEventListener extends OnBaseEventListener
    {
        void onValidation(String email, String name, String password1, String confirmPassword, String recommender, String birthday);

        void showTermOfService();


        void showTermOfPrivacy();

        void showBirthdayDatePicker();
    }

    public SignupStep1Layout(Context context, OnEventListener mOnEventListener)
    {
        super(context, mOnEventListener);
    }

    @Override
    protected void initLayout(View view)
    {
        initToolbar(view);
        initLayoutForm(view);
        initLayoutCheckBox(view);
    }

    private void initToolbar(View view)
    {
        View toolbar = view.findViewById(R.id.toolbar);
        DailyToolbarLayout dailyToolbarLayout = new DailyToolbarLayout(mContext, toolbar);
        dailyToolbarLayout.initToolbar(mContext.getString(R.string.actionbar_title_signup_1_activity), new OnClickListener()
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
        final ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollLayout);
        EdgeEffectColor.setEdgeGlowColor(scrollView, mContext.getResources().getColor(R.color.default_over_scroll_edge));

        mEmailView = view.findViewById(R.id.emailView);
        mEmailEditText = (EditText) view.findViewById(R.id.emailEditText);
        mEmailEditText.setOnFocusChangeListener(this);
        mEmailEditText.setOnTouchListener(this);

        mNameView = view.findViewById(R.id.nameView);
        mNameEditText = (EditText) view.findViewById(R.id.nameEditText);
        mNameEditText.setOnFocusChangeListener(this);
        mNameEditText.setOnTouchListener(this);

        mNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    mNameEditText.requestFocus();

                    InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mNameEditText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        mPasswordView = view.findViewById(R.id.passwordView);
        mPasswordEditText = (EditText) view.findViewById(R.id.passwordEditText);
        mPasswordEditText.setOnFocusChangeListener(this);
        mPasswordEditText.setOnTouchListener(this);

        mConfirmPasswordView = view.findViewById(R.id.confirmPasswordView);
        mConfirmPasswordEditText = (EditText) view.findViewById(R.id.confirmPasswordEditText);
        mConfirmPasswordEditText.setOnFocusChangeListener(this);
        mConfirmPasswordEditText.setOnTouchListener(this);

        mBirthdayView = view.findViewById(R.id.birthdayView);
        mBirthdayEditText = (EditText) view.findViewById(R.id.birthdayEditText);
        mBirthdayEditText.setOnFocusChangeListener(this);
        mBirthdayEditText.setOnTouchListener(this);
        mBirthdayEditText.setKeyListener(null);
        mBirthdayEditText.setOnClickListener(this);

        mRecommenderView = view.findViewById(R.id.recommenderView);
        mRecommenderEditText = (EditText) view.findViewById(R.id.recommenderEditText);
        mRecommenderEditText.setOnFocusChangeListener(this);
        mRecommenderEditText.setOnTouchListener(this);

        // 회원 가입시 이름 필터 적용.
        StringFilter stringFilter = new StringFilter(mContext);
        InputFilter[] allowAlphanumericHangul = new InputFilter[2];
        allowAlphanumericHangul[0] = stringFilter.allowAlphanumericHangul;
        allowAlphanumericHangul[1] = new InputFilter.LengthFilter(20);

        mNameEditText.setFilters(allowAlphanumericHangul);

        // 추천인 코드 최대 길이
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(MAX_OF_RECOMMENDER);
        mRecommenderEditText.setFilters(fArray);

        View nextStepView = view.findViewById(R.id.nextStepView);
        nextStepView.setOnClickListener(this);

        mEmailView.requestFocus();
    }

    private void initLayoutCheckBox(View view)
    {
        mAllAgreementCheckBox = (CheckBox) view.findViewById(R.id.allAgreementCheckBox);
        mTermsOfPrivacyCheckBox = (CheckBox) view.findViewById(R.id.personalCheckBox);
        mTermsOfServiceCheckBox = (CheckBox) view.findViewById(R.id.termsCheckBox);
        mBenefitCheckBox = (CheckBox) view.findViewById(R.id.benefitCheckBox);

        mAllAgreementCheckBox.setOnClickListener(this);
        mTermsOfPrivacyCheckBox.setOnClickListener(this);
        mTermsOfServiceCheckBox.setOnClickListener(this);
        mBenefitCheckBox.setOnClickListener(this);

        TextView termsContentView = (TextView) view.findViewById(R.id.termsContentView);
        termsContentView.setPaintFlags(termsContentView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        termsContentView.setOnClickListener(this);

        TextView personalContentView = (TextView) view.findViewById(R.id.personalContentView);
        personalContentView.setPaintFlags(personalContentView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        personalContentView.setOnClickListener(this);
    }

    public boolean isCheckedTermsOfService()
    {
        return mTermsOfServiceCheckBox.isChecked();
    }

    public boolean isCheckedTermsOfPrivacy()
    {
        return mTermsOfPrivacyCheckBox.isChecked();
    }

    public boolean isBenefitCheckBox()
    {
        return mBenefitCheckBox.isChecked();
    }

    public void setBirthdayText(int year, int month, int dayOfMonth)
    {
        Calendar calendar = DailyCalendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0);

        mBirthdayEditText.setText(String.format("%4d.%02d.%02d", year, month + 1, dayOfMonth));
        mBirthdayEditText.setTag(calendar);
    }

    public void setRecommenderText(String recommender)
    {
        if (mRecommenderEditText == null)
        {
            return;
        }

        mRecommenderEditText.setText(recommender);
    }

    private void nextStep()
    {
        String emailText = mEmailEditText.getText().toString().trim();
        String nameText = mNameEditText.getText().toString().trim();
        // 패스워드는 trim하지 않는다.
        String passwordText = mPasswordEditText.getText().toString();
        String confirmPasswordText = mConfirmPasswordEditText.getText().toString();
        String recommender = mRecommenderEditText.getText().toString().trim();
        // 생일
        String birthday = mBirthdayEditText.getText().toString().trim();

        if (Util.isTextEmpty(birthday) == false)
        {
            Calendar calendar = (Calendar) mBirthdayEditText.getTag();

            if (calendar == null)
            {
                birthday = null;
            } else
            {
                birthday = DailyCalendar.format(calendar.getTime(), DailyCalendar.ISO_8601_FORMAT);
            }
        }

        ((OnEventListener) mOnEventListener).onValidation(emailText, nameText, passwordText, confirmPasswordText, recommender, birthday);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.nextStepView:
                nextStep();
                break;

            case R.id.termsContentView:
                ((OnEventListener) mOnEventListener).showTermOfService();
                break;

            case R.id.personalContentView:
                ((OnEventListener) mOnEventListener).showTermOfPrivacy();
                break;

            case R.id.allAgreementCheckBox:
            {
                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                boolean isChecked = mAllAgreementCheckBox.isChecked();

                mTermsOfServiceCheckBox.setChecked(isChecked);
                mTermsOfPrivacyCheckBox.setChecked(isChecked);
                mBenefitCheckBox.setChecked(isChecked);
                break;
            }

            case R.id.personalCheckBox:
            case R.id.termsCheckBox:
            case R.id.benefitCheckBox:
            {
                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                if (mTermsOfPrivacyCheckBox.isChecked() == true && mTermsOfServiceCheckBox.isChecked() == true && mBenefitCheckBox.isChecked() == true)
                {
                    mAllAgreementCheckBox.setChecked(true);
                } else
                {
                    mAllAgreementCheckBox.setChecked(false);
                }
                break;
            }

            case R.id.birthdayEditText:
                onFocusChange(mBirthdayEditText, true);
                break;
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

            case R.id.nameEditText:
                setFocusLabelView(mNameView, mNameEditText, hasFocus);
                break;

            case R.id.passwordEditText:
                setFocusLabelView(mPasswordView, mPasswordEditText, hasFocus);
                break;

            case R.id.confirmPasswordEditText:
                setFocusLabelView(mConfirmPasswordView, mConfirmPasswordEditText, hasFocus);
                break;

            case R.id.birthdayEditText:
                setFocusLabelView(mBirthdayView, mBirthdayEditText, hasFocus);

                if (hasFocus == true)
                {
                    ((OnEventListener) mOnEventListener).showBirthdayDatePicker();
                }
                break;

            case R.id.recommenderEditText:
                setFocusLabelView(mRecommenderView, mRecommenderEditText, hasFocus);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (v instanceof EditText == false)
        {
            return false;
        }

        EditText editText = (EditText) v;

        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            Drawable[] drawables = editText.getCompoundDrawables();

            if (drawables == null || drawables[DRAWABLE_RIGHT] == null)
            {
                return false;
            }

            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()))
            {
                editText.setText(null);
                return true;
            }
        }

        return false;
    }

    private void resetFocus()
    {
        mEmailView.setSelected(false);
        mNameView.setSelected(false);
        mPasswordView.setSelected(false);
        mConfirmPasswordView.setSelected(false);
        mRecommenderView.setSelected(false);
        mBirthdayView.setSelected(false);
    }

    private void setFocusLabelView(View labelView, EditText editText, boolean hasFocus)
    {
        if (hasFocus == true)
        {
            labelView.setActivated(false);
            labelView.setSelected(true);

            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.search_ic_01_delete, 0);
        } else
        {
            if (editText.length() > 0)
            {
                labelView.setActivated(true);
            }

            labelView.setSelected(false);

            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
}
