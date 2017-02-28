package com.twoheart.dailyhotel.place.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ScrollView;
import android.widget.TextView;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.networkcontroller.PlacePaymentThankyouNetworkController;
import com.twoheart.dailyhotel.util.EdgeEffectColor;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.CustomFontTypefaceSpan;
import com.twoheart.dailyhotel.widget.FontManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public abstract class PlacePaymentThankyouActivity extends BaseActivity implements OnClickListener
{
    protected static final String INTENT_EXTRA_DATA_IMAGEURL = "imageUrl";
    protected static final String INTENT_EXTRA_DATA_PLACE_NAME = "placeName";
    protected static final String INTENT_EXTRA_DATA_PLACE_TYPE = "placeType";
    protected static final String INTENT_EXTRA_DATA_USER_NAME = "userName";
    protected static final String INTENT_EXTRA_DATA_CHECK_IN_DATE = "checkIn";
    protected static final String INTENT_EXTRA_DATA_CHECK_OUT_DATE = "checkOut";
    protected static final String INTENT_EXTRA_DATA_NIGHTS = "nights";
    protected static final String INTENT_EXTRA_DATA_VISIT_TIME = "visitTime";
    protected static final String INTENT_EXTRA_DATA_PRODUCT_COUNT = "productCount";
    protected static final String INTENT_EXTRA_DATA_PAYMENT_TYPE = "paymentType";
    protected static final String INTENT_EXTRA_DATA_DISCOUNT_TYPE = "discountType";
    protected static final String INTENT_EXTRA_DATA_MAP_PAYMENT_INFORM = "mapPaymentInform";

    String mPaymentType;
    Map<String, String> mParams;

    protected abstract void recordEvent(String action, String label);

    protected abstract void onFirstPurchaseSuccess(boolean isFirstStayPurchase, boolean isFirstGourmetPurchase, String paymentType, Map<String, String> params);

    protected abstract void onCouponUsedPurchase(boolean isFirstStayPurchase, boolean isFirstGourmetPurchase, String paymentType, Map<String, String> params);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.abc_fade_in, R.anim.hold);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_payment_result);

        Intent intent = getIntent();

        if (intent == null)
        {
            Util.restartApp(this);
            return;
        }

        mPaymentType = intent.getStringExtra(INTENT_EXTRA_DATA_PAYMENT_TYPE);

        String imageUrl = intent.getStringExtra(INTENT_EXTRA_DATA_IMAGEURL);
        String placeName = intent.getStringExtra(INTENT_EXTRA_DATA_PLACE_NAME);
        String placeType = intent.getStringExtra(INTENT_EXTRA_DATA_PLACE_TYPE);
        String userName = intent.getStringExtra(INTENT_EXTRA_DATA_USER_NAME);

        String discountType = intent.getStringExtra(INTENT_EXTRA_DATA_DISCOUNT_TYPE);

        mParams = (Map<String, String>) intent.getSerializableExtra(INTENT_EXTRA_DATA_MAP_PAYMENT_INFORM);

        String productIndex = mParams.get(AnalyticsManager.KeyType.TICKET_INDEX);

        initToolbar();
        initLayout(imageUrl, placeName, placeType, userName);

        final ScrollView informationLayout = (ScrollView) findViewById(R.id.informationLayout);
        EdgeEffectColor.setEdgeGlowColor(informationLayout, getResources().getColor(R.color.default_over_scroll_edge));

        recordEvent(AnalyticsManager.Action.END_PAYMENT, mPaymentType);
        recordEvent(AnalyticsManager.Action.PAYMENT_USED, discountType);
        recordEvent(AnalyticsManager.Action.PRODUCT_ID, productIndex);

        PlacePaymentThankyouNetworkController networkController = new PlacePaymentThankyouNetworkController(this, mNetworkTag, mNetworkControllerListener);
        networkController.requestUserTracking();
    }

    private void initToolbar()
    {
        View closeView = findViewById(R.id.closeView);
        closeView.setOnClickListener(this);
    }

    private void initLayout(String imageUrl, String place, String placeType, String userName)
    {
        if (Util.isTextEmpty(place, placeType) == true)
        {
            Util.restartApp(this);
            return;
        }

        int imageHeight = Util.getRatioHeightType4x3(Util.getLCDWidth(this));
        com.facebook.drawee.view.SimpleDraweeView simpleDraweeView = (com.facebook.drawee.view.SimpleDraweeView) findViewById(R.id.placeImageView);
        ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();
        layoutParams.height = imageHeight;
        simpleDraweeView.setLayoutParams(layoutParams);

        TextView placeTextView = (TextView) findViewById(R.id.bookingPlaceTextView);
        TextView placeTypeTextView = (TextView) findViewById(R.id.productTypeTextView);
        TextView messageTextView = (TextView) findViewById(R.id.messageTextView);
        View confirmView = findViewById(R.id.confirmView);

        Util.requestImageResize(this, simpleDraweeView, imageUrl);
        placeTextView.setText(place);
        placeTypeTextView.setText(placeType);

        String message;
        if (Util.isTextEmpty(userName) == false)
        {
            message = getString(R.string.message_completed_payment_format, userName);
            SpannableStringBuilder userNameBuilder = new SpannableStringBuilder(message);
            userNameBuilder.setSpan( //
                new CustomFontTypefaceSpan(FontManager.getInstance(this).getMediumTypeface()),//
                0, userName.length(),//
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageTextView.setText(userNameBuilder);
        } else
        {
            message = getString(R.string.message_completed_payment_default);
            messageTextView.setText(message);
        }

        confirmView.setOnClickListener(this);

        startReceiptAnimation();
    }

    private void startReceiptAnimation()
    {
        final View confirmImageView = findViewById(R.id.confirmImageView);
        confirmImageView.setVisibility(View.INVISIBLE);
        final View receiptLayout = findViewById(R.id.receiptLayout);

        float startY = 0f - Util.getLCDHeight(PlacePaymentThankyouActivity.this);
        final float endY = 0.0f;

        final float startScaleY = 2.3f;
        final float endScaleY = 1.0f;

        int animatorSetStartDelay = Util.isOverAPI21() ? 400 : 600;
        int transAnimatorDuration = Util.isOverAPI21() ? 300 : 400;
        int scaleAnimatorStartDelay = transAnimatorDuration - 50;
        int scaleAnimatorDuration = Util.isOverAPI21() ? 200 : 200;

        receiptLayout.setTranslationY(startY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setStartDelay(animatorSetStartDelay);

        final ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(confirmImageView //
            , PropertyValuesHolder.ofFloat("scaleX", startScaleY, endScaleY) //
            , PropertyValuesHolder.ofFloat("scaleY", startScaleY, endScaleY) //
            , PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f) //
        );

        scaleAnimator.setDuration(scaleAnimatorDuration);
        scaleAnimator.setStartDelay(scaleAnimatorStartDelay);
        scaleAnimator.setInterpolator(new OvershootInterpolator(1.6f));
        scaleAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                confirmImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                confirmImageView.setScaleX(endScaleY);
                confirmImageView.setScaleY(endScaleY);

                confirmImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
                confirmImageView.setScaleX(endScaleY);
                confirmImageView.setScaleY(endScaleY);

                confirmImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });

        ObjectAnimator translateAnimator = ObjectAnimator.ofPropertyValuesHolder(receiptLayout //
            , PropertyValuesHolder.ofFloat("translationY", startY, endY) //
        );

        translateAnimator.setDuration(transAnimatorDuration);
        translateAnimator.setInterpolator(new OvershootInterpolator(0.82f));
        translateAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {

            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                receiptLayout.setTranslationY(endY);
            }

            @Override
            public void onAnimationCancel(Animator animation)
            {
                receiptLayout.setTranslationY(endY);
            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });

        animatorSet.playTogether(translateAnimator, scaleAnimator);
        animatorSet.start();
    }

    @Override
    public void finish()
    {
        setResult(RESULT_OK);

        super.finish();

        overridePendingTransition(R.anim.hold, R.anim.abc_fade_out);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.closeView:
                recordEvent(AnalyticsManager.Action.THANKYOU_SCREEN_BUTTON_CLICKED, AnalyticsManager.Label.CLOSE_BUTTON_CLICKED);
                finish();
                break;
            case R.id.confirmView:
                recordEvent(AnalyticsManager.Action.THANKYOU_SCREEN_BUTTON_CLICKED, AnalyticsManager.Label.VIEW_BOOKING_STATUS_CLICKED);
                finish();
                break;
        }
    }

    private PlacePaymentThankyouNetworkController.OnNetworkControllerListener mNetworkControllerListener = new PlacePaymentThankyouNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onUserTracking(int hotelPaymentCompletedCount, int gourmetPaymentCompletedCount)
        {
            boolean isFirstStayPurchase = hotelPaymentCompletedCount == 1;
            boolean isFirstGourmetPurchase = gourmetPaymentCompletedCount == 1;
            boolean isCouponUsed = false;

            if (mParams != null && mParams.containsKey(AnalyticsManager.KeyType.COUPON_REDEEM) == true)
            {
                try
                {
                    isCouponUsed = Boolean.parseBoolean(mParams.get(AnalyticsManager.KeyType.COUPON_REDEEM));
                } catch (Exception e)
                {
                    ExLog.d(e.toString());
                }
            }

            if (isFirstStayPurchase == true || isFirstGourmetPurchase == true)
            {
                PlacePaymentThankyouActivity.this.onFirstPurchaseSuccess(isFirstStayPurchase, isFirstGourmetPurchase, mPaymentType, mParams);

            }

            if (isCouponUsed == true)
            {
                PlacePaymentThankyouActivity.this.onCouponUsedPurchase(isFirstStayPurchase, isFirstGourmetPurchase, mPaymentType, mParams);
            }
        }

        @Override
        public void onError(Throwable e)
        {
            // do nothing
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            // do nothing
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            // do nothing
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            // do nothing
        }
    };
}
