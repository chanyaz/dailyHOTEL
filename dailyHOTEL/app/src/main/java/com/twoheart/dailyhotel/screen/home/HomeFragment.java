package com.twoheart.dailyhotel.screen.home;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.ScreenUtils;
import com.daily.dailyhotel.entity.CarouselListItem;
import com.daily.dailyhotel.entity.CommonDateTime;
import com.daily.dailyhotel.entity.ImageMap;
import com.daily.dailyhotel.entity.People;
import com.daily.dailyhotel.entity.RecentlyPlace;
import com.daily.dailyhotel.entity.StayBookDateTime;
import com.daily.dailyhotel.entity.StayOutbound;
import com.daily.dailyhotel.entity.StayOutbounds;
import com.daily.dailyhotel.parcel.analytics.GourmetDetailAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayDetailAnalyticsParam;
import com.daily.dailyhotel.parcel.analytics.StayOutboundDetailAnalyticsParam;
import com.daily.dailyhotel.repository.local.RecentlyLocalImpl;
import com.daily.dailyhotel.repository.remote.CommonRemoteImpl;
import com.daily.dailyhotel.repository.remote.RecentlyRemoteImpl;
import com.daily.dailyhotel.screen.home.gourmet.detail.GourmetDetailActivity;
import com.daily.dailyhotel.screen.home.stay.inbound.detail.StayDetailActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.detail.StayOutboundDetailActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.preview.StayOutboundPreviewActivity;
import com.daily.dailyhotel.screen.home.stay.outbound.search.StayOutboundSearchActivity;
import com.daily.dailyhotel.storage.database.DailyDb;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twoheart.dailyhotel.DailyHotel;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.DailyCategoryType;
import com.twoheart.dailyhotel.model.Province;
import com.twoheart.dailyhotel.model.time.GourmetBookingDay;
import com.twoheart.dailyhotel.model.time.StayBookingDay;
import com.twoheart.dailyhotel.network.model.Event;
import com.twoheart.dailyhotel.network.model.Recommendation;
import com.twoheart.dailyhotel.network.model.TodayDateTime;
import com.twoheart.dailyhotel.place.base.BaseActivity;
import com.twoheart.dailyhotel.place.base.BaseMenuNavigationFragment;
import com.twoheart.dailyhotel.screen.common.PermissionManagerActivity;
import com.twoheart.dailyhotel.screen.event.EventWebActivity;
import com.twoheart.dailyhotel.screen.gourmet.list.GourmetMainActivity;
import com.twoheart.dailyhotel.screen.gourmet.preview.GourmetPreviewActivity;
import com.twoheart.dailyhotel.screen.home.category.list.StayCategoryTabActivity;
import com.twoheart.dailyhotel.screen.home.category.nearby.StayCategoryNearByActivity;
import com.twoheart.dailyhotel.screen.home.category.region.HomeCategoryRegionListActivity;
import com.twoheart.dailyhotel.screen.home.collection.CollectionGourmetActivity;
import com.twoheart.dailyhotel.screen.home.collection.CollectionStayActivity;
import com.twoheart.dailyhotel.screen.hotel.list.StayMainActivity;
import com.twoheart.dailyhotel.screen.hotel.preview.StayPreviewActivity;
import com.twoheart.dailyhotel.screen.information.terms.LocationTermsActivity;
import com.twoheart.dailyhotel.screen.information.terms.PrivacyActivity;
import com.twoheart.dailyhotel.screen.information.terms.ProtectYouthTermsActivity;
import com.twoheart.dailyhotel.screen.information.terms.TermActivity;
import com.twoheart.dailyhotel.screen.mydaily.member.SignupStep1Activity;
import com.twoheart.dailyhotel.screen.mydaily.recentplace.RecentPlacesTabActivity;
import com.twoheart.dailyhotel.screen.mydaily.wishlist.WishListTabActivity;
import com.twoheart.dailyhotel.screen.search.SearchActivity;
import com.twoheart.dailyhotel.screen.search.stay.result.StaySearchResultActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyExternalDeepLink;
import com.twoheart.dailyhotel.util.DailyInternalDeepLink;
import com.twoheart.dailyhotel.util.DailyLocationFactory;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by android_sam on 2017. 1. 11..
 */

public class HomeFragment extends BaseMenuNavigationFragment
{
    private static final int MAX_REQUEST_SIZE = 15;
    private static final int MAX_RESPONSE_SIZE = 10;

    private static final int IS_RUNNED_NONE = 0;
    private static final int IS_RUNNED_WISHLIST = 1 << 1;
    private static final int IS_RUNNED_RECENTLIST = 1 << 2;

    HomeLayout mHomeLayout;
    BaseActivity mBaseActivity;
    PlaceType mPlaceType = PlaceType.HOTEL;
    private HomeNetworkController mNetworkController;
    TodayDateTime mTodayDateTime;
    boolean mIsAttach;
    boolean mDontReload;
    private boolean mIsLogin;

    int mNetworkRunState = IS_RUNNED_NONE; // 0x0000 : 초기 상태, Ox0010 : 위시 완료 , Ox0100 : 최근 본 업장완료!

    private DailyDeepLink mDailyDeepLink;

    View mViewByLongPress;
    CarouselListItem mCarouselListItemByLongPress;
    DailyLocationFactory mDailyLocationFactory;

    private RecentlyRemoteImpl mRecentlyRemoteImpl;
    private RecentlyLocalImpl mRecentlyLocalImpl;

    private CommonRemoteImpl mCommonRemoteImpl;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCommonRemoteImpl = new CommonRemoteImpl(getActivity());
        mRecentlyRemoteImpl = new RecentlyRemoteImpl(getActivity());
        mRecentlyLocalImpl = new RecentlyLocalImpl(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mBaseActivity = (BaseActivity) getActivity();

        mIsLogin = DailyHotel.isLogin();
        mHomeLayout = new HomeLayout(mBaseActivity, mOnEventListener);
        mHomeLayout.setOnScrollChangedListener(new OnScreenScrollChangeListener()
        {
            @Override
            public void onScrollChange(ViewGroup scrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
            {
                if (mHomeLayout == null)
                {
                    return;
                }

                if (mOnScreenScrollChangeListener != null)
                {
                    if (mHomeLayout.getErrorPopupVisible() == true)
                    {
                        mOnScreenScrollChangeListener.onScrollState(true);
                        return;
                    }

                    mOnScreenScrollChangeListener.onScrollChange(scrollView, scrollX, scrollY, oldScrollX, oldScrollY);
                }
            }

            @Override
            public void onScrollState(boolean disabled)
            {
                if (mOnScreenScrollChangeListener != null)
                {
                    mOnScreenScrollChangeListener.onScrollState(disabled);
                }
            }
        });

        mNetworkController = new HomeNetworkController(mBaseActivity, mNetworkTag, mNetworkControllerListener);

        return mHomeLayout.onCreateView(R.layout.fragment_home_main, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mIsAttach = true;
    }

    @Override
    public void onNewBundle(Bundle bundle)
    {
        if (bundle == null)
        {
            return;
        }

        if (bundle.containsKey(Constants.NAME_INTENT_EXTRA_DATA_DEEPLINK) == true)
        {
            try
            {
                mDailyDeepLink = DailyDeepLink.getNewInstance(Uri.parse(bundle.getString(Constants.NAME_INTENT_EXTRA_DATA_DEEPLINK)));
            } catch (Exception e)
            {
                mDailyDeepLink = null;
            }
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        moveDeepLink(mDailyDeepLink);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mHomeLayout != null && mHomeLayout.getBlurVisibility() == true)
        {
            mHomeLayout.setBlurVisibility(mBaseActivity, false);

            mHomeLayout.resumeNextEventPosition();
        } else
        {
            refreshList(true);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mDontReload = true;

        if (mHomeLayout != null)
        {
            mHomeLayout.clearNextEventPosition();
        }
    }

    @Override
    public void onDestroy()
    {
        if (mHomeLayout != null)
        {
            mHomeLayout.removeOnLayoutChangeListener();
        }

        clearCompositeDisposable();

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        unLockUI();

        switch (requestCode)
        {
            case CODE_REQUEST_ACTIVITY_LOGIN:
                if (resultCode == Activity.RESULT_OK)
                {
                }
                break;

            case CODE_REQEUST_ACTIVITY_SIGNUP:
            {
                break;
            }

            case CODE_REQUEST_ACTIVITY_SATISFACTION_HOTEL:
            case CODE_REQUEST_ACTIVITY_SATISFACTION_GOURMET:
            {
                mDontReload = true;

                if (mHomeLayout != null)
                {
                    mHomeLayout.setScrollTop();
                }

                forceRefreshing();
                break;
            }

            // 해당 go home 목록이 MainActivity 목록과 동일해야함.
            case Constants.CODE_REQUEST_ACTIVITY_STAY:
            case Constants.CODE_REQUEST_ACTIVITY_GOURMET:
            case Constants.CODE_REQUEST_ACTIVITY_EVENTWEB:
            case Constants.CODE_REQUEST_ACTIVITY_GOURMET_DETAIL:
            case Constants.CODE_REQUEST_ACTIVITY_STAY_DETAIL:
            case Constants.CODE_REQUEST_ACTIVITY_SEARCH:
            case Constants.CODE_REQUEST_ACTIVITY_SEARCH_RESULT:
            case Constants.CODE_REQUEST_ACTIVITY_BOOKING_DETAIL:
            case Constants.CODE_REQUEST_ACTIVITY_COLLECTION:
            case Constants.CODE_REQUEST_ACTIVITY_RECENTPLACE:
            case Constants.CODE_REQUEST_ACTIVITY_GUIDE:
            case Constants.CODE_REQUEST_ACTIVITY_ABOUT:
            case Constants.CODE_REQUEST_ACTIVITY_SNS:
            case Constants.CODE_REQUEST_ACTIVITY_LIFESTYLE:
            case Constants.CODE_REQUEST_ACTIVITY_EVENT_LIST:
            case Constants.CODE_REQUEST_ACTIVITY_NOTICE_LIST:
            case Constants.CODE_REQUEST_ACTIVITY_FAQ:
            case Constants.CODE_REQUEST_ACTIVITY_CONTACTUS:
            case Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY:
            case Constants.CODE_REQUEST_ACTIVITY_VIRTUAL_BOOKING_DETAIL:
            case Constants.CODE_REQUEST_ACTIVITY_COUPONLIST:
            case Constants.CODE_REQUEST_ACTIVITY_BONUS:
            case Constants.CODE_REQUEST_ACTIVITY_STAMP:
            case Constants.CODE_REQUEST_ACTIVITY_STAY_OB_DETAIL:
                if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    mDontReload = true;
                    mHomeLayout.setScrollTop();

                    forceRefreshing();
                } else if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_SEARCH)
                {
                    onSearchClick();
                } else if (resultCode == Constants.CODE_RESULT_ACTIVITY_GO_REGION_LIST)
                {
                    if (data != null && data.hasExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE) == true)
                    {
                        DailyCategoryType categoryType = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE);
                        mOnEventListener.onCategoryItemClick(categoryType);
                    }
                }
                break;

            case Constants.CODE_REQUEST_ACTIVITY_REGIONLIST:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    if (data != null && data.hasExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE) == true)
                    {
                        Province province = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_PROVINCE);
                        DailyCategoryType categoryType = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE);

                        DailyPreference.getInstance(mBaseActivity).setDailyRegion(categoryType, Util.getDailyRegionJSONObject(province));

                        //                        new Handler().postDelayed(new Runnable()
                        //                        {
                        //                            @Override
                        //                            public void run()
                        //                            {
                        //                                Intent intent = StayCategoryTabActivity.newInstance(mBaseActivity, categoryType, null);
                        //                                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_STAY);
                        //                            }
                        //                        }, 50);

                        try
                        {
                            Intent intent = StayCategoryTabActivity.newInstance(mBaseActivity, categoryType, null);
                            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_STAY);
                        } catch (Exception e)
                        {
                            ExLog.e(e.toString());
                        }

                    }
                } else if (resultCode == RESULT_ARROUND_SEARCH_LIST && data != null)
                {
                    // 검색 결과 화면으로 이동한다.
                    DailyCategoryType dailyCategoryType = data.getParcelableExtra(NAME_INTENT_EXTRA_DATA_DAILY_CATEGORY_TYPE);

                    try
                    {
                        StayBookingDay stayBookingDay = new StayBookingDay();
                        stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
                        stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

                        Intent intent = StayCategoryNearByActivity.newInstance(mBaseActivity //
                            , mTodayDateTime, stayBookingDay, null, dailyCategoryType, AnalyticsManager.Screen.HOME);
                        startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_SEARCH_RESULT);
                    } catch (Exception e)
                    {
                        ExLog.e(e.toString());
                    }
                }
                break;
            }

            case Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION:
            {
                checkLocationProvider();
                break;
            }

            case Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    checkLocationProvider();
                } else if (resultCode == CODE_RESULT_ACTIVITY_GO_HOME)
                {
                    mDontReload = true;
                    mHomeLayout.setScrollTop();

                    forceRefreshing();
                }
                break;
            }

            case CODE_REQUEST_ACTIVITY_PREVIEW:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    Observable.create(new ObservableOnSubscribe<Object>()
                    {
                        @Override
                        public void subscribe(ObservableEmitter<Object> e) throws Exception
                        {
                            if (CarouselListItem.TYPE_OB_STAY == mCarouselListItemByLongPress.mType)
                            {
                                // stayOutbound
                                startStayOutboundDetail(mViewByLongPress, mCarouselListItemByLongPress, mTodayDateTime);
                            } else
                            {
                                startPlaceDetail(mViewByLongPress, mCarouselListItemByLongPress, mTodayDateTime);
                            }
                        }
                    }).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                }
                break;
            }
        }
    }

    private void moveDeepLink(DailyDeepLink dailyDeepLink)
    {
        if (dailyDeepLink == null || dailyDeepLink.isValidateLink() == false)
        {
            return;
        }

        if (dailyDeepLink.isExternalDeepLink() == true)
        {
            DailyExternalDeepLink externalDeepLink = (DailyExternalDeepLink) dailyDeepLink;

            if (externalDeepLink.isHomeEventDetailView() == true)
            {
                startEventWebActivity(externalDeepLink.getUrl(), externalDeepLink.getTitle());
            } else if (externalDeepLink.isHomeRecommendationPlaceListView() == true)
            {
                String serviceType = externalDeepLink.getPlaceType();

                try
                {
                    int index = Integer.parseInt(externalDeepLink.getIndex());
                    String date = externalDeepLink.getDate();
                    int datePlus = externalDeepLink.getDatePlus();

                    switch (serviceType)
                    {
                        case "gourmet":
                            if (DailyTextUtils.isTextEmpty(date) == false)
                            {
                                GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();
                                Date visitDate = DailyCalendar.convertDate(date, "yyyyMMdd", TimeZone.getTimeZone("GMT+09:00"));
                                gourmetBookingDay.setVisitDay(DailyCalendar.format(visitDate, DailyCalendar.ISO_8601_FORMAT));

                                startDeepLinkRecommendationGourmetActivity(index, gourmetBookingDay.getVisitDay(DailyCalendar.ISO_8601_FORMAT), -1);
                            } else
                            {
                                startDeepLinkRecommendationGourmetActivity(index, null, datePlus);
                            }
                            break;

                        case "stay":
                        default:
                            int nights = 1;

                            try
                            {
                                nights = Integer.parseInt(externalDeepLink.getNights());
                            } catch (Exception e)
                            {
                                ExLog.d(e.toString());
                            } finally
                            {
                                if (nights <= 0)
                                {
                                    nights = 1;
                                }
                            }

                            if (DailyTextUtils.isTextEmpty(date) == false)
                            {
                                StayBookingDay stayBookingDay = new StayBookingDay();
                                Date checkInDate = DailyCalendar.convertDate(date, "yyyyMMdd", TimeZone.getTimeZone("GMT+09:00"));
                                stayBookingDay.setCheckInDay(DailyCalendar.format(checkInDate, DailyCalendar.ISO_8601_FORMAT));
                                stayBookingDay.setCheckOutDay(stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT), nights);

                                startDeepLinkRecommendationStayActivity(index, stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                                    , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT), -1, -1);
                            } else
                            {
                                startDeepLinkRecommendationStayActivity(index, null, null, datePlus, nights);
                            }
                            break;
                    }
                } catch (Exception e)
                {
                    ExLog.e(e.toString());
                }
            } else if (externalDeepLink.isHotelListView() == true//
                || externalDeepLink.isHotelDetailView() == true//
                || externalDeepLink.isHotelSearchView() == true//
                || externalDeepLink.isHotelSearchResultView() == true)
            {
                onStayClick(true, externalDeepLink);
            } else if (externalDeepLink.isGourmetListView() == true//
                || externalDeepLink.isGourmetDetailView() == true//
                || externalDeepLink.isGourmetSearchView() == true//
                || externalDeepLink.isGourmetSearchResultView() == true)
            {
                onGourmetClick(true, externalDeepLink);
            } else if (externalDeepLink.isRecentlyWatchHotelView() == true)
            {
                startRecentList(PlaceType.HOTEL);
            } else if (externalDeepLink.isRecentlyWatchGourmetView() == true)
            {
                startRecentList(PlaceType.FNB);
            } else if (externalDeepLink.isWishListHotelView() == true)
            {
                startWishList(PlaceType.HOTEL);
            } else if (externalDeepLink.isWishListGourmetView() == true)
            {
                startWishList(PlaceType.FNB);
            } else if (externalDeepLink.isStampView() == true)
            {
                if (DailyRemoteConfigPreference.getInstance(mBaseActivity).isRemoteConfigStampStayEndEventPopupEnabled() == true)
                {
                    mBaseActivity.showSimpleDialog(null, getString(R.string.message_stamp_finish_stamp), getString(R.string.dialog_btn_text_confirm), null);
                }
            } else if (externalDeepLink.isShortcutView() == true)
            {
                String categoryCode = externalDeepLink.getCategoryCode();
                if (DailyTextUtils.isTextEmpty(categoryCode) == false)
                {
                    DailyCategoryType dailyCategoryType = null;

                    if (categoryCode.equalsIgnoreCase(DailyCategoryType.STAY_HOTEL.getCodeString(mBaseActivity)) == true)
                    {
                        dailyCategoryType = DailyCategoryType.STAY_HOTEL;
                    } else if (categoryCode.equalsIgnoreCase(DailyCategoryType.STAY_BOUTIQUE.getCodeString(mBaseActivity)) == true)
                    {
                        dailyCategoryType = DailyCategoryType.STAY_BOUTIQUE;
                    } else if (categoryCode.equalsIgnoreCase(DailyCategoryType.STAY_PENSION.getCodeString(mBaseActivity)) == true)
                    {
                        dailyCategoryType = DailyCategoryType.STAY_PENSION;
                    } else if (categoryCode.equalsIgnoreCase(DailyCategoryType.STAY_RESORT.getCodeString(mBaseActivity)) == true)
                    {
                        dailyCategoryType = DailyCategoryType.STAY_RESORT;
                    }

                    if (dailyCategoryType != null)
                    {
                        try
                        {
                            Intent intent = StayCategoryTabActivity.newInstance(mBaseActivity, dailyCategoryType, dailyDeepLink.getDeepLink());
                            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_STAY);
                        } catch (Exception e)
                        {
                            ExLog.e(e.toString());
                        }
                    }
                }
            } else if (externalDeepLink.isStayOutboundSearchResultView() == true)
            {
                try
                {
                    Intent intent = StayOutboundSearchActivity.newInstance(mBaseActivity, dailyDeepLink.getDeepLink());
                    startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_STAY_OUTBOUND_SEARCH);
                } catch (Exception e)
                {
                    ExLog.e(e.toString());
                }
            } else if (externalDeepLink.isCampaignTagListView() == true)
            {
                String placeType = externalDeepLink.getPlaceType();
                if (DailyTextUtils.isTextEmpty(placeType) == false)
                {
                    switch (externalDeepLink.getPlaceType())
                    {
                        case DailyDeepLink.GOURMET:
                            onGourmetClick(true, externalDeepLink);

                            break;
                        case DailyDeepLink.STAY:
                            onStayClick(true, externalDeepLink);
                    }
                }
            } else if (externalDeepLink.isPlaceDetailView() == true)
            {
                if (DailyDeepLink.STAY_OUTBOUND.equalsIgnoreCase(externalDeepLink.getPlaceType()) == true)
                {
                    try
                    {
                        Intent intent = StayOutboundSearchActivity.newInstance(mBaseActivity, dailyDeepLink.getDeepLink());
                        startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_STAY_OUTBOUND_SEARCH);
                    } catch (Exception e)
                    {
                        ExLog.e(e.toString());
                    }
                }
            }
        } else
        {
            DailyInternalDeepLink internalDeepLink = (DailyInternalDeepLink) mDailyDeepLink;

            if (internalDeepLink.isStayOutboundSearchSuggestView() == true)
            {
                try
                {
                    Intent intent = StayOutboundSearchActivity.newInstance(mBaseActivity, dailyDeepLink.getDeepLink());
                    startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_STAY_OUTBOUND_SEARCH);
                } catch (Exception e)
                {
                    ExLog.e(e.toString());
                }
            }
        }

        dailyDeepLink.clear();
    }


    private void requestMessageData()
    {
        if (mHomeLayout == null)
        {
            return;
        }

        HomeLayout.MessageType messageType = HomeLayout.MessageType.NONE;

        if (mIsLogin == false)
        {
            boolean isLogoutAreaEnable = DailyRemoteConfigPreference.getInstance(mBaseActivity).isRemoteConfigHomeMessageAreaLogoutEnabled();
            boolean isTextMessageAreaEnable = DailyPreference.getInstance(mBaseActivity).isHomeTextMessageAreaEnabled();

            if (isLogoutAreaEnable == true && isTextMessageAreaEnable == true)
            {
                messageType = HomeLayout.MessageType.TEXT;
            }
        }

        if (HomeLayout.MessageType.TEXT == messageType)
        {
            requestTextMessage();
        } else
        {
            mHomeLayout.hideMessageLayout();
        }
    }

    private void requestTextMessage()
    {
        String title = DailyRemoteConfigPreference.getInstance(mBaseActivity).getRemoteConfigHomeMessageAreaLogoutTitle();
        String description = DailyRemoteConfigPreference.getInstance(mBaseActivity).getRemoteConfigHomeMessageAreaLogoutCallToAction();

        if (mHomeLayout != null)
        {
            mHomeLayout.setTextMessageData(title, description);

            if (DailyTextUtils.isTextEmpty(title) == false || DailyTextUtils.isTextEmpty(description) == false)
            {
                AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                    AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_MESSAGE_OPEN,//
                    AnalyticsManager.Label.SIGNUP, null);
            }
        }
    }

    private void requestCategoryEnabled()
    {
        boolean isEnabled = DailyRemoteConfigPreference.getInstance(mBaseActivity).getRemoteConfigHomeCategoryEnabled();

        if (mHomeLayout != null)
        {
            mHomeLayout.setCategoryEnabled(isEnabled);
        }
    }

    void checkStayOutboundCutNewVisible(TodayDateTime todayDateTime)
    {
        if (todayDateTime == null)
        {
            return;
        }

        final String OVER_TIME = "overTime";
        final int VIEW_TIME = 14; // 14일

        String visibleDate = DailyPreference.getInstance(mBaseActivity).getHomeShortCutStayOutboundNewDate();

        // 해외 호텔 new 표시
        if (DailyTextUtils.isTextEmpty(visibleDate) == true)
        {
            DailyPreference.getInstance(mBaseActivity).setHomeShortCutStayOutboundNewDate(todayDateTime.currentDateTime);

            mHomeLayout.setCategoryStayOutboundNewVisible(true);
        } else if (OVER_TIME.equalsIgnoreCase(visibleDate) == true)
        {
            mHomeLayout.setCategoryStayOutboundNewVisible(false);
        } else
        {
            try
            {
                Calendar currentCalendar = DailyCalendar.getInstance();
                currentCalendar.setTime(DailyCalendar.convertDate(todayDateTime.currentDateTime, DailyCalendar.ISO_8601_FORMAT));

                Calendar visibleCalendar = DailyCalendar.getInstance();
                visibleCalendar.setTime(DailyCalendar.convertDate(visibleDate, DailyCalendar.ISO_8601_FORMAT));
                visibleCalendar.add(Calendar.DAY_OF_MONTH, VIEW_TIME);

                if (currentCalendar.before(visibleCalendar) == true)
                {
                    mHomeLayout.setCategoryStayOutboundNewVisible(true);
                } else
                {
                    mHomeLayout.setCategoryStayOutboundNewVisible(false);
                    DailyPreference.getInstance(mBaseActivity).setHomeShortCutStayOutboundNewDate(OVER_TIME);
                }
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }
    }

    void startSignUp(String recommenderCode)
    {
        if (isLockUiComponent() == true || mIsAttach == false)
        {
            return;
        }

        lockUiComponent();

        BaseActivity baseActivity = (BaseActivity) getActivity();

        Intent intent;

        if (DailyTextUtils.isTextEmpty(recommenderCode) == true)
        {
            intent = SignupStep1Activity.newInstance(baseActivity, null);
        } else
        {
            intent = SignupStep1Activity.newInstance(baseActivity, recommenderCode, null);
        }

        startActivityForResult(intent, CODE_REQEUST_ACTIVITY_SIGNUP);
    }

    void startEventWebActivity(String url, String eventName)
    {
        if (DailyTextUtils.isTextEmpty(url) == true)
        {
            return;
        }

        Intent intent = EventWebActivity.newInstance(mBaseActivity, EventWebActivity.SourceType.HOME_EVENT, url, eventName);
        mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_EVENTWEB);
    }

    private void startDeepLinkRecommendationStayActivity(int index, String checkInDateTime, String checkOutDateTime, int afterDay, int nights)
    {
        Intent intent;

        if (DailyTextUtils.isTextEmpty(checkInDateTime, checkOutDateTime) == false)
        {
            intent = CollectionStayActivity.newInstance(mBaseActivity, index, checkInDateTime, checkOutDateTime);
        } else
        {
            intent = CollectionStayActivity.newInstance(mBaseActivity, index, afterDay, nights);
        }

        mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_COLLECTION);

        mBaseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

    private void startDeepLinkRecommendationGourmetActivity(int index, String visitTime, int afterDay)
    {
        Intent intent;

        if (DailyTextUtils.isTextEmpty(visitTime) == false)
        {
            intent = CollectionGourmetActivity.newInstance(mBaseActivity, index, visitTime);
        } else
        {
            intent = CollectionGourmetActivity.newInstance(mBaseActivity, index, afterDay);
        }

        mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_COLLECTION);

        mBaseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
    }

    void startWishList(PlaceType placeType)
    {
        if (isLockUiComponent() == true || mIsAttach == false)
        {
            return;
        }

        lockUiComponent();

        BaseActivity baseActivity = (BaseActivity) getActivity();
        Intent intent = WishListTabActivity.newInstance(baseActivity, WishListTabActivity.SourceType.HOME, placeType);

        baseActivity.startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_RECENTPLACE);
    }

    public void startRecentList(PlaceType placeType)
    {
        if (isLockUiComponent() == true || mIsAttach == false)
        {
            return;
        }

        lockUiComponent();

        BaseActivity baseActivity = (BaseActivity) getActivity();
        Intent intent = RecentPlacesTabActivity.newInstance(baseActivity, RecentPlacesTabActivity.SourceType.HOME, placeType);

        baseActivity.startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_RECENTPLACE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void startPlaceDetail(View view, CarouselListItem carouselListItem, TodayDateTime todayDateTime)
    {
        if (carouselListItem == null || todayDateTime == null)
        {
            return;
        }

        RecentlyPlace recentlyPlace = carouselListItem.getItem();
        if (recentlyPlace == null)
        {
            return;
        }

        ServiceType serviceType;
        try
        {
            serviceType = ServiceType.valueOf(recentlyPlace.serviceType);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
            return;
        }

        try
        {
            switch (serviceType)
            {
                case HOTEL:
                {
                    StayBookingDay stayBookingDay = new StayBookingDay();
                    stayBookingDay.setCheckInDay(todayDateTime.dailyDateTime);
                    stayBookingDay.setCheckOutDay(todayDateTime.dailyDateTime, 1);

                    StayDetailAnalyticsParam analyticsParam = new StayDetailAnalyticsParam();
                    analyticsParam.setAddressAreaName(recentlyPlace.addrSummary);

                    if (recentlyPlace.prices != null)
                    {
                        analyticsParam.price = recentlyPlace.prices.normalPrice;

                        if (recentlyPlace.prices.discountPrice > 0)
                        {
                            analyticsParam.discountPrice = recentlyPlace.prices.discountPrice;
                        }
                    } else
                    {
                        analyticsParam.price = 0;
                        analyticsParam.discountPrice = 0;
                    }

                    analyticsParam.setShowOriginalPriceYn(analyticsParam.price, analyticsParam.discountPrice);
                    analyticsParam.setProvince(null);
                    analyticsParam.entryPosition = -1;
                    analyticsParam.totalListCount = -1;
                    analyticsParam.isDailyChoice = false;
                    analyticsParam.gradeName = recentlyPlace.details.grade;

                    if (Util.isUsedMultiTransition() == true)
                    {
                        mBaseActivity.setExitSharedElementCallback(new SharedElementCallback()
                        {
                            @Override
                            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots)
                            {
                                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                                for (View view : sharedElements)
                                {
                                    if (view instanceof SimpleDraweeView)
                                    {
                                        view.setVisibility(View.VISIBLE);
                                        break;
                                    }
                                }
                            }
                        });

                        //                        AnalyticsParam analyticsParam = new AnalyticsParam();
                        //                        analyticsParam.setParam(mBaseActivity, recentlyPlace);
                        //                        analyticsParam.setProvince(null);
                        //                        analyticsParam.setTotalListCount(-1);

                        //                        Intent intent = StayDetailActivity.newInstance(mBaseActivity //
                        //                            , stayBookingDay, recentlyPlace.index, recentlyPlace.title, recentlyPlace.imageUrl //
                        //                            , analyticsParam, true, PlaceDetailLayout.TRANS_GRADIENT_BOTTOM_TYPE_NONE);

                        Intent intent = StayDetailActivity.newInstance(mBaseActivity //
                            , recentlyPlace.index, recentlyPlace.title, recentlyPlace.imageUrl, StayDetailActivity.NONE_PRICE//
                            , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                            , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                            , true, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam);

                        if (intent == null)
                        {
                            Util.restartApp(mBaseActivity);
                            return;
                        }

                        View simpleDraweeView = view.findViewById(R.id.contentImageView);
                        View gradientTopView = view.findViewById(R.id.gradientTopView);
                        View gradientBottomView = view.findViewById(R.id.gradientBottomView);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mBaseActivity//
                            , android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)) //
                            , android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view)) //
                            , android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view)));

                        mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_STAY_DETAIL, options.toBundle());
                    } else
                    {
                        //                        AnalyticsParam analyticsParam = new AnalyticsParam();
                        //                        analyticsParam.setParam(mBaseActivity, recentlyPlace);
                        //                        analyticsParam.setProvince(null);
                        //                        analyticsParam.setTotalListCount(-1);

                        //                        Intent intent = StayDetailActivity.newInstance(mBaseActivity //
                        //                            , stayBookingDay, recentlyPlace.index, recentlyPlace.title, recentlyPlace.imageUrl //
                        //                            , analyticsParam, false, PlaceDetailLayout.TRANS_GRADIENT_BOTTOM_TYPE_NONE);

                        Intent intent = StayDetailActivity.newInstance(mBaseActivity //
                            , recentlyPlace.index, recentlyPlace.title, recentlyPlace.imageUrl, StayDetailActivity.NONE_PRICE//
                            , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                            , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                            , false, StayDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam);

                        if (intent == null)
                        {
                            Util.restartApp(mBaseActivity);
                            return;
                        }

                        mBaseActivity.startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_STAY_DETAIL);

                        mBaseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                    }
                    break;
                }

                case GOURMET:
                {
                    // --> 추후에 정리되면 메소드로 수정
                    GourmetDetailAnalyticsParam analyticsParam = new GourmetDetailAnalyticsParam();

                    if (recentlyPlace.prices != null)
                    {
                        analyticsParam.price = recentlyPlace.prices.normalPrice;

                        if (recentlyPlace.prices.discountPrice > 0)
                        {
                            analyticsParam.discountPrice = recentlyPlace.prices.discountPrice;
                        }
                    } else
                    {
                        analyticsParam.price = 0;
                        analyticsParam.discountPrice = 0;
                    }

                    analyticsParam.setShowOriginalPriceYn(analyticsParam.price, analyticsParam.discountPrice);
                    analyticsParam.setProvince(null);
                    analyticsParam.entryPosition = -1;
                    analyticsParam.totalListCount = -1;
                    analyticsParam.isDailyChoice = false;
                    analyticsParam.setAddressAreaName(recentlyPlace.addrSummary);
                    // <-- 추후에 정리되면 메소드로 수정

                    if (Util.isUsedMultiTransition() == true)
                    {
                        mBaseActivity.setExitSharedElementCallback(new SharedElementCallback()
                        {
                            @Override
                            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots)
                            {
                                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                                for (View view : sharedElements)
                                {
                                    if (view instanceof SimpleDraweeView)
                                    {
                                        view.setVisibility(View.VISIBLE);
                                        break;
                                    }
                                }
                            }
                        });

                        Intent intent = GourmetDetailActivity.newInstance(mBaseActivity //
                            , recentlyPlace.index, recentlyPlace.title, recentlyPlace.imageUrl//
                            , GourmetDetailActivity.NONE_PRICE, mTodayDateTime.dailyDateTime//
                            , recentlyPlace.details != null ? recentlyPlace.details.category : null//
                            , recentlyPlace.isSoldOut, false, false, true//
                            , GourmetDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE//
                            , analyticsParam);

                        if (intent == null)
                        {
                            Util.restartApp(mBaseActivity);
                            return;
                        }

                        View simpleDraweeView = view.findViewById(R.id.contentImageView);
                        View gradientTopView = view.findViewById(R.id.gradientTopView);
                        View gradientBottomView = view.findViewById(R.id.gradientBottomView);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mBaseActivity//
                            , android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)) //
                            , android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view)) //
                            , android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view)));

                        mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMET_DETAIL, options.toBundle());
                    } else
                    {
                        Intent intent = GourmetDetailActivity.newInstance(mBaseActivity //
                            , recentlyPlace.index, recentlyPlace.title, recentlyPlace.imageUrl//
                            , GourmetDetailActivity.NONE_PRICE, mTodayDateTime.dailyDateTime//
                            , recentlyPlace.details != null ? recentlyPlace.details.category : null//
                            , recentlyPlace.isSoldOut, false, false, false//
                            , GourmetDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE//
                            , analyticsParam);

                        if (intent == null)
                        {
                            Util.restartApp(mBaseActivity);
                            return;
                        }

                        mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_GOURMET_DETAIL);
                        mBaseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
                    }
                    break;
                }
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void startStayOutboundDetail(View view, CarouselListItem carouselListItem, TodayDateTime todayDateTime)
    {
        if (carouselListItem == null)
        {
            return;
        }

        StayOutbound stayOutbound = carouselListItem.getItem();
        if (stayOutbound == null)
        {
            return;
        }

        ImageMap imageMap = stayOutbound.getImageMap();
        String imageUrl;

        if (ScreenUtils.getScreenWidth(getActivity()) >= ScreenUtils.DEFAULT_STAYOUTBOUND_XXHDPI_WIDTH)
        {
            if (DailyTextUtils.isTextEmpty(imageMap.bigUrl) == true)
            {
                imageUrl = imageMap.smallUrl;
            } else
            {
                imageUrl = imageMap.bigUrl;
            }
        } else
        {
            if (DailyTextUtils.isTextEmpty(imageMap.mediumUrl) == true)
            {
                imageUrl = imageMap.smallUrl;
            } else
            {
                imageUrl = imageMap.mediumUrl;
            }
        }

        StayBookDateTime stayBookDateTime = new StayBookDateTime();

        try
        {
            stayBookDateTime.setCheckInDateTime(todayDateTime.currentDateTime);
            stayBookDateTime.setCheckOutDateTime(todayDateTime.currentDateTime, 1);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        StayOutboundDetailAnalyticsParam analyticsParam = new StayOutboundDetailAnalyticsParam();

        try
        {
            analyticsParam.index = stayOutbound.index;
            analyticsParam.benefit = false;
            analyticsParam.grade = null;
            analyticsParam.rankingPosition = -1;
            analyticsParam.rating = null;
            analyticsParam.listSize = -1;
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        if (Util.isUsedMultiTransition() == true)
        {
            getActivity().setExitSharedElementCallback(new SharedElementCallback()
            {
                @Override
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots)
                {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                    for (View view : sharedElements)
                    {
                        if (view instanceof SimpleDraweeView)
                        {
                            view.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            });

            View simpleDraweeView = view.findViewById(R.id.contentImageView);
            View gradientTopView = view.findViewById(R.id.gradientTopView);
            View gradientBottomView = view.findViewById(R.id.gradientBottomView);

            android.support.v4.util.Pair[] pairs = new Pair[3];
            pairs[0] = android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image));
            pairs[1] = android.support.v4.util.Pair.create(gradientTopView, getString(R.string.transition_gradient_top_view));
            pairs[2] = android.support.v4.util.Pair.create(gradientBottomView, getString(R.string.transition_gradient_bottom_view));

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs);

            try
            {
                stayBookDateTime.setCheckInDateTime(todayDateTime.currentDateTime);
                stayBookDateTime.setCheckOutDateTime(todayDateTime.currentDateTime, 1);
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }

            mBaseActivity.startActivityForResult(StayOutboundDetailActivity.newInstance(getActivity(), stayOutbound.index//
                , stayOutbound.name, imageUrl, StayOutboundDetailActivity.NONE_PRICE//
                , stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , stayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , 2, null, true, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam)//
                , Constants.CODE_REQUEST_ACTIVITY_STAY_OB_DETAIL, options.toBundle());
        } else
        {
            mBaseActivity.startActivityForResult(StayOutboundDetailActivity.newInstance(getActivity(), stayOutbound.index//
                , stayOutbound.name, imageUrl, StayOutboundDetailActivity.NONE_PRICE//
                , stayBookDateTime.getCheckInDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , stayBookDateTime.getCheckOutDateTime(DailyCalendar.ISO_8601_FORMAT)//
                , 2, null, false, StayOutboundDetailActivity.TRANS_GRADIENT_BOTTOM_TYPE_NONE, analyticsParam)//
                , Constants.CODE_REQUEST_ACTIVITY_STAY_OB_DETAIL);

            mBaseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
        }
    }

    public void sendHomeScreenAnalytics()
    {
        if (mBaseActivity == null)
        {
            return;
        }

        String memberType = mIsLogin == true //
            ? AnalyticsManager.ValueType.MEMBER : AnalyticsManager.ValueType.GUEST;

        HashMap<String, String> params = new HashMap<>();

        params.put(AnalyticsManager.KeyType.MEMBER_TYPE, memberType);

        AnalyticsManager.getInstance(mBaseActivity).recordScreen(//
            mBaseActivity, AnalyticsManager.Screen.HOME, null, params);
    }

    public void sendHomeBlockEventAnalytics()
    {
        if (mHomeLayout == null || mBaseActivity == null)
        {
            return;
        }

        boolean isRunWishList = (mNetworkRunState & IS_RUNNED_WISHLIST) > 0;
        boolean isRunRecentList = (mNetworkRunState & IS_RUNNED_RECENTLIST) > 0;

        if (mNetworkRunState != IS_RUNNED_NONE && isRunWishList == true && isRunRecentList == true)
        {
            String label = AnalyticsManager.Label.NONE;
            if (mHomeLayout.hasWishListData() == true && mHomeLayout.hasRecentListData() == true)
            {
                label = AnalyticsManager.Label.WISHLIST_RECENTVIEW;
            } else if (mHomeLayout.hasWishListData() == true)
            {
                label = AnalyticsManager.Label.WISHLIST;
            } else if (mHomeLayout.hasRecentListData() == true)
            {
                label = AnalyticsManager.Label.RECENTVIEW;
            }

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_BLOCK_SHOW,//
                label, null);
        }
    }

    private void requestWishList()
    {
        if (mIsLogin == false)
        {
            mNetworkControllerListener.onWishList(null, false);
            return;
        }

        mNetworkController.requestWishList();
    }

    private void requestRecentList()
    {
        Observable<ArrayList<RecentlyPlace>> ibObservable = mRecentlyLocalImpl.getRecentlyJSONObject(MAX_REQUEST_SIZE, ServiceType.HOTEL, ServiceType.GOURMET) //
            .observeOn(Schedulers.io()).flatMap(new Function<JSONObject, ObservableSource<ArrayList<RecentlyPlace>>>()
            {
                @Override
                public ObservableSource<ArrayList<RecentlyPlace>> apply(@NonNull JSONObject jsonObject) throws Exception
                {
                    if (jsonObject == null || jsonObject.has("keys") == false)
                    {
                        return Observable.just(new ArrayList<>());
                    }

                    return mRecentlyRemoteImpl.getInboundRecentlyList(jsonObject);
                }
            });

        Observable<StayOutbounds> obObservable = mRecentlyLocalImpl.getDbTargetIndices(Constants.ServiceType.OB_STAY, DailyDb.MAX_RECENT_PLACE_COUNT) //
            .observeOn(Schedulers.io()).flatMap(new Function<String, ObservableSource<StayOutbounds>>()
            {
                @Override
                public ObservableSource<StayOutbounds> apply(@NonNull String targetIndices) throws Exception
                {
                    return mRecentlyRemoteImpl.getStayOutboundRecentlyList(targetIndices, DailyDb.MAX_RECENT_PLACE_COUNT);
                }
            });

        addCompositeDisposable(Observable.zip(ibObservable, obObservable //
            , new BiFunction<ArrayList<RecentlyPlace>, StayOutbounds, ArrayList<CarouselListItem>>()
            {
                @Override
                public ArrayList<CarouselListItem> apply(@NonNull ArrayList<RecentlyPlace> recentlyPlaceList, @NonNull StayOutbounds stayOutbounds) throws Exception
                {
                    ArrayList<CarouselListItem> carouselListItemList = new ArrayList<>();
                    if (recentlyPlaceList != null)
                    {
                        for (RecentlyPlace recentlyPlace : recentlyPlaceList)
                        {
                            CarouselListItem item = new CarouselListItem(CarouselListItem.TYPE_RECENTLY_PLACE, recentlyPlace);
                            carouselListItemList.add(item);
                        }
                    }

                    List<StayOutbound> stayOutboundList = stayOutbounds.getStayOutbound();
                    if (stayOutboundList != null)
                    {
                        for (StayOutbound stayOutbound : stayOutboundList)
                        {
                            CarouselListItem item = new CarouselListItem(CarouselListItem.TYPE_OB_STAY, stayOutbound);
                            carouselListItemList.add(item);
                        }
                    }

                    return carouselListItemList;
                }
            }).flatMap(new Function<ArrayList<CarouselListItem>, ObservableSource<ArrayList<CarouselListItem>>>()
        {
            @Override
            public ObservableSource<ArrayList<CarouselListItem>> apply(@NonNull ArrayList<CarouselListItem> carouselListItems) throws Exception
            {
                return mRecentlyLocalImpl.sortCarouselListItemList(carouselListItems, (Constants.ServiceType[]) null);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayList<CarouselListItem>>()
        {
            @Override
            public void accept(ArrayList<CarouselListItem> carouselListItems) throws Exception
            {
                HomeFragment.this.setRecentlyList(carouselListItems, false);
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                HomeFragment.this.setRecentlyList(null, true);
                Crashlytics.logException(new Exception("need check Daily DB", throwable));
            }
        }));
    }

    void setRecentlyList(ArrayList<CarouselListItem> carouselListItemList, boolean isError)
    {
        ArrayList<CarouselListItem> list = new ArrayList<>();

        if (carouselListItemList != null)
        {
            if (carouselListItemList.size() > MAX_RESPONSE_SIZE)
            {
                list.addAll(carouselListItemList.subList(0, MAX_RESPONSE_SIZE));
            } else
            {
                list.addAll(carouselListItemList);
            }
        }

        mHomeLayout.setRecentListData(list, isError);

        mNetworkRunState = mNetworkRunState | IS_RUNNED_RECENTLIST;

        sendHomeBlockEventAnalytics();
    }

    private void getCommonDateTime()
    {
        addCompositeDisposable(mCommonRemoteImpl.getCommonDateTime().map(new Function<CommonDateTime, TodayDateTime>()
        {
            @Override
            public TodayDateTime apply(@NonNull CommonDateTime commonDateTime) throws Exception
            {
                TodayDateTime todayDateTime = new TodayDateTime(commonDateTime.openDateTime //
                    , commonDateTime.closeDateTime, commonDateTime.currentDateTime, commonDateTime.dailyDateTime);

                return todayDateTime;
            }
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<TodayDateTime>()
        {
            @Override
            public void accept(@NonNull TodayDateTime todayDateTime) throws Exception
            {
                unLockUI();
                setCommonDateTime(todayDateTime);

                checkStayOutboundCutNewVisible(todayDateTime);
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception
            {
                mHomeLayout.setRefreshing(false);

                onHandleError(throwable);
            }
        }));
    }

    void setCommonDateTime(TodayDateTime todayDateTime)
    {
        if (isFinishing() == true)
        {
            return;
        }

        if (mHomeLayout != null)
        {
            if (mHomeLayout.isRefreshing() == true)
            {
                mHomeLayout.setRefreshing(false);
            }
        }

        mTodayDateTime = todayDateTime;
    }

    public void forceRefreshing()
    {
        if (mHomeLayout == null || mHomeLayout.isRefreshing() == false || lockUiComponentAndIsLockUiComponent() == true)
        {
            return;
        }

        lockUI(false);

        mHomeLayout.forceRefreshing(false);
    }

    public void refreshList(boolean isShowLockUi)
    {
        boolean newLoginState = DailyHotel.isLogin();
        if (newLoginState != mIsLogin)
        {
            mIsLogin = newLoginState;
            requestAllData(isShowLockUi);
            return;
        }

        if (mDontReload == true)
        {
            mDontReload = false;

            if (mHomeLayout != null)
            {
                mHomeLayout.resumeNextEventPosition();
            }
        } else
        {
            requestAllData(isShowLockUi);

            sendHomeScreenAnalytics();
        }
    }

    private void requestAllData(boolean isShowLockUi)
    {
        if (mNetworkController != null)
        {
            lockUI(isShowLockUi);

            mNetworkRunState = IS_RUNNED_NONE;

            requestCategoryEnabled();
            requestMessageData();
            mNetworkController.requestEventList();
            mNetworkController.requestRecommendationList();
            requestWishList();

            getCommonDateTime();
            requestRecentList();

            if (DailyHotel.isLogin() == true && DailyRemoteConfigPreference.getInstance(mBaseActivity).isRemoteConfigStampEnabled() == true //
                && DailyRemoteConfigPreference.getInstance(mBaseActivity).isRemoteConfigStampHomeEnabled() == true)
            {
                mNetworkController.requestUserStamps();
            }
        }
    }

    protected void checkLocationProvider()
    {
        if (mDailyLocationFactory == null)
        {
            mDailyLocationFactory = new DailyLocationFactory(getContext());
        }

        mDailyLocationFactory.checkLocationMeasure(new DailyLocationFactory.OnCheckLocationListener()
        {
            @Override
            public void onRequirePermission()
            {
                unLockUI();

                Intent intent = PermissionManagerActivity.newInstance( //
                    mBaseActivity, PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);
            }

            @Override
            public void onFailed()
            {
                unLockUI();
            }

            @Override
            public void onProviderDisabled()
            {
                unLockUI();

                if (isFinishing() == true)
                {
                    return;
                }

                // 현재 GPS 설정이 꺼져있습니다 설정에서 바꾸어 주세요.
                mDailyLocationFactory.stopLocationMeasure();

                mBaseActivity.showSimpleDialog(getString(R.string.dialog_title_used_gps)//
                    , getString(R.string.dialog_msg_used_gps)//
                    , getString(R.string.dialog_btn_text_dosetting)//
                    , getString(R.string.dialog_btn_text_cancel)//
                    , new View.OnClickListener()//
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_SETTING_LOCATION);
                        }
                    }, null, false);
            }

            @Override
            public void onProviderEnabled()
            {
                onSearchLocation();
            }
        });
    }

    void onSearchLocation()
    {
        if (mTodayDateTime == null)
        {
            Util.restartApp(mBaseActivity);
            return;
        }

        try
        {
            StayBookingDay stayBookingDay = new StayBookingDay();
            stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
            stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

            Intent intent = StaySearchResultActivity.newInstance(mBaseActivity, mTodayDateTime, stayBookingDay, null, AnalyticsManager.Screen.HOME);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_SEARCH_RESULT);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

    }

    void onSearchClick()
    {
        if (mBaseActivity == null)
        {
            return;
        }

        if (isLockUiComponent() == true || mIsAttach == false)
        {
            return;
        }

        try
        {
            StayBookingDay stayBookingDay = new StayBookingDay();
            stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
            stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

            mBaseActivity.startActivityForResult(SearchActivity.newInstance(mBaseActivity, mPlaceType, stayBookingDay), Constants.CODE_REQUEST_ACTIVITY_SEARCH);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.SEARCH, AnalyticsManager.Action.SEARCH_BUTTON_CLICK,//
                AnalyticsManager.Label.HOME, null);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void onStayClick(boolean isDeepLink, DailyDeepLink dailyDeepLink)
    {
        if (mBaseActivity == null)
        {
            return;
        }

        if (isLockUiComponent() == true || mIsAttach == false)
        {
            return;
        }

        mBaseActivity.startActivityForResult(StayMainActivity.newInstance(mBaseActivity//
            , dailyDeepLink != null ? dailyDeepLink.getDeepLink() : null), Constants.CODE_REQUEST_ACTIVITY_STAY);

        if (isDeepLink == false)
        {
            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.STAY_LIST_CLICK,//
                AnalyticsManager.Label.HOME, null);
        }
    }

    void onGourmetClick(boolean isDeepLink, DailyDeepLink dailyDeepLink)
    {
        if (mBaseActivity == null)
        {
            return;
        }

        if (isLockUiComponent() == true || mIsAttach == false)
        {
            return;
        }

        mBaseActivity.startActivityForResult(GourmetMainActivity.newInstance(mBaseActivity//
            , dailyDeepLink != null ? dailyDeepLink.getDeepLink() : null), Constants.CODE_REQUEST_ACTIVITY_GOURMET);

        if (isDeepLink == false)
        {
            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.GOURMET_LIST_CLICK,//
                AnalyticsManager.Label.HOME, null);
        }
    }

    @Override
    public void setOnScrollChangedListener(BaseMenuNavigationFragment.OnScreenScrollChangeListener listener)
    {
        mOnScreenScrollChangeListener = listener;

        if (mHomeLayout != null)
        {
            mHomeLayout.setOnScrollChangedListener(listener);
        }
    }

    @Override
    public void setOnMenuChangeListener(OnMenuChangeListener listener)
    {

    }

    @Override
    public void scrollTop()
    {
        if (mHomeLayout != null)
        {
            mHomeLayout.setScrollTop();
        }
    }

    private HomeLayout.OnEventListener mOnEventListener = new HomeLayout.OnEventListener()
    {
        @Override
        public void onMessageTextAreaClick()
        {
            // 회원가입으로 이동!
            startSignUp(null);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.MESSAGE_CLICK,//
                AnalyticsManager.Label.SIGNUP, null);
        }

        @Override
        public void onMessageTextAreaCloseClick()
        {
            DailyPreference.getInstance(mBaseActivity).setHomeTextMessageAreaEnabled(false);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.MESSAGE_CLOSE,//
                AnalyticsManager.Label.SIGNUP, null);
        }

        @Override
        public void onSearchImageClick()
        {
            onSearchClick();
        }

        @Override
        public void onStayButtonClick()
        {
            onStayClick(false, null);
        }

        @Override
        public void onGourmetButtonClick()
        {
            onGourmetClick(false, null);
        }

        @Override
        public void onRefreshAll(boolean isShowProgress)
        {
            mDontReload = false;
            refreshList(isShowProgress);
        }

        @Override
        public void onTopButtonClick()
        {
            mHomeLayout.setScrollTop();
        }

        @Override
        public void onEventItemClick(Event event)
        {
            if (event == null)
            {
                return;
            }

            if (DailyTextUtils.isTextEmpty(event.linkUrl, event.title) == true)
            {
                return;
            }

            HomeFragment.this.startEventWebActivity(event.linkUrl, event.title);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_EVENT_BANNER_CLICK,//
                Integer.toString(event.index), null);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onRecommendationClick(View view, Recommendation recommendation)
        {
            Intent intent;

            if (Util.isUsedMultiTransition() == true)
            {
                mBaseActivity.setExitSharedElementCallback(new SharedElementCallback()
                {
                    @Override
                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots)
                    {
                        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                        for (View view : sharedElements)
                        {
                            if (view instanceof SimpleDraweeView)
                            {
                                view.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                });

                switch (recommendation.serviceType)
                {
                    case "GOURMET":
                        intent = CollectionGourmetActivity.newInstance(mBaseActivity, recommendation.idx//
                            , ScreenUtils.getResolutionImageUrl(mBaseActivity, recommendation.defaultImageUrl, recommendation.lowResolutionImageUrl)//
                            , recommendation.title, recommendation.subtitle, true);
                        break;

                    case "HOTEL":
                    default:
                        intent = CollectionStayActivity.newInstance(mBaseActivity, recommendation.idx//
                            , ScreenUtils.getResolutionImageUrl(mBaseActivity, recommendation.defaultImageUrl, recommendation.lowResolutionImageUrl)//
                            , recommendation.title, recommendation.subtitle, true);
                        break;
                }

                View simpleDraweeView = view.findViewById(R.id.contentImageView);
                View contentTextLayout = view.findViewById(R.id.contentTextLayout);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mBaseActivity,//
                    android.support.v4.util.Pair.create(simpleDraweeView, getString(R.string.transition_place_image)),//
                    android.support.v4.util.Pair.create(contentTextLayout, getString(R.string.transition_layout)));

                mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_COLLECTION, options.toBundle());
            } else
            {
                switch (recommendation.serviceType)
                {
                    case "GOURMET":
                        intent = CollectionGourmetActivity.newInstance(mBaseActivity, recommendation.idx//
                            , ScreenUtils.getResolutionImageUrl(mBaseActivity, recommendation.defaultImageUrl, recommendation.lowResolutionImageUrl)//
                            , recommendation.title, recommendation.subtitle, false);
                        break;

                    case "HOTEL":
                    default:
                        intent = CollectionStayActivity.newInstance(mBaseActivity, recommendation.idx//
                            , ScreenUtils.getResolutionImageUrl(mBaseActivity, recommendation.defaultImageUrl, recommendation.lowResolutionImageUrl)//
                            , recommendation.title, recommendation.subtitle, false);
                        break;
                }

                mBaseActivity.startActivityForResult(intent, CODE_REQUEST_ACTIVITY_COLLECTION);

                mBaseActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.hold);
            }

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_RECOMMEND_LIST_CLICK,//
                Integer.toString(recommendation.idx), null);
        }

        @Override
        public void onWishListViewAllClick()
        {
            startWishList(null);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_ALL_WISHLIST_CLICK, //
                null, null);
        }

        @Override
        public void onRecentListViewAllClick()
        {
            startRecentList(null);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_ALL_RECENTVIEW_CLICK, //
                null, null);
        }

        @Override
        public void onWishListItemClick(View view)
        {
            if (isFinishing() == true || view == null || mHomeLayout == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            CarouselListItem item = (CarouselListItem) view.getTag();
            if (item == null)
            {
                return;
            }

            RecentlyPlace wishItem = item.getItem();
            if (wishItem == null)
            {
                return;
            }

            startPlaceDetail(view, item, mTodayDateTime);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_WISHLIST_CLICK,//
                Integer.toString(wishItem.index), null);
        }

        @Override
        public void onWishListItemLongClick(View view)
        {
            if (isFinishing() == true || view == null || mHomeLayout == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            CarouselListItem item = (CarouselListItem) view.getTag();
            if (item == null)
            {
                return;
            }

            try
            {
                RecentlyPlace recentlyPlace = item.getItem();
                if (recentlyPlace == null)
                {
                    return;
                }

                mViewByLongPress = view;
                mCarouselListItemByLongPress = item;

                mHomeLayout.setBlurVisibility(mBaseActivity, true);

                ServiceType serviceType = ServiceType.valueOf(recentlyPlace.serviceType);

                switch (serviceType)
                {
                    case HOTEL:
                    {
                        StayBookingDay stayBookingDay = new StayBookingDay();
                        stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
                        stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

                        Intent intent = StayPreviewActivity.newInstance(mBaseActivity, stayBookingDay, recentlyPlace);

                        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PREVIEW);
                        break;
                    }

                    case GOURMET:
                    {
                        GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();
                        gourmetBookingDay.setVisitDay(mTodayDateTime.dailyDateTime);

                        Intent intent = GourmetPreviewActivity.newInstance(mBaseActivity, gourmetBookingDay, recentlyPlace);

                        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PREVIEW);
                        break;
                    }
                }
            } catch (Exception e)
            {
                ExLog.w(e.toString());
            } finally
            {
                unLockUI();
            }
        }

        @Override
        public void onRecentListItemClick(View view)
        {
            if (isFinishing() == true || view == null || mHomeLayout == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            CarouselListItem item = (CarouselListItem) view.getTag();
            if (item == null)
            {
                return;
            }

            int index;
            if (CarouselListItem.TYPE_OB_STAY == item.mType)
            {
                // stayOutbound
                StayOutbound stayOutbound = item.getItem();
                if (stayOutbound == null)
                {
                    return;
                }

                index = stayOutbound.index;
                startStayOutboundDetail(view, item, mTodayDateTime);
            } else
            {
                RecentlyPlace recentlyPlace = item.getItem();
                if (recentlyPlace == null)
                {
                    return;
                }

                index = recentlyPlace.index;
                startPlaceDetail(view, item, mTodayDateTime);
            }

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_RECENTVIEW_CLICK,//
                Integer.toString(index), null);
        }

        @Override
        public void onRecentListItemLongClick(View view)
        {
            if (isFinishing() == true || view == null || mHomeLayout == null || lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            CarouselListItem item = (CarouselListItem) view.getTag();
            if (item == null)
            {
                return;
            }

            try
            {
                if (CarouselListItem.TYPE_OB_STAY == item.mType)
                {
                    StayOutbound stayOutbound = item.getItem();
                    if (stayOutbound == null)
                    {
                        return;
                    }

                    mViewByLongPress = view;
                    mCarouselListItemByLongPress = item;

                    mHomeLayout.setBlurVisibility(mBaseActivity, true);

                    StayBookingDay stayBookingDay = new StayBookingDay();
                    stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
                    stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

                    startActivityForResult(StayOutboundPreviewActivity.newInstance(getActivity(), stayOutbound.index//
                        , stayOutbound.name//
                        , stayBookingDay.getCheckInDay(DailyCalendar.ISO_8601_FORMAT)//
                        , stayBookingDay.getCheckOutDay(DailyCalendar.ISO_8601_FORMAT)//
                        , People.DEFAULT_ADULTS, null)//
                        , CODE_REQUEST_ACTIVITY_PREVIEW);

                } else
                {
                    RecentlyPlace recentlyPlace = item.getItem();
                    if (recentlyPlace == null)
                    {
                        return;
                    }

                    mViewByLongPress = view;
                    mCarouselListItemByLongPress = item;

                    mHomeLayout.setBlurVisibility(mBaseActivity, true);

                    ServiceType serviceType = ServiceType.valueOf(recentlyPlace.serviceType);
                    switch (serviceType)
                    {
                        case HOTEL:
                        {
                            StayBookingDay stayBookingDay = new StayBookingDay();
                            stayBookingDay.setCheckInDay(mTodayDateTime.dailyDateTime);
                            stayBookingDay.setCheckOutDay(mTodayDateTime.dailyDateTime, 1);

                            Intent intent = StayPreviewActivity.newInstance(mBaseActivity, stayBookingDay, recentlyPlace);

                            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PREVIEW);
                            break;
                        }

                        case GOURMET:
                        {
                            GourmetBookingDay gourmetBookingDay = new GourmetBookingDay();
                            gourmetBookingDay.setVisitDay(mTodayDateTime.dailyDateTime);

                            Intent intent = GourmetPreviewActivity.newInstance(mBaseActivity, gourmetBookingDay, recentlyPlace);

                            startActivityForResult(intent, CODE_REQUEST_ACTIVITY_PREVIEW);
                            break;
                        }
                    }
                }
            } catch (Exception e)
            {
                ExLog.w(e.toString());
            } finally
            {
                unLockUI();
            }
        }

        @Override
        public void onTermsClick()
        {
            Intent intent = new Intent(mBaseActivity, TermActivity.class);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
        }

        @Override
        public void onPrivacyTermsClick()
        {
            Intent intent = new Intent(mBaseActivity, PrivacyActivity.class);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
        }

        @Override
        public void onLocationTermsClick()
        {
            Intent intent = new Intent(mBaseActivity, LocationTermsActivity.class);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
        }

        @Override
        public void onProtectedYouthClick()
        {
            Intent intent = new Intent(mBaseActivity, ProtectYouthTermsActivity.class);
            startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_TERMS_AND_POLICY);
        }

        @Override
        public void onCategoryItemClick(DailyCategoryType categoryType)
        {
            if (categoryType == null || DailyCategoryType.NONE.equals(categoryType) == true)
            {
                return;
            }

            switch (categoryType)
            {
                case STAY_NEARBY:
                {
                    if (lockUiComponentAndIsLockUiComponent() == true)
                    {
                        return;
                    }

                    Intent intent = PermissionManagerActivity.newInstance(mBaseActivity //
                        , PermissionManagerActivity.PermissionType.ACCESS_FINE_LOCATION);
                    startActivityForResult(intent, Constants.CODE_REQUEST_ACTIVITY_PERMISSION_MANAGER);

                    try
                    {
                        AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                            AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_SHORTCUT_CLICK,//
                            AnalyticsManager.Label.NEAR_BY, null);
                    } catch (Exception e)
                    {
                        ExLog.d(e.toString());
                    }
                    return;
                }

                case STAY_OUTBOUND_HOTEL:
                {
                    Intent intent = StayOutboundSearchActivity.newInstance(mBaseActivity);
                    startActivityForResult(intent, Constants.CODE_RESULT_ACTIVITY_STAY_OUTBOUND_SEARCH);

                    AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                        AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_SHORTCUT_CLICK,//
                        AnalyticsManager.Label.OUTBOUND, null);
                    return;
                }
            }

            try
            {
                StayBookingDay stayBookingDay = new StayBookingDay();

                String checkInDay = mTodayDateTime.dailyDateTime;
                int nights = 1;
                stayBookingDay.setCheckInDay(checkInDay);
                stayBookingDay.setCheckOutDay(checkInDay, nights);

                mBaseActivity.startActivityForResult( //
                    HomeCategoryRegionListActivity.newInstance(mBaseActivity, categoryType, stayBookingDay) //
                    , Constants.CODE_REQUEST_ACTIVITY_REGIONLIST);

                String label = "";
                switch (categoryType)
                {
                    case STAY_HOTEL:
                        label = AnalyticsManager.Label.HOTEL;
                        break;
                    case STAY_BOUTIQUE:
                        label = AnalyticsManager.Label.BOUTIQUE;
                        break;
                    case STAY_PENSION:
                        label = AnalyticsManager.Label.PENSION;
                        break;
                    case STAY_RESORT:
                        label = AnalyticsManager.Label.RESORT;
                        break;
                }

                AnalyticsManager.getInstance(mBaseActivity).recordEvent(//
                    AnalyticsManager.Category.NAVIGATION, AnalyticsManager.Action.HOME_SHORTCUT_CLICK,//
                    label, null);
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }

        @Override
        public void onStampEventClick()
        {
            if (lockUiComponentAndIsLockUiComponent() == true)
            {
                return;
            }

            startActivityForResult(EventWebActivity.newInstance(mBaseActivity, EventWebActivity.SourceType.STAMP//
                , DailyRemoteConfigPreference.getInstance(mBaseActivity).getKeyRemoteConfigStaticUrlDailyStampHome()//
                , mBaseActivity.getString(R.string.label_stamp_event_title)), Constants.CODE_REQUEST_ACTIVITY_STAMP);

            AnalyticsManager.getInstance(mBaseActivity).recordEvent(AnalyticsManager.Category.NAVIGATION,//
                AnalyticsManager.Action.STAMP_DETAIL_CLICK, AnalyticsManager.Label.HOME, null);
        }

        @Override
        public void finish()
        {
            unLockUI();
            mBaseActivity.finish();
        }
    };

    HomeNetworkController.OnNetworkControllerListener mNetworkControllerListener = new HomeNetworkController.OnNetworkControllerListener()
    {
        @Override
        public void onEventList(ArrayList<Event> list)
        {
            if (mHomeLayout != null)
            {
                mHomeLayout.setEventList(list);
            }
        }

        @Override
        public void onWishList(ArrayList<CarouselListItem> list, boolean isError)
        {
            if (mHomeLayout != null)
            {
                mHomeLayout.setWishListData(list, isError);
            }

            mNetworkRunState = mNetworkRunState | IS_RUNNED_WISHLIST;

            sendHomeBlockEventAnalytics();
        }

        @Override
        public void onRecommendationList(ArrayList<Recommendation> list, boolean isError)
        {
            if (mHomeLayout != null)
            {
                mHomeLayout.setRecommendationData(list, isError);
            }
        }

        @Override
        public void onStamps(int count, boolean isError)
        {
            if (mHomeLayout != null)
            {
                mHomeLayout.setStampCount(count, isError);
            }
        }

        @Override
        public void onError(Call call, Throwable e, boolean onlyReport)
        {
            HomeFragment.this.onError(call, e, onlyReport);
        }

        @Override
        public void onError(Throwable e)
        {
            HomeFragment.this.onError(e);
        }

        @Override
        public void onErrorPopupMessage(int msgCode, String message)
        {
            HomeFragment.this.onErrorPopupMessage(msgCode, message);
        }

        @Override
        public void onErrorToastMessage(String message)
        {
            HomeFragment.this.onErrorToastMessage(message);
        }

        @Override
        public void onErrorResponse(Call call, Response response)
        {
            HomeFragment.this.onErrorResponse(call, response);
        }
    };
}
