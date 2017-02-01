package com.twoheart.dailyhotel.place.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.twoheart.dailyhotel.network.DailyMobileAPI;
import com.twoheart.dailyhotel.util.Constants;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;

public abstract class BaseFragment extends Fragment implements Constants
{
    protected String mNetworkTag;

    public BaseFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mNetworkTag = getClass().getName();
    }

    @Override
    public void onDestroy()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        DailyMobileAPI.getInstance(baseActivity).cancelAll(baseActivity, mNetworkTag);

        super.onDestroy();
    }

    public void onError(Throwable e)
    {
        unLockUI();

        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.onError(e);
    }

    public void onErrorResponse(Call call, Response<JSONObject> response)
    {
        unLockUI();

        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.onErrorResponse(call, response);
    }

    protected void onErrorPopupMessage(int msgCode, String message)
    {
        unLockUI();

        final BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.onErrorPopupMessage(msgCode, message);
    }

    protected void onErrorPopupMessage(int msgCode, String message, View.OnClickListener listener)
    {
        unLockUI();

        final BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.onErrorPopupMessage(msgCode, message, listener);
    }

    protected void onErrorToastMessage(String message)
    {
        unLockUI();

        final BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.onErrorToastMessage(message);
    }

    public void lockUI()
    {
        lockUI(true);
    }

    public void lockUI(boolean isShowProgress)
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.lockUI(isShowProgress);
    }

    public void unLockUI()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.unLockUI();
    }

    public void lockUIImmediately()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.lockUIImmediately();
    }

    /**
     * UI Component의 잠금 상태를 확인하는 변수..
     *
     * @return
     */
    protected boolean isLockUiComponent()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return true;
        }

        return baseActivity.isLockUiComponent();
    }

    /**
     * UI Component를 잠금상태로 변경..
     */
    protected void lockUiComponent()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.lockUiComponent();
    }

    public boolean lockUiComponentAndIsLockUiComponent()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return true;
        }

        return baseActivity.lockUiComponentAndIsLockUiComponent();
    }

    /**
     * UI Component를 잠금해제로 변경..
     */
    protected void releaseUiComponent()
    {
        BaseActivity baseActivity = (BaseActivity) getActivity();

        if (baseActivity == null)
        {
            return;
        }

        baseActivity.releaseUiComponent();
    }

    protected boolean isFinishing()
    {
        Activity activity = getActivity();

        return (isAdded() == false || activity == null//
            || activity.isFinishing() == true);
    }
}
