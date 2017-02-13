package com.twoheart.dailyhotel.screen.main;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.firebase.DailyRemoteConfig;
import com.twoheart.dailyhotel.model.Review;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.screen.common.CloseOnBackPressed;
import com.twoheart.dailyhotel.screen.common.ExitActivity;
import com.twoheart.dailyhotel.screen.gourmet.list.GourmetMainActivity;
import com.twoheart.dailyhotel.screen.home.HomeFragment;
import com.twoheart.dailyhotel.screen.hotel.list.StayMainActivity;
import com.twoheart.dailyhotel.screen.review.ReviewActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyPreference;
import com.twoheart.dailyhotel.util.ExLog;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;
import com.twoheart.dailyhotel.widget.DailyImageView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements Constants
{
    public static final String BROADCAST_EVENT_UPDATE = " com.twoheart.dailyhotel.broadcastreceiver.EVENT_UPDATE";

    // Back 버튼을 두 번 눌러 핸들러 멤버 변수
    private CloseOnBackPressed mBackButtonHandler;
    MainNetworkController mNetworkController;
    MainFragmentManager mMainFragmentManager;
    MenuBarLayout mMenuBarLayout;
    private Dialog mSettingNetworkDialog;
    View mSplashLayout, mTooltipLayout;

    boolean mIsInitialization;
    boolean mIsBenefitAlarm;
    Handler mDelayTimeHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (isFinishing() == true)
            {
                return;
            }

            switch (msg.what)
            {
                case 0:
                    if (mIsInitialization == true)
                    {
                        lockUIImmediately();
                    }
                    break;

                case 1:
                    break;

                case 2:
                {
                    if (mSplashLayout.getVisibility() == View.VISIBLE)
                    {
                        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                        animation.setDuration(400);
                        animation.setAnimationListener(new Animation.AnimationListener()
                        {
                            @Override
                            public void onAnimationStart(Animation animation)
                            {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation)
                            {
                                mSplashLayout.setVisibility(View.GONE);
                                mSplashLayout.setAnimation(null);

                                ViewGroup viewGroup = (ViewGroup) mSplashLayout.getParent();
                                viewGroup.removeView(mSplashLayout);
                                mSplashLayout = null;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation)
                            {

                            }
                        });

                        mSplashLayout.startAnimation(animation);
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mIsInitialization = true;
        mNetworkController = new MainNetworkController(MainActivity.this, mNetworkTag, mOnNetworkControllerListener);

        DailyPreference.getInstance(this).setSettingRegion(PlaceType.HOTEL, false);
        DailyPreference.getInstance(this).setSettingRegion(PlaceType.FNB, false);
        DailyPreference.getInstance(this).clearPaymentInformation();

        // 현재 앱버전을 Analytics로..
        String version = DailyPreference.getInstance(this).getAppVersion();
        String currentVersion = Util.getAppVersionCode(this);
        if (currentVersion.equalsIgnoreCase(version) == false)
        {
            DailyPreference.getInstance(this).setAppVersion(currentVersion);
            AnalyticsManager.getInstance(this).currentAppVersion(currentVersion);
        }

        initLayout();

        mNetworkController.requestCheckServer();

        // 3초안에 메인화면이 뜨지 않으면 프로그래스바가 나온다
        mDelayTimeHandler.sendEmptyMessageDelayed(0, 3000);

        // 로그인한 유저와 로그인하지 않은 유저의 판단값이 다르다.
        if (DailyPreference.getInstance(this).isUserBenefitAlarm() == true)
        {
            AnalyticsManager.getInstance(this).setPushEnabled(true, null);
        } else
        {
            AnalyticsManager.getInstance(this).setPushEnabled(false, null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        mOnNetworkControllerListener.onConfigurationResponse();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();

        System.gc();
    }

    private void initLayout()
    {
        setContentView(R.layout.activity_main);

        mSplashLayout = findViewById(R.id.splashLayout);

        loadSplash(mSplashLayout);

        mTooltipLayout = findViewById(R.id.tooltipLayout);

        if (DailyPreference.getInstance(this).isViewWishListTooltip() == true)
        {
            hideAnimationTooltip();
        } else
        {
            DailyPreference.getInstance(this).setIsViewWishListTooltip(true);
            mTooltipLayout.setVisibility(View.VISIBLE);
            mTooltipLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    hideAnimationTooltip();
                }
            });
        }

        ViewGroup bottomMenuBarLayout = (ViewGroup) findViewById(R.id.bottomMenuBarLayout);
        mMenuBarLayout = new MenuBarLayout(this, bottomMenuBarLayout, onMenuBarSelectedListener);

        ViewGroup contentLayout = (ViewGroup) findViewById(R.id.contentLayout);
        mMainFragmentManager = new MainFragmentManager(this, contentLayout, new MenuBarLayout.MenuBarLayoutOnPageChangeListener(mMenuBarLayout));

        mBackButtonHandler = new CloseOnBackPressed(this);
    }

    private void loadSplash(View splashLayout)
    {
        if (splashLayout == null)
        {
            return;
        }

        String splashVersion = DailyPreference.getInstance(this).getRemoteConfigIntroImageVersion();

        DailyImageView imageView = (DailyImageView) splashLayout.findViewById(R.id.splashImageView);

        if (Util.isTextEmpty(splashVersion) == true || Constants.DAILY_INTRO_DEFAULT_VERSION.equalsIgnoreCase(splashVersion) == true)
        {
            imageView.setVectorImageResource(R.drawable.img_splash_logo);
        } else if (Constants.DAILY_INTRO_CURRENT_VERSION.equalsIgnoreCase(splashVersion) == true)
        {
            imageView.setPadding(0, 0, 0, 0);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(R.drawable.splash);
        } else
        {
            String fileName = Util.makeIntroImageFileName(splashVersion);
            File file = new File(getCacheDir(), fileName);

            if (file.exists() == false)
            {
                DailyPreference.getInstance(this).setRemoteConfigIntroImageVersion(Constants.DAILY_INTRO_DEFAULT_VERSION);
                imageView.setVectorImageResource(R.drawable.img_splash_logo);
            } else
            {
                try
                {
                    imageView.setPadding(0, 0, 0, 0);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageURI(Uri.fromFile(file));
                } catch (Exception | OutOfMemoryError e)
                {
                    DailyPreference.getInstance(this).setRemoteConfigIntroImageVersion(Constants.DAILY_INTRO_DEFAULT_VERSION);
                    imageView.setPadding(0, 0, 0, Util.dpToPx(this, 26));
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    imageView.setVectorImageResource(R.drawable.img_splash_logo);
                }
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mIsInitialization == true)
        {
            if (Util.isAvailableNetwork(this) == false)
            {
                mDelayTimeHandler.removeMessages(0);

                showDisabledNetworkPopup();
            }
        } else
        {
            mNetworkController.requestCommonDatetime();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment)
    {
        super.onAttachFragment(fragment);

        releaseUiComponent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_SATISFACTION_HOTEL:
                mNetworkController.requestReviewGourmet();
                break;

            case CODE_REQUEST_ACTIVITY_SATISFACTION_GOURMET:
                mNetworkController.requestNoticeAgreement();
                break;

            // 해당 go home 목록이 MainActivity 목록과 동일해야함.
            case CODE_REQUEST_ACTIVITY_STAY:
            case CODE_REQUEST_ACTIVITY_GOURMET:
            case CODE_REQUEST_ACTIVITY_EVENTWEB:
            case CODE_REQUEST_ACTIVITY_GOURMET_DETAIL:
            case CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            case CODE_REQUEST_ACTIVITY_SEARCH:
            case CODE_REQUEST_ACTIVITY_SEARCH_RESULT:
            case CODE_REQUEST_ACTIVITY_BOOKING_DETAIL:
            case CODE_REQUEST_ACTIVITY_COLLECTION:
            {
                if (mMainFragmentManager == null || mMainFragmentManager.getCurrentFragment() == null)
                {
                    Util.restartApp(this);
                    return;
                }

                if (resultCode == Activity.RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY)
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_BOOKING_FRAGMENT, false);
                } else if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    if (mMainFragmentManager != null)
                    {
                        Fragment fragment = mMainFragmentManager.getCurrentFragment();

                        if (fragment instanceof HomeFragment)
                        {
                            fragment.onActivityResult(requestCode, resultCode, data);
                        } else
                        {
                            mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);
                        }
                    }
                } else
                {
                    mMainFragmentManager.getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_REGIONLIST:
            {
                if (mMainFragmentManager == null || mMainFragmentManager.getCurrentFragment() == null)
                {
                    Util.restartApp(this);
                    return;
                }

                if (data == null && (resultCode == Activity.RESULT_OK || resultCode == CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY))
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_BOOKING_FRAGMENT, false);
                } else
                {
                    mMainFragmentManager.getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                }
                break;
            }

            case Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION:
            case Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER:
            {
                if (mMainFragmentManager == null || mMainFragmentManager.getCurrentFragment() == null)
                {
                    Util.restartApp(this);
                    return;
                }

                mMainFragmentManager.getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                break;
            }

            case Constants.CODE_REQUEST_ACTIVITY_RECENTPLACE:
            {
                unLockUI();

                if (mMainFragmentManager == null || mMainFragmentManager.getCurrentFragment() == null)
                {
                    Util.restartApp(this);
                    return;
                }

                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    case CODE_RESULT_ACTIVITY_PAYMENT_ACCOUNT_READY:
                        mMainFragmentManager.select(MainFragmentManager.INDEX_BOOKING_FRAGMENT, false);
                        break;

                    case CODE_RESULT_ACTIVITY_STAY_LIST:
                        mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);

                        startActivityForResult(StayMainActivity.newInstance(this), Constants.CODE_REQUEST_ACTIVITY_STAY);
                        break;

                    case CODE_RESULT_ACTIVITY_GOURMET_LIST:
                        mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);

                        startActivityForResult(GourmetMainActivity.newInstance(this), Constants.CODE_REQUEST_ACTIVITY_GOURMET);
                        break;

                    // 해당 go home 목록이 HomeFragment 목록과 동일해야함.
                    case Constants.CODE_RESULT_ACTIVITY_GO_HOME:
                        if (mMainFragmentManager != null)
                        {
                            Fragment fragment = mMainFragmentManager.getCurrentFragment();

                            if (fragment instanceof HomeFragment)
                            {
                                fragment.onActivityResult(requestCode, resultCode, data);
                            } else
                            {
                                mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);
                            }
                        }
                        break;

                    default:
                        mMainFragmentManager.getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                        break;
                }
                break;
            }

            // 해당 go home 목록이 HomeFragment 목록과 동일해야함.
            case Constants.CODE_REQUEST_ACTIVITY_ABOUT:
            case Constants.CODE_REQUEST_ACTIVITY_EVENT_LIST:
            case Constants.CODE_REQUEST_ACTIVITY_NOTICE_LIST:
            case Constants.CODE_REQUEST_ACTIVITY_FAQ:
            case Constants.CODE_REQUEST_ACTIVITY_CONTACTUS:
            case Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY:
            case Constants.CODE_REQUEST_ACTIVITY_VIRTUAL_BOOKING_DETAIL:
            {
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    if (mMainFragmentManager != null)
                    {
                        Fragment fragment = mMainFragmentManager.getCurrentFragment();

                        if (fragment instanceof HomeFragment)
                        {
                            fragment.onActivityResult(requestCode, resultCode, data);
                        } else
                        {
                            mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
        {
            return true;
        } else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed()
    {
        int lastIndex = mMainFragmentManager.getLastIndexFragment();

        if (lastIndex == MainFragmentManager.INDEX_HOME_FRAGMENT)
        {
            if (mBackButtonHandler.onBackPressed())
            {
                ExitActivity.exitApplication(this);

                super.onBackPressed();
            }
        } else
        {
            mMainFragmentManager.select(mMainFragmentManager.getLastMainIndexFragment(), false);
        }
    }

    @Override
    protected void onDestroy()
    {
        if (mSettingNetworkDialog != null)
        {
            if (mSettingNetworkDialog.isShowing() == true)
            {
                mSettingNetworkDialog.dismiss();
            }

            mSettingNetworkDialog = null;
        }

        super.onDestroy();
    }

    @Override
    public void onError()
    {
        if (mIsInitialization == false)
        {
            super.onError();

            mMainFragmentManager.select(MainFragmentManager.INDEX_ERROR_FRAGMENT, false);
        }
    }

    void hideAnimationTooltip()
    {
        if (mTooltipLayout.getTag() != null)
        {
            return;
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mTooltipLayout, "alpha", 1.0f, 0.0f);

        mTooltipLayout.setTag(objectAnimator);

        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setDuration(300);
        objectAnimator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animator)
            {

            }

            @Override
            public void onAnimationEnd(Animator animator)
            {
                mTooltipLayout.setTag(null);
                mTooltipLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animator)
            {

            }
        });

        objectAnimator.start();
    }

    void checkAppVersion(final String currentVersion, final String forceVersion)
    {
        if (Util.isTextEmpty(currentVersion, forceVersion) == true)
        {
            mOnNetworkControllerListener.onConfigurationResponse();
            return;
        }

        int appVersion = Integer.parseInt(Util.getAppVersionCode(MainActivity.this).replace(".", ""));
        int skipMaxVersion = Integer.parseInt(DailyPreference.getInstance(MainActivity.this).getSkipVersion().replace(".", ""));
        int forceVersionNumber = Integer.parseInt(forceVersion.replace(".", ""));
        int currentVersionNumber = Integer.parseInt(currentVersion.replace(".", ""));

        boolean isForceUpdate = forceVersionNumber > appVersion;
        boolean isUpdate = currentVersionNumber > appVersion;

        if (isForceUpdate == true)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            View.OnClickListener posListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse(Util.storeReleaseAddress()));

                    if (marketLaunch.resolveActivity(getPackageManager()) == null)
                    {
                        marketLaunch.setData(Uri.parse(Constants.URL_STORE_GOOGLE_DAILYHOTEL_WEB));
                    }

                    startActivity(marketLaunch);
                    finish();
                }
            };

            DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            };

            showSimpleDialog(getString(R.string.label_alarm_update), getString(R.string.dialog_msg_please_update_new_version), getString(R.string.dialog_btn_text_update), posListener, cancelListener);

        } else if (isUpdate == true && skipMaxVersion != currentVersionNumber)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            View.OnClickListener posListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                    marketLaunch.setData(Uri.parse(Util.storeReleaseAddress()));

                    if (marketLaunch.resolveActivity(getPackageManager()) == null)
                    {
                        marketLaunch.setData(Uri.parse(Constants.URL_STORE_GOOGLE_DAILYHOTEL_WEB));
                    }

                    startActivity(marketLaunch);
                    finish();
                }
            };

            final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    DailyPreference.getInstance(MainActivity.this).setSkipVersion(currentVersion);

                    mOnNetworkControllerListener.onConfigurationResponse();
                }
            };

            View.OnClickListener negListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    cancelListener.onCancel(null);
                }
            };

            showSimpleDialog(getString(R.string.label_alarm_update)//
                , getString(R.string.dialog_msg_update_now)//
                , getString(R.string.dialog_btn_text_update)//
                , getString(R.string.dialog_btn_text_cancel)//
                , posListener, negListener, cancelListener, null, false);
        } else
        {
            mOnNetworkControllerListener.onConfigurationResponse();
        }
    }

    void showDisabledNetworkPopup()
    {
        if (isFinishing() == true)
        {
            return;
        }

        mDelayTimeHandler.removeMessages(0);
        unLockUI();

        if (mSettingNetworkDialog != null)
        {
            if (mSettingNetworkDialog.isShowing() == true)
            {
                mSettingNetworkDialog.dismiss();
            }

            mSettingNetworkDialog = null;
        }

        View.OnClickListener positiveListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (Util.isAvailableNetwork(MainActivity.this) == true)
                {
                    lockUI();
                    mNetworkController.requestCheckServer();
                } else
                {
                    mDelayTimeHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            showDisabledNetworkPopup();
                        }
                    }, 100);
                }
            }
        };

        View.OnClickListener negativeListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        };

        mSettingNetworkDialog = createSimpleDialog(getString(R.string.dialog_btn_text_waiting)//
            , getString(R.string.dialog_msg_network_unstable_retry_or_set_wifi)//
            , getString(R.string.dialog_btn_text_retry)//
            , getString(R.string.dialog_btn_text_setting), positiveListener, negativeListener);

        mSettingNetworkDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                finish();
            }
        });

        try
        {
            WindowManager.LayoutParams layoutParams = Util.getDialogWidthLayoutParams(this, mSettingNetworkDialog);

            mSettingNetworkDialog.show();

            mSettingNetworkDialog.getWindow().setAttributes(layoutParams);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }
    }

    void finishSplash()
    {
        mDelayTimeHandler.sendEmptyMessageDelayed(2, 2000);
        mDelayTimeHandler.removeMessages(0);
        mIsInitialization = false;
        mNetworkController.requestCommonDatetime();
    }

    private MenuBarLayout.OnMenuBarSelectedListener onMenuBarSelectedListener = new MenuBarLayout.OnMenuBarSelectedListener()
    {
        @Override
        public void onMenuSelected(int index, int previousIndex)
        {
            if (mMainFragmentManager.getLastIndexFragment() == index || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            // 메뉴가 변경될때마다 이벤트 여부를 체크한다.
            if (mIsInitialization == false)
            {
                mNetworkController.requestCommonDatetime();
            }

            switch (index)
            {
                // 홈
                case 0:
                    mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);

                    if (DailyHotel.isLogin() == true && DailyPreference.getInstance(MainActivity.this).isRequestReview() == false)
                    {
                        DailyPreference.getInstance(MainActivity.this).setIsRequestReview(true);
                        mNetworkController.requestReviewStay();
                    }

                    AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.HOME_CLICK, getIndexName(previousIndex), null);
                    break;

                // 예약내역
                case 1:
                    mMainFragmentManager.select(MainFragmentManager.INDEX_BOOKING_FRAGMENT, false);

                    AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.BOOKINGSTATUS_CLICK, getIndexName(previousIndex), null);
                    break;

                // 마이데일리
                case 2:
                    mMainFragmentManager.select(MainFragmentManager.INDEX_MYDAILY_FRAGMENT, false);

                    AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.MYDAILY_CLICK, getIndexName(previousIndex), null);
                    break;

                // 더보기
                case 3:
                    mMainFragmentManager.select(MainFragmentManager.INDEX_INFORMATION_FRAGMENT, false);

                    AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Category.NAVIGATION//
                        , AnalyticsManager.Action.MENU_CLICK, getIndexName(previousIndex), null);
                    break;
            }
        }

        @Override
        public void onMenuUnselected(int index)
        {
        }

        @Override
        public void onMenuReselected(int index)
        {
            if (isLockUiComponent() == true)
            {
                return;
            }

            switch (index)
            {
                // 홈
                case 0:
                    if (mMainFragmentManager != null)
                    {
                        Fragment fragment = mMainFragmentManager.getCurrentFragment();

                        if (fragment instanceof HomeFragment && fragment.isResumed())
                        {
                            ((HomeFragment) fragment).forceRefreshing();
                        }
                    }
                    break;
            }
        }

        private String getIndexName(int index)
        {
            switch (index)
            {
                case 0:
                    return AnalyticsManager.Label.HOME;

                case 1:
                    return AnalyticsManager.Label.BOOKINGSTATUS;

                case 2:
                    return AnalyticsManager.Label.MYDAILY;

                case 3:
                    return AnalyticsManager.Label.MENU;

                default:
                    return AnalyticsManager.Label.HOME;
            }
        }
    };

    MainNetworkController.OnNetworkControllerListener mOnNetworkControllerListener = new MainNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void updateNewEvent(boolean isNewEvent, boolean isNewCoupon, boolean isNewNotices)
        {
            if (isNewCoupon == true)
            {
                mMenuBarLayout.setMyDailyNewIconVisible(true);
            } else
            {
                mMenuBarLayout.setMyDailyNewIconVisible(false);
            }

            if (isNewEvent == false && isNewNotices == false && Util.hasNoticeNewList(MainActivity.this) == false)
            {
                mMenuBarLayout.setInformationNewIconVisible(false);
            } else
            {
                mMenuBarLayout.setInformationNewIconVisible(true);
            }

            DailyPreference.getInstance(MainActivity.this).setNewEvent(isNewEvent);
            DailyPreference.getInstance(MainActivity.this).setNewCoupon(isNewCoupon);
            DailyPreference.getInstance(MainActivity.this).setNewNotice(isNewNotices);

            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(new Intent(BROADCAST_EVENT_UPDATE));
        }

        @Override
        public void onReviewGourmet(Review review)
        {
            Intent intent = ReviewActivity.newInstance(MainActivity.this, review);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SATISFACTION_GOURMET);
        }

        @Override
        public void onReviewStay(Review review)
        {
            Intent intent = ReviewActivity.newInstance(MainActivity.this, review);
            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SATISFACTION_HOTEL);
        }

        @Override
        public void onError(Throwable e)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            MainActivity.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int magCode, String message)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            MainActivity.this.onErrorPopupMessage(magCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            MainActivity.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            MainActivity.this.onErrorResponse(call, response);
        }

        @Override
        public void onCheckServerResponse(String title, String message)
        {
            mDelayTimeHandler.removeMessages(0);
            unLockUI();

            if (Util.isTextEmpty(title, message) == true)
            {
                DailyRemoteConfig.getInstance(MainActivity.this).requestRemoteConfig(new DailyRemoteConfig.OnCompleteListener()
                {
                    @Override
                    public void onComplete(String currentVersion, String forceVersion)
                    {
                        if (Util.isTextEmpty(currentVersion, forceVersion) == true)
                        {
                            mNetworkController.requestVersion();
                        } else
                        {
                            checkAppVersion(currentVersion, forceVersion);
                        }
                    }
                });
            } else
            {
                showSimpleDialog(title, message, getString(R.string.dialog_btn_text_confirm), null, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }, null, false);
            }
        }

        @Override
        public void onAppVersionResponse(String currentVersion, String forceVersion)
        {
            checkAppVersion(currentVersion, forceVersion);
        }

        @Override
        public void onConfigurationResponse()
        {
            lockUI(false);

            finishSplash();

            if (DailyDeepLink.getInstance().isValidateLink() == true)
            {
                if (DailyDeepLink.getInstance().isHomeEventDetailView() == true//
                    || DailyDeepLink.getInstance().isHomeRecommendationPlaceListView() == true//
                    || DailyDeepLink.getInstance().isHotelView() == true//
                    || DailyDeepLink.getInstance().isGourmetView() == true//
                    || DailyDeepLink.getInstance().isRecentlyWatchHotelView() == true//
                    || DailyDeepLink.getInstance().isRecentlyWatchGourmetView() == true//
                    || DailyDeepLink.getInstance().isWishListHotelView() == true//
                    || DailyDeepLink.getInstance().isWishListGourmetView() == true//
                    )
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, true);

                } else if (DailyDeepLink.getInstance().isBookingView() == true //
                    || DailyDeepLink.getInstance().isBookingDetailView() == true)
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_BOOKING_FRAGMENT, true);
                } else if (DailyDeepLink.getInstance().isMyDailyView() == true //
                    || DailyDeepLink.getInstance().isBonusView() == true//
                    || DailyDeepLink.getInstance().isCouponView() == true //
                    || DailyDeepLink.getInstance().isRecommendFriendView() == true //
                    || DailyDeepLink.getInstance().isRegisterCouponView() == true //
                    || DailyDeepLink.getInstance().isProfileView() == true//
                    || DailyDeepLink.getInstance().isProfileBirthdayView() == true//
                    )
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_MYDAILY_FRAGMENT, true);
                } else if (DailyDeepLink.getInstance().isSingUpView() == true)
                {
                    if (DailyHotel.isLogin() == false)
                    {
                        mMainFragmentManager.select(MainFragmentManager.INDEX_MYDAILY_FRAGMENT, true);
                    } else
                    {
                        DailyDeepLink.getInstance().clear();
                        mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, true);
                    }
                } else if (DailyDeepLink.getInstance().isEventView() == true//
                    || DailyDeepLink.getInstance().isEventDetailView() == true//
                    || DailyDeepLink.getInstance().isInformationView() == true //
                    || DailyDeepLink.getInstance().isNoticeDetailView() == true//
                    || DailyDeepLink.getInstance().isFAQView() == true//
                    || DailyDeepLink.getInstance().isTermsNPolicyView() == true//
                    )
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_INFORMATION_FRAGMENT, true);
                } else
                {
                    mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, true);
                }

                AnalyticsManager.getInstance(MainActivity.this).startApplication();
            } else
            {
                mMainFragmentManager.select(MainFragmentManager.INDEX_HOME_FRAGMENT, false);

                if (DailyHotel.isLogin() == true)
                {
                    // session alive
                    // 호텔 평가를 위한 사용자 정보 조회
                    // 해당 경우는 Analytics 의 startApplication을 해당 메소드의 onResponse에서 처리
                    mNetworkController.requestUserInformation();
                } else
                {
                    // 헤택이 Off 되어있는 경우 On으로 수정
                    boolean isUserBenefitAlarm = DailyPreference.getInstance(MainActivity.this).isUserBenefitAlarm();
                    if (isUserBenefitAlarm == false//
                        && DailyPreference.getInstance(MainActivity.this).isShowBenefitAlarm() == false)
                    {
                        mNetworkController.requestNoticeAgreement();
                        AnalyticsManager.getInstance(MainActivity.this).setPushEnabled(isUserBenefitAlarm, null);
                    } else
                    {
                        AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Screen.APP_LAUNCHED, null, null, null);
                        AnalyticsManager.getInstance(MainActivity.this).setPushEnabled(isUserBenefitAlarm, null);
                    }

                    AnalyticsManager.getInstance(MainActivity.this).startApplication();
                }

                // 10초 후에 터치가 없으면 자동으로 사라짐.(기획서상 10초이지만 실제 보이기까지 여분의 시간을 넣음)
                mDelayTimeHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mTooltipLayout.getVisibility() != View.GONE)
                        {
                            hideAnimationTooltip();
                        }
                    }
                }, 10000);
            }
        }

        @Override
        public void onNoticeAgreement(String message, boolean isFirstTimeBuyer)
        {
            if (DailyPreference.getInstance(MainActivity.this).isUserBenefitAlarm() == true)
            {
                AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Screen.APP_LAUNCHED, null, null, null);
                return;
            }

            final boolean isLogined = DailyHotel.isLogin();

            if (isLogined == true)
            {
                if (isFirstTimeBuyer == true && DailyPreference.getInstance(MainActivity.this).isShowBenefitAlarmFirstBuyer() == false)
                {
                    DailyPreference.getInstance(MainActivity.this).setShowBenefitAlarmFirstBuyer(true);
                } else if (DailyPreference.getInstance(MainActivity.this).isShowBenefitAlarm() == false)
                {

                } else
                {
                    AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Screen.APP_LAUNCHED, null, null, null);
                    return;
                }
            } else
            {
                if (DailyPreference.getInstance(MainActivity.this).isShowBenefitAlarm() == true)
                {
                    AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Screen.APP_LAUNCHED, null, null, null);
                    return;
                }
            }

            // 혜택
            showSimpleDialogType01(getString(R.string.label_setting_alarm), message, getString(R.string.label_now_setting_alarm), getString(R.string.label_after_setting_alarm)//
                , new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mIsBenefitAlarm = true;
                        mNetworkController.requestNoticeAgreementResult(true);
                    }
                }, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mIsBenefitAlarm = false;
                        mNetworkController.requestNoticeAgreementResult(false);
                    }
                }, new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        mIsBenefitAlarm = false;
                        mNetworkController.requestNoticeAgreementResult(false);
                    }
                }, null, true);
        }

        @Override
        public void onNoticeAgreementResult(final String agreeMessage, final String cancelMessage)
        {
            DailyPreference.getInstance(MainActivity.this).setShowBenefitAlarm(true);
            DailyPreference.getInstance(MainActivity.this).setUserBenefitAlarm(mIsBenefitAlarm);
            AnalyticsManager.getInstance(MainActivity.this).setPushEnabled(mIsBenefitAlarm, AnalyticsManager.ValueType.LAUNCH);

            if (mIsBenefitAlarm == true)
            {
                AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES//
                    , AnalyticsManager.Action.FIRST_NOTIFICATION_SETTING_CLICKED, AnalyticsManager.Label.ON, null);

                showSimpleDialog(getString(R.string.label_setting_alarm), agreeMessage, getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Screen.APP_LAUNCHED, null, null, null);
                    }
                });
            } else
            {
                AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Category.POPUP_BOXES//
                    , AnalyticsManager.Action.FIRST_NOTIFICATION_SETTING_CLICKED, AnalyticsManager.Label.OFF, null);

                showSimpleDialog(getString(R.string.label_setting_alarm), cancelMessage, getString(R.string.dialog_btn_text_confirm), null, new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        AnalyticsManager.getInstance(MainActivity.this).recordEvent(AnalyticsManager.Screen.APP_LAUNCHED, null, null, null);
                    }
                });
            }
        }

        @Override
        public void onCommonDateTime(long currentDateTime, long dailyDateTime, long openDateTime, long closeDateTime)
        {
            try
            {
                // 요청하면서 CS운영시간도 같이 받아온다.
                String startHour = DailyCalendar.format(openDateTime, "H", TimeZone.getTimeZone("GMT"));
                String endtHour = DailyCalendar.format(closeDateTime, "H", TimeZone.getTimeZone("GMT"));

                DailyPreference.getInstance(MainActivity.this).setOperationTime(String.format("%s,%s", startHour, endtHour));
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }

            String viewedEventTime = DailyPreference.getInstance(MainActivity.this).getViewedEventTime();
            String viewedCouponTime = DailyPreference.getInstance(MainActivity.this).getViewedCouponTime();
            String viewedNoticeTime = DailyPreference.getInstance(MainActivity.this).getViewedNoticeTime();

            currentDateTime -= 3600 * 1000 * 9;

            Calendar calendar = DailyCalendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+09:00"));
            calendar.setTimeInMillis(currentDateTime);

            //            String lastestTime = Util.getISO8601String(calendar.getTime());
            String lastestTime = DailyCalendar.format(calendar.getTime(), DailyCalendar.ISO_8601_FORMAT);

            DailyPreference.getInstance(MainActivity.this).setLastestEventTime(lastestTime);
            DailyPreference.getInstance(MainActivity.this).setLastestCouponTime(lastestTime);
            DailyPreference.getInstance(MainActivity.this).setLastestNoticeTime(lastestTime);

            if (Util.isTextEmpty(viewedEventTime) == true)
            {
                viewedEventTime = DailyCalendar.format(new Date(0L), DailyCalendar.ISO_8601_FORMAT);
            }

            if (Util.isTextEmpty(viewedCouponTime) == true)
            {
                viewedCouponTime = DailyCalendar.format(new Date(0L), DailyCalendar.ISO_8601_FORMAT);
            }

            if (Util.isTextEmpty(viewedNoticeTime) == true)
            {
                viewedNoticeTime = DailyCalendar.format(new Date(0L), DailyCalendar.ISO_8601_FORMAT);
            }

            Calendar dailyCalendar = DailyCalendar.getInstance();
            dailyCalendar.setTimeInMillis(dailyDateTime - 3600 * 1000 * 9);

            String startDay = DailyCalendar.format(dailyCalendar.getTime(), "yyyy-MM-dd");

            // 같은날짜에는 중복으로 요청하지 않는다.
            if (startDay.equalsIgnoreCase(DailyPreference.getInstance(MainActivity.this).getCheckCalendarHolidays()) == false)
            {
                // 90일을 미리 얻어온다.
                dailyCalendar.add(Calendar.DAY_OF_MONTH, 90);
                String endDay = DailyCalendar.format(dailyCalendar.getTime(), "yyyy-MM-dd");

                mNetworkController.requestHoliday(startDay, endDay);
            }

            mNetworkController.requestEventNCouponNNoticeNewCount(viewedEventTime, viewedCouponTime, viewedNoticeTime);
        }

        @Override
        public void onUserProfileBenefit(boolean isExceedBonus)
        {
            DailyPreference.getInstance(MainActivity.this).setUserExceedBonus(isExceedBonus);
            AnalyticsManager.getInstance(MainActivity.this).setExceedBonus(isExceedBonus);

            mNetworkController.requestReviewStay();

            DailyPreference.getInstance(MainActivity.this).setIsRequestReview(true);
        }

        @Override
        public void onHolidays(String startDay, String holidays)
        {
            DailyPreference.getInstance(MainActivity.this).setCheckCalendarHolidays(startDay);
            DailyPreference.getInstance(MainActivity.this).setCalendarHolidays(holidays);
        }
    };
}
