package com.twoheart.dailyhotel;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.base.util.VersionUtils;
import com.daily.base.widget.DailyToast;
import com.facebook.login.LoginManager;
import com.kakao.usermgmt.UserManagement;
import com.twoheart.dailyhotel.screen.main.MainActivity;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.DailyDeepLink;
import com.twoheart.dailyhotel.util.DailyExternalDeepLink;
import com.daily.dailyhotel.storage.preference.DailyPreference;
import com.daily.dailyhotel.storage.preference.DailyUserPreference;
import com.twoheart.dailyhotel.util.Util;
import com.twoheart.dailyhotel.util.analytics.AnalyticsManager;

public class LauncherActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (VersionUtils.isOverAPI21() == true && VersionUtils.isOverAPI23() == false)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.statusbar_background));
        }

        DailyPreference.getInstance(this).setBackgroundAppTime(0);

        Intent intent = getIntent();

        if (intent == null)
        {
            return;
        }

        Util.initializeMemory();

        if (DailyPreference.getInstance(this).getTrueVRSupport() == 0)
        {
            boolean support = Util.verifyTrueView(this);

            DailyPreference.getInstance(this).setTrueVRSupport(support ? 1 : -1);
        }

        // 개인 정보 방침에 따른 개인 정보 암호화.
        if (DailyTextUtils.isTextEmpty(DailyPreference.getInstance(this).getUserType()) == false)
        {
            DailyUserPreference.getInstance(this).requestMigration(this);
            DailyPreference.getInstance(this).clearUserInformation();
        }

        // 리뷰 초기화
        DailyPreference.getInstance(this).setIsRequestReview(false);

        // 스테이 카테고리 정보를 초기화한다.
        DailyPreference.getInstance(this).setStayCategory(null, null);

        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);

        Uri uri = getIntent().getData();

        if (uri != null)
        {
            DailyDeepLink dailyDeepLink = DailyDeepLink.getNewInstance(uri);

            if (dailyDeepLink != null && dailyDeepLink.isExternalDeepLink() == true)
            {
                AnalyticsManager.getInstance(this).startDeepLink(uri);

                DailyExternalDeepLink externalDeepLink = (DailyExternalDeepLink) dailyDeepLink;

                if (externalDeepLink.isValidateLink() == true)
                {
                    if (Constants.DEBUG == true)
                    {
                        String baseUrl = externalDeepLink.getBaseUrl();
                        String baseOutBoundUrl = externalDeepLink.getBaseOutBoundUrl();

                        if (DailyTextUtils.isTextEmpty(baseUrl, baseOutBoundUrl) == false)
                        {
                            logOut();
                            DailyPreference.getInstance(this).setBaseUrl(baseUrl);
                            DailyPreference.getInstance(this).setBaseOutBoundUrl(baseOutBoundUrl);
                            externalDeepLink.clear();
                            Util.restartExitApp(this);
                            return;
                        }
                    }

                    AnalyticsManager.getInstance(this).recordDeepLink(externalDeepLink);

                    newIntent.setData(uri);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }

                dailyDeepLink.clear();
            }
        }

        startActivity(newIntent);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.hold);
        finish();
    }

    private void logOut()
    {
        DailyPreference.getInstance(this).clear();
        DailyUserPreference.getInstance(this).clear();

        try
        {
            LoginManager.getInstance().logOut();
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        try
        {
            UserManagement.requestLogout(null);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        DailyToast.showToast(this, R.string.toast_msg_logouted, Toast.LENGTH_SHORT);

        // Adjust에서 로그아웃시 기존 정보를 보냄으로 이벤트 발생후 삭제 필요.
        AnalyticsManager.getInstance(this).recordScreen(this, AnalyticsManager.Screen.MENU_LOGOUT_COMPLETE, null);
        AnalyticsManager.getInstance(this).setUserInformation(AnalyticsManager.ValueType.EMPTY, AnalyticsManager.ValueType.EMPTY);
    }
}
