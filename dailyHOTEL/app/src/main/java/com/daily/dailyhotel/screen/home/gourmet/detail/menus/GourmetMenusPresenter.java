package com.daily.dailyhotel.screen.home.gourmet.detail.menus;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.daily.base.BaseAnalyticsInterface;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.widget.DailyToast;
import com.daily.dailyhotel.base.BaseExceptionPresenter;
import com.daily.dailyhotel.entity.DetailImageInformation;
import com.daily.dailyhotel.entity.GourmetBookDateTime;
import com.daily.dailyhotel.entity.GourmetCart;
import com.daily.dailyhotel.entity.GourmetCartMenu;
import com.daily.dailyhotel.entity.GourmetMenu;
import com.daily.dailyhotel.parcel.GourmetMenuParcel;
import com.daily.dailyhotel.parcel.analytics.ImageListAnalyticsParam;
import com.daily.dailyhotel.repository.local.CartLocalImpl;
import com.daily.dailyhotel.screen.common.images.ImageListActivity;
import com.daily.dailyhotel.screen.home.gourmet.detail.GourmetDetailPresenter;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyCalendar;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by sheldon
 * Clean Architecture
 */
public class GourmetMenusPresenter extends BaseExceptionPresenter<GourmetMenusActivity, GourmetMenusInterface> implements GourmetMenusView.OnEventListener
{
    GourmetMenusAnalyticsInterface mAnalytics;

    private CartLocalImpl mCartLocalImpl;

    private GourmetBookDateTime mGourmetBookDateTime;
    private List<GourmetMenu> mGourmetMenuList;
    private List<GourmetMenu> mGourmetMenuVisibleList;

    private int mPosition;
    private int mCenterPosition = -1;

    private int mGourmetIndex;
    private String mGourmetName, mCategory, mImageUrl;
    private List<Integer> mOperationTimeList;
    private int mVisitTime;
    private GourmetCart mGourmetCart;
    private boolean mOpenedOperationTime, mOpenedCartMenus;

    public interface GourmetMenusAnalyticsInterface extends BaseAnalyticsInterface
    {
        void onScreen(Activity activity);

        void onEventBackClick(Activity activity);

        void onEventFlicking(Activity activity);

        void onEventImageClick(Activity activity, String label);

        void onEventOpenCartMenuClick(Activity activity, int gourmetIndex, int menuCount);

        void onEventBookingClick(Activity activity, int gourmetIndex, int menuCount);

        void onEventOpenOperationTimeClick(Activity activity);

        void onEventOperationVisitTimeClick(Activity activity, int visitTime);

        void onEventChangeCart(Activity activity, String action, String label);
    }

    public GourmetMenusPresenter(@NonNull GourmetMenusActivity activity)
    {
        super(activity);
    }

    @NonNull
    @Override
    protected GourmetMenusInterface createInstanceViewInterface()
    {
        return new GourmetMenusView(getActivity(), this);
    }

    @Override
    public void constructorInitialize(GourmetMenusActivity activity)
    {
        setContentView(R.layout.activity_gourmet_menus_data);

        setAnalytics(new GourmetMenusAnalyticsImpl());

        mCartLocalImpl = new CartLocalImpl(activity);

        setRefresh(true);
    }

    @Override
    public void setAnalytics(BaseAnalyticsInterface analytics)
    {
        mAnalytics = (GourmetMenusAnalyticsInterface) analytics;
    }

    @Override
    public boolean onIntent(Intent intent)
    {
        if (intent == null)
        {
            return true;
        }

        setGourmetBookDateTime(intent.getStringExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_VISIT_DATE_TIME));
        mGourmetIndex = intent.getIntExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_GOURMET_INDEX, 0);
        mGourmetName = intent.getStringExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_GOURMET_NAME);

        ArrayList<GourmetMenuParcel> gourmetMenuParcelList = intent.getParcelableArrayListExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_MENU_LIST);

        if (gourmetMenuParcelList == null || gourmetMenuParcelList.size() == 0)
        {
            return false;
        }

        mGourmetMenuList = new ArrayList<>();
        mGourmetMenuVisibleList = new ArrayList<>();

        for (GourmetMenuParcel gourmetMenuParcel : gourmetMenuParcelList)
        {
            mGourmetMenuList.add(gourmetMenuParcel.getGourmetMenu());

            ExLog.d("pinkred : " + gourmetMenuParcel.getGourmetMenu().minimumOrderQuantity + ", " + gourmetMenuParcel.getGourmetMenu().maximumOrderQuantity + ", " + gourmetMenuParcel.getGourmetMenu().saleOrderQuantity);
        }

        mPosition = intent.getIntExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_POSITION, 0);
        mOperationTimeList = intent.getIntegerArrayListExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_OPERATION_TIMES);
        mVisitTime = intent.getIntExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_VISIT_TIME, GourmetDetailPresenter.FULL_TIME);
        mCategory = intent.getStringExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_CATEGORY);
        mImageUrl = intent.getStringExtra(GourmetMenusActivity.INTENT_EXTRA_DATA_IMAGE_URL);

        return true;
    }

    @Override
    public void onPostCreate()
    {
        if (DailyPreference.getInstance(getActivity()).getGourmetProductDetailGuide() == true)
        {
            DailyPreference.getInstance(getActivity()).setGourmetProductDetailGuide(false);
            getViewInterface().setGuideVisible(true);
        }

        setToolbarTitle(mVisitTime);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mAnalytics.onScreen(getActivity());

        if (isRefresh() == true)
        {
            onRefresh(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        // 꼭 호출해 주세요.
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed()
    {
        mAnalytics.onEventBackClick(getActivity());

        if (mOpenedOperationTime == true)
        {
            onOperationTimeClick();
            return true;
        } else if (mOpenedCartMenus == true)
        {
            onCloseCartMenusClick();
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        unLockAll();
    }

    @Override
    protected synchronized void onRefresh(boolean showProgress)
    {
        if (getActivity().isFinishing() == true || isRefresh() == false)
        {
            return;
        }

        addCompositeDisposable(mCartLocalImpl.getGourmetCart().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<GourmetCart>()
        {
            @Override
            public void accept(GourmetCart gourmetCart) throws Exception
            {
                mGourmetCart = gourmetCart;

                getViewInterface().setOperationTimes(mOperationTimeList);
                getViewInterface().setCartVisible(false);

                notifyOperationTimeChanged(mPosition);

                onScrolled(mPosition, false);

                if ((gourmetCart.equalsDay(mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT)) == true)//
                    && mGourmetCart.gourmetIndex == mGourmetIndex && mGourmetCart.getMenuCount() > 0)
                {
                    getViewInterface().setCartVisible(true);
                    getViewInterface().setSummeryCart(getString(R.string.label_gourmet_product_detail_check_the_menus), mGourmetCart.getTotalCount());
                }
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                mGourmetCart = new GourmetCart();

                getViewInterface().setOperationTimes(mOperationTimeList);
                getViewInterface().setCartVisible(false);

                notifyOperationTimeChanged(mPosition);

                onScrolled(mPosition, false);
            }
        }));
    }

    @Override
    public void onBackClick()
    {
        getActivity().onBackPressed();
    }

    @Override
    public void onCloseClick()
    {
        if (mOpenedOperationTime == true)
        {
            onOperationTimeClick();
        } else
        {
            onBackClick();
        }
    }

    @Override
    public void onGuideClick()
    {
        if (lock() == true)
        {
            return;
        }

        Observable<Boolean> observable = getViewInterface().hideGuideAnimation();

        if (observable == null)
        {
            unLockAll();
            return;
        }

        addCompositeDisposable(observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                unLockAll();
            }
        }));
    }

    @Override
    public void onBookingClick()
    {
        if (lock() == true)
        {
            return;
        }

        mAnalytics.onEventBookingClick(getActivity(), mGourmetIndex, mGourmetCart.getMenuCount());

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onScrolled(int position, boolean real)
    {
        if (mCenterPosition == position)
        {
            return;
        }

        mCenterPosition = position;

        getViewInterface().setMenuIndicator(position + 1, mGourmetMenuVisibleList.size());

        if (real == true)
        {
            mAnalytics.onEventFlicking(getActivity());
        }
    }

    @Override
    public void onMoreImageClick(int position)
    {
        if (lock() == true)
        {
            return;
        }

        GourmetMenu gourmetMenu = mGourmetMenuList.get(position);
        List<DetailImageInformation> gourmetMenuImageList = gourmetMenu.getImageList();


        if (gourmetMenuImageList == null || gourmetMenuImageList.size() == 0)
        {
            unLockAll();
            return;
        }

        ImageListAnalyticsParam analyticsParam = new ImageListAnalyticsParam();
        analyticsParam.serviceType = Constants.ServiceType.GOURMET;

        startActivityForResult(ImageListActivity.newInstance(getActivity(), gourmetMenu.name//
            , gourmetMenuImageList, 0, analyticsParam), GourmetMenusActivity.REQUEST_CODE_IMAGE_LIST);

        mAnalytics.onEventImageClick(getActivity(), Integer.toString(gourmetMenu.index));
    }

    @Override
    public void onOperationTimeClick()
    {
        if (lock() == true)
        {
            return;
        }

        screenLock(false);

        if (mOpenedOperationTime == true)
        {
            addCompositeDisposable(getViewInterface().closeOperationTimes().subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    setToolbarTitle(mVisitTime);

                    mOpenedOperationTime = false;

                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    mOpenedOperationTime = false;

                    unLockAll();
                }
            }));
        } else
        {
            setToolbarTitle(getString(R.string.label_gourmet_product_detail_view_operation_time_list));

            addCompositeDisposable(getViewInterface().openOperationTimes(mVisitTime).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    mOpenedOperationTime = true;

                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    mOpenedOperationTime = true;

                    unLockAll();
                }
            }));

            mAnalytics.onEventOpenOperationTimeClick(getActivity());
        }
    }

    @Override
    public void onVisitTimeClick(int time)
    {
        if (lock() == true)
        {
            return;
        }

        mVisitTime = time;

        mPosition = 0;

        notifyOperationTimeChanged(mPosition);

        mOpenedOperationTime = false;

        addCompositeDisposable(getViewInterface().closeOperationTimes().subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                setToolbarTitle(mVisitTime);

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                unLockAll();
            }
        }));

        mAnalytics.onEventOperationVisitTimeClick(getActivity(), time);
    }

    /**
     * 팝업(다이얼로그) 창에서 시간을 선택하는 경우
     *
     * @param time
     * @param menuIndex
     */
    @Override
    public void onVisitTimeClick(int time, int menuIndex)
    {
        if (time <= 0 || menuIndex < 0 || lock() == true)
        {
            return;
        }

        mVisitTime = time;

        notifyOperationTimeChanged(menuIndex);

        setToolbarTitle(mVisitTime);

        mGourmetCart.setGourmetInformation(mGourmetIndex, mGourmetName, mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT), mVisitTime);
        mGourmetCart.setGourmetSubInformation(mCategory, mImageUrl);

        if (plusMenu(false, menuIndex) == true)
        {
            getViewInterface().setCartVisible(true);

            addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));
        } else
        {
            unLockAll();
        }
    }

    @Override
    public void onMenuOderCountPlusClick(int position)
    {
        if (position < 0 || mGourmetCart == null || lock() == true)
        {
            return;
        }

        GourmetMenu gourmetMenu = mGourmetMenuVisibleList.get(position);

        if (gourmetMenu == null)
        {
            unLockAll();
            return;
        }

        // 처음 메뉴 개수가 0인경우
        if (mGourmetCart.getMenuCount() == 0)
        {
            if (mVisitTime > 0)
            {
                mGourmetCart.setGourmetInformation(mGourmetIndex, mGourmetName, mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT), mVisitTime);
                mGourmetCart.setGourmetSubInformation(mCategory, mImageUrl);

                if (plusMenu(false, gourmetMenu.index) == true)
                {
                    getViewInterface().setCartVisible(true);

                    addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                    {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception
                        {
                            unLockAll();
                        }
                    }, new Consumer<Throwable>()
                    {
                        @Override
                        public void accept(Throwable throwable) throws Exception
                        {
                            unLockAll();
                        }
                    }));
                } else
                {
                    unLockAll();
                }
            } else
            {
                onChangeTimeClick(gourmetMenu.index);

                unLockAll();
            }
        } else
        {
            try
            {
                String message = null;
                final String actionAnalytics; // ga를 위한 값이다

                // 다른 매장으로 변경시 추가시
                if (mGourmetCart.gourmetIndex != mGourmetIndex)
                {
                    message = getString(R.string.message_gourmet_product_detail_change_cart_other_gourmet);
                    actionAnalytics = AnalyticsManager.Action.POPUP_DIFFERENT_SHOP;
                } else if (mGourmetCart.equalsDay(mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT)) != true)
                {
                    // 방문 일이 변경되는 경우
                    message = getString(R.string.message_gourmet_product_detail_change_cart_other_visit_day);
                    actionAnalytics = AnalyticsManager.Action.POPUP_DIFFERENT_DAY;
                } else if (mGourmetCart.visitTime != mVisitTime)
                {
                    // 방문 시간이 변경되는 경우
                    message = getString(R.string.message_gourmet_product_detail_add_menu_after_initialization);
                    actionAnalytics = AnalyticsManager.Action.POPUP_DIFFERENT_TIME;
                } else
                {
                    actionAnalytics = null;
                }

                if (DailyTextUtils.isTextEmpty(message) == true)
                {
                    mGourmetCart.setGourmetInformation(mGourmetIndex, mGourmetName, mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT), mVisitTime);
                    mGourmetCart.setGourmetSubInformation(mCategory, mImageUrl);

                    if (plusMenu(false, gourmetMenu.index) == true)
                    {
                        getViewInterface().setCartVisible(true);

                        addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                        {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception
                            {
                                unLockAll();
                            }
                        }, new Consumer<Throwable>()
                        {
                            @Override
                            public void accept(Throwable throwable) throws Exception
                            {
                                unLockAll();
                            }
                        }));
                    } else
                    {
                        unLockAll();
                    }
                } else
                {
                    getViewInterface().showSimpleDialog(null, message//
                        , getString(R.string.label_gourmet_product_detail_add_new_menu), getString(R.string.label_gourmet_product_detail_existing_menu), new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                // 장바구니 초기화 후 시간 변경 화면
                                mGourmetCart.clear();

                                getViewInterface().setCartVisible(false);

                                addCompositeDisposable(mCartLocalImpl.clearGourmetCart().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                                {
                                    @Override
                                    public void accept(Boolean aBoolean) throws Exception
                                    {
                                        if (mVisitTime > 0)
                                        {
                                            mGourmetCart.setGourmetInformation(mGourmetIndex, mGourmetName, mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT), mVisitTime);
                                            mGourmetCart.setGourmetSubInformation(mCategory, mImageUrl);

                                            if (plusMenu(false, gourmetMenu.index) == true)
                                            {
                                                getViewInterface().setCartVisible(true);

                                                addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
                                                {
                                                    @Override
                                                    public void accept(Boolean aBoolean) throws Exception
                                                    {
                                                        unLockAll();
                                                    }
                                                }, new Consumer<Throwable>()
                                                {
                                                    @Override
                                                    public void accept(Throwable throwable) throws Exception
                                                    {
                                                        unLockAll();
                                                    }
                                                }));
                                            } else
                                            {
                                                unLockAll();
                                            }
                                        } else
                                        {
                                            onChangeTimeClick(gourmetMenu.index);
                                        }
                                    }
                                }));

                                mAnalytics.onEventChangeCart(getActivity(), actionAnalytics, AnalyticsManager.Label.ADD_NEW);
                            }
                        }, new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                mAnalytics.onEventChangeCart(getActivity(), actionAnalytics, AnalyticsManager.Label.KEEP_EXISTED);
                            }
                        });

                    unLockAll();
                }
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }

    @Override
    public void onMenuOderCountMinusClick(int position)
    {
        if (position < 0 || mGourmetCart == null || mGourmetCart.gourmetIndex != mGourmetIndex || mGourmetCart.getMenuCount() <= 0 || lock() == true)
        {
            return;
        }

        GourmetMenu gourmetMenu = mGourmetMenuVisibleList.get(position);

        if (gourmetMenu == null)
        {
            unLockAll();
            return;
        }

        minusMenu(false, gourmetMenu.index);

        if (mGourmetCart.getMenuCount() == 0)
        {
            mGourmetCart.clear();

            getViewInterface().setCartVisible(false);

            addCompositeDisposable(mCartLocalImpl.clearGourmetCart().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));
        } else
        {
            addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));
        }
    }

    @Override
    public void onOpenCartMenusClick()
    {
        if (mGourmetCart == null || lock() == true)
        {
            return;
        }

        getViewInterface().setGourmetCart(mGourmetCart);
        Observable<Boolean> observable = getViewInterface().openCartMenus(mGourmetCart);

        if (observable == null)
        {
            unLockAll();
            return;
        }

        addCompositeDisposable(observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                getViewInterface().setSummeryCart(getString(R.string.label_gourmet_product_detail_booking_total_price//
                    , DailyTextUtils.getPriceFormat(getActivity(), mGourmetCart.getTotalPrice(), false))//
                    , mGourmetCart.getTotalCount());

                mOpenedCartMenus = true;

                unLockAll();

                mAnalytics.onEventOpenCartMenuClick(getActivity(), mGourmetIndex, mGourmetCart.getMenuCount());
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                unLockAll();
            }
        }));
    }

    @Override
    public void onCloseCartMenusClick()
    {
        if (mGourmetCart == null || lock() == true)
        {
            return;
        }

        Observable<Boolean> observable = getViewInterface().closeCartMenus();

        if (observable == null)
        {
            unLockAll();
            return;
        }

        addCompositeDisposable(observable.subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                getViewInterface().notifyGourmetMenusChanged();

                if (mGourmetCart == null || mGourmetCart.getMenuCount() == 0)
                {
                    getViewInterface().setCartVisible(false);
                } else
                {
                    getViewInterface().setSummeryCart(getString(R.string.label_gourmet_product_detail_check_the_menus), mGourmetCart.getTotalCount());
                }

                mOpenedCartMenus = false;

                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                getViewInterface().notifyGourmetMenusChanged();

                if (mGourmetCart == null || mGourmetCart.getMenuCount() == 0)
                {
                    getViewInterface().setCartVisible(false);
                }

                unLockAll();
            }
        }));
    }

    @Override
    public void onDeleteCartMenuClick(int menuIndex)
    {
        if (mGourmetCart == null || mGourmetCart.gourmetIndex != mGourmetIndex || lock() == true)
        {
            return;
        }

        mGourmetCart.remove(menuIndex);

        getViewInterface().setMenuOrderCount(menuIndex, 0, 0, 0, 0);

        if (mGourmetCart.getMenuCount() > 0)
        {
            // Cart
            getViewInterface().setSummeryCart(getString(R.string.label_gourmet_product_detail_booking_total_price//
                , DailyTextUtils.getPriceFormat(getActivity(), mGourmetCart.getTotalPrice(), false))//
                , mGourmetCart.getTotalCount());
            getViewInterface().setGourmetCart(mGourmetCart);
        } else
        {
            unLockAll();

            onCloseCartMenusClick();
        }

        addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
        {
            @Override
            public void accept(Boolean aBoolean) throws Exception
            {
                unLockAll();
            }
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                unLockAll();
            }
        }));
    }

    @Override
    public void onCartMenuMinusClick(int menuIndex)
    {
        if (mGourmetCart == null || mGourmetCart.gourmetIndex != mGourmetIndex || lock() == true)
        {
            return;
        }

        int menuCount = mGourmetCart.getMenuCount();

        minusMenu(true, menuIndex);

        GourmetCartMenu gourmetCartMenu = mGourmetCart.getGourmetCartMenu(menuIndex);

        if (mGourmetCart.getMenuOrderCount(menuIndex) > 0)
        {
            getViewInterface().setGourmetCartMenu(menuIndex, mGourmetCart.getMenuOrderCount(menuIndex), gourmetCartMenu.minimumOrderQuantity, gourmetCartMenu.maximumOrderQuantity, gourmetCartMenu.saleOrderQuantity);
        }

        // 메뉴 갯수가 변경된 경우
        if (mGourmetCart.getMenuCount() != menuCount)
        {
            getViewInterface().setGourmetCart(mGourmetCart);
        }

        if (mGourmetCart.getMenuCount() == 0)
        {
            mGourmetCart.clear();

            addCompositeDisposable(mCartLocalImpl.clearGourmetCart().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    unLockAll();

                    onCloseCartMenusClick();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();

                    onCloseCartMenusClick();
                }
            }));
        } else
        {
            addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));
        }
    }

    @Override
    public void onCartMenuPlusClick(int menuIndex)
    {
        if (mGourmetCart == null || mGourmetCart.gourmetIndex != mGourmetIndex || lock() == true)
        {
            return;
        }

        if (plusMenu(true, menuIndex) == true)
        {
            GourmetCartMenu gourmetCartMenu = mGourmetCart.getGourmetCartMenu(menuIndex);

            getViewInterface().setGourmetCartMenu(menuIndex, mGourmetCart.getMenuOrderCount(menuIndex), gourmetCartMenu.minimumOrderQuantity, gourmetCartMenu.maximumOrderQuantity, gourmetCartMenu.saleOrderQuantity);

            addCompositeDisposable(mCartLocalImpl.setGourmetCart(mGourmetCart).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>()
            {
                @Override
                public void accept(Boolean aBoolean) throws Exception
                {
                    unLockAll();
                }
            }, new Consumer<Throwable>()
            {
                @Override
                public void accept(Throwable throwable) throws Exception
                {
                    unLockAll();
                }
            }));
        } else
        {
            unLockAll();
        }
    }

    private void setToolbarTitle(String text)
    {
        getViewInterface().setToolbarTitle(text);
    }

    private void setToolbarTitle(int visitTime)
    {
        if (visitTime == GourmetDetailPresenter.FULL_TIME)
        {
            getViewInterface().setToolbarTitle(getString(R.string.label_gourmet_product_detail_operation_time_list));
        } else
        {
            getViewInterface().setToolbarTitle(DailyTextUtils.formatIntegerTimeToStringTime(visitTime) + " " + getString(R.string.label_menu));
        }
    }

    private void setGourmetBookDateTime(String visitDateTime)
    {
        if (DailyTextUtils.isTextEmpty(visitDateTime) == true)
        {
            return;
        }

        if (mGourmetBookDateTime == null)
        {
            mGourmetBookDateTime = new GourmetBookDateTime();
        }

        try
        {
            mGourmetBookDateTime.setVisitDateTime(visitDateTime);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    private void onChangeTimeClick(int menuIndex)
    {
        if (menuIndex < 0)
        {
            return;
        }

        GourmetMenu gourmetMenu = getGourmetMenu(menuIndex);

        if (gourmetMenu == null)
        {
            return;
        }

        List<Integer> operationTimeList = gourmetMenu.getOperationTimeList();

        getViewInterface().showTimePickerDialog(operationTimeList, menuIndex);
    }

    private synchronized boolean plusMenu(boolean openedCartMenus, int menuIndex)
    {
        if (menuIndex < 0 || mGourmetCart == null || mGourmetCart.gourmetIndex != mGourmetIndex//
            || mGourmetMenuVisibleList == null || mGourmetMenuVisibleList.size() == 0)
        {
            return false;
        }

        GourmetMenu gourmetMenu = getGourmetMenu(menuIndex);
        int currentMenuOrderCount = mGourmetCart.getMenuOrderCount(menuIndex);

        if (gourmetMenu.minimumOrderQuantity <= gourmetMenu.saleOrderQuantity)
        {
            // 처음 주문하는 경우 최소 수량 주문 필요.
            if (currentMenuOrderCount == 0)
            {
                for (int i = 0; i < gourmetMenu.minimumOrderQuantity; i++)
                {
                    mGourmetCart.plus(gourmetMenu);
                }
            } else if (currentMenuOrderCount >= gourmetMenu.maximumOrderQuantity)
            {
                // 주문 메뉴는 최대 개수 혹은 남은 개수를 초과하지 못한다.
                getViewInterface().showSimpleDialog(null, getString(R.string.message_gourmet_detail_max_quantity, gourmetMenu.maximumOrderQuantity), getString(R.string.dialog_btn_text_confirm), null);
                return false;
            } else if (currentMenuOrderCount >= gourmetMenu.saleOrderQuantity)
            {
                getViewInterface().showToast(R.string.message_gourmet_detail_not_enough_quantity, DailyToast.LENGTH_LONG);
            } else
            {
                mGourmetCart.plus(gourmetMenu);
            }
        } else
        {
            return false;
        }

        getViewInterface().setMenuOrderCount(menuIndex, mGourmetCart.getMenuOrderCount(menuIndex), gourmetMenu.minimumOrderQuantity, gourmetMenu.maximumOrderQuantity, gourmetMenu.saleOrderQuantity);

        getViewInterface().setSummeryCart(openedCartMenus ? getString(R.string.label_gourmet_product_detail_booking_total_price//
            , DailyTextUtils.getPriceFormat(getActivity(), mGourmetCart.getTotalPrice(), false))//
            : getString(R.string.label_gourmet_product_detail_check_the_menus), mGourmetCart.getTotalCount());

        return true;
    }

    private synchronized boolean minusMenu(boolean openedCartMenus, int menuIndex)
    {
        if (menuIndex < 0 || mGourmetCart == null || mGourmetCart.gourmetIndex != mGourmetIndex//
            || mGourmetMenuVisibleList == null || mGourmetMenuVisibleList.size() == 0)
        {
            return false;
        }

        GourmetMenu gourmetMenu = getGourmetMenu(menuIndex);
        int currentMenuOrderCount = mGourmetCart.getMenuOrderCount(menuIndex);

        if (currentMenuOrderCount > 0)
        {
            // 최소 수량 보다 작아지면 0으로
            if (currentMenuOrderCount - gourmetMenu.minimumOrderQuantity <= 0)
            {
                mGourmetCart.remove(gourmetMenu.index);
            } else
            {
                mGourmetCart.minus(gourmetMenu.index);
            }
        } else
        {
            return false;
        }

        getViewInterface().setMenuOrderCount(menuIndex, mGourmetCart.getMenuOrderCount(gourmetMenu.index), gourmetMenu.minimumOrderQuantity, gourmetMenu.maximumOrderQuantity, gourmetMenu.saleOrderQuantity);

        getViewInterface().setSummeryCart(openedCartMenus ? getString(R.string.label_gourmet_product_detail_booking_total_price//
            , DailyTextUtils.getPriceFormat(getActivity(), mGourmetCart.getTotalPrice(), false))//
            : getString(R.string.label_gourmet_product_detail_check_the_menus), mGourmetCart.getTotalCount());

        return true;
    }

    private GourmetMenu getGourmetMenu(int menuIndex)
    {
        if (menuIndex > 0)
        {
            for (GourmetMenu gourmetMenu : mGourmetMenuList)
            {
                if (gourmetMenu.index == menuIndex)
                {
                    return gourmetMenu;
                }
            }
        }

        return null;
    }

    private void notifyOperationTimeChanged()
    {
        if (mOperationTimeList == null)
        {
            return;
        }

        getViewInterface().setVisitTime(mVisitTime);

        mGourmetMenuVisibleList.clear();
        mGourmetMenuVisibleList.addAll(getGourmetMenuList(mVisitTime));

        if (mPosition >= mGourmetMenuVisibleList.size())
        {
            mPosition = 0;
        }

        getViewInterface().setGourmetMenus(mGourmetMenuVisibleList, mPosition);
        getViewInterface().setMenuIndicator(mPosition + 1, mGourmetMenuVisibleList.size());
    }

    private void notifyOperationTimeChanged(int menuIndex)
    {
        if (mOperationTimeList == null || menuIndex < 0)
        {
            return;
        }

        getViewInterface().setVisitTime(mVisitTime);

        mGourmetMenuVisibleList.clear();
        mGourmetMenuVisibleList.addAll(getGourmetMenuList(mVisitTime));

        int size = mGourmetMenuVisibleList.size();

        if (mPosition >= size)
        {
            mPosition = 0;
        }

        for (int i = 0; i < size; i++)
        {
            if (mGourmetMenuVisibleList.get(i).index == menuIndex)
            {
                mPosition = i;
            }
        }

        getViewInterface().setGourmetMenus(mGourmetMenuVisibleList, mPosition);
        getViewInterface().setMenuIndicator(mPosition + 1, mGourmetMenuVisibleList.size());
    }

    private List<GourmetMenu> getGourmetMenuList(int time)
    {
        List<GourmetMenu> gourmetMenuList = new ArrayList<>();

        for (GourmetMenu gourmetMenu : mGourmetMenuList)
        {
            gourmetMenu.orderCount = 0;
            gourmetMenu.visible = true;
        }

        if (time == GourmetDetailPresenter.FULL_TIME)
        {
            gourmetMenuList.addAll(mGourmetMenuList);
        } else
        {
            for (GourmetMenu gourmetMenu : mGourmetMenuList)
            {
                for (int operationTime : gourmetMenu.getOperationTimeList())
                {
                    if (operationTime == time)
                    {
                        gourmetMenu.visible = true;

                        gourmetMenuList.add(gourmetMenu);

                        try
                        {
                            if (mGourmetCart != null && mGourmetCart.equalsDay(mGourmetBookDateTime.getVisitDateTime(DailyCalendar.ISO_8601_FORMAT)) == true//
                                && mGourmetCart.gourmetIndex == mGourmetIndex && mGourmetCart.visitTime == time)
                            {
                                gourmetMenu.orderCount = mGourmetCart.getMenuOrderCount(gourmetMenu.index);
                            }
                        } catch (Exception e)
                        {
                            ExLog.e(e.toString());
                        }
                        break;
                    }
                }
            }
        }

        return gourmetMenuList;
    }
}
