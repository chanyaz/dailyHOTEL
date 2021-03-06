package com.daily.dailyhotel.screen.home.gourmet.thankyou;

import android.animation.Animator;
import android.text.SpannableString;

import com.daily.base.BaseDialogViewInterface;
import com.daily.dailyhotel.entity.GourmetCart;

public interface GourmetThankYouInterface extends BaseDialogViewInterface
{
    void setUserName(String guestName);

    void setImageUrl(String imageUrl);

    void setBooking(SpannableString visitDate, int persons, GourmetCart gourmetCart);

    void startAnimation(Animator.AnimatorListener listener);
}
