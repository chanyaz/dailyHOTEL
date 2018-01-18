package com.twoheart.dailyhotel.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.daily.base.util.DailyTextUtils;
import com.daily.base.util.ExLog;
import com.daily.dailyhotel.storage.preference.DailyRemoteConfigPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.Setting;
import com.twoheart.dailyhotel.util.Constants;
import com.twoheart.dailyhotel.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class DailyRemoteConfig
{
    Context mContext;
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    public interface OnCompleteListener
    {
        void onComplete(String currentVersion, String forceVersion);
    }

    public DailyRemoteConfig(Context context)
    {
        mContext = context;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(Constants.DEBUG).build());
    }

    public void requestRemoteConfig(final OnCompleteListener listener)
    {
        if (DailyTextUtils.isTextEmpty(DailyRemoteConfigPreference.getInstance(mContext).getRemoteConfigCompanyName()) == true)
        {
            writeCompanyInformation(mContext, mContext.getString(R.string.default_company_information));
        }

        long fetchTime = 600L;

        if (Constants.DEBUG == true)
        {
            fetchTime = 0L;
        }

        mFirebaseRemoteConfig.fetch(fetchTime).addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful() == true)
                {
                    mFirebaseRemoteConfig.activateFetched();
                } else
                {
                    return;
                }

                setConfig(listener);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                if (e instanceof FirebaseRemoteConfigFetchThrottledException == true)
                {
                    try
                    {
                        setConfig(listener);
                        return;
                    } catch (Exception e1)
                    {
                        Crashlytics.logException(e1);
                    }
                } else
                {
                    Crashlytics.logException(e);
                }

                // 버전이 업데이트 되는 경우 텍스트의 내용을 다시 넣는 것을 수행한다.
                writeTextFiled(mContext, DailyRemoteConfigPreference.getInstance(mContext).getRemoteConfigText());
                listener.onComplete(null, null);
            }
        });
    }

    void setConfig(final OnCompleteListener listener)
    {
        String androidUpdateVersion = mFirebaseRemoteConfig.getString("androidUpdateVersion");
        String androidPaymentType = mFirebaseRemoteConfig.getString("androidPaymentType");
        String companyInfo = mFirebaseRemoteConfig.getString("companyInfo");
        String androidSplashImageUrl = mFirebaseRemoteConfig.getString("androidSplashImageLink");
        String androidSplashImageUpdateTime = mFirebaseRemoteConfig.getString("androidSplashImageUpdateTime");
        String androidText = mFirebaseRemoteConfig.getString("androidText");
        String androidHomeEventDefaultLink = mFirebaseRemoteConfig.getString("androidHomeEventDefaultLink");
        String androidBoutiqueBM = mFirebaseRemoteConfig.getString("androidBoutiqueBM");
        String androidStaticUrl = mFirebaseRemoteConfig.getString("androidStaticUrl");
        String androidStayRankABTest = mFirebaseRemoteConfig.getString("androidStayRankABTest");
        String androidOBSearchKeyword = mFirebaseRemoteConfig.getString("androidOBSearchKeyword");
        String androidRewardSticker = mFirebaseRemoteConfig.getString("androidRewardSticker");
        String androidAppResearch = mFirebaseRemoteConfig.getString("androidAppResearch");
        String androidGourmetSearchKeyword = mFirebaseRemoteConfig.getString("androidGourmetSearchKeyword");
        String androidPaymentCardEvent = mFirebaseRemoteConfig.getString("androidPaymentCardEvent");

        if (Constants.DEBUG == true)
        {
            try
            {
                ExLog.d("androidUpdateVersion : " + new JSONObject(androidUpdateVersion).toString());
                ExLog.d("androidPaymentType : " + new JSONObject(androidPaymentType).toString());
                ExLog.d("companyInfo : " + new JSONObject(companyInfo).toString());
                ExLog.d("androidSplashImageLink : " + new JSONObject(androidSplashImageUrl).toString());
                ExLog.d("androidSplashImageUpdateTime : " + new JSONObject(androidSplashImageUpdateTime).toString());
                ExLog.d("androidText : " + new JSONObject(androidText).toString());
                ExLog.d("androidHomeEventDefaultLink : " + new JSONObject(androidHomeEventDefaultLink).toString());
                ExLog.d("androidBoutiqueBM : " + new JSONObject(androidBoutiqueBM).toString());
                ExLog.d("androidStaticUrl : " + new JSONObject(androidStaticUrl).toString());

                if (DailyTextUtils.isTextEmpty(androidStayRankABTest) == true)
                {
                    ExLog.d("androidStayRankABTest : ");
                } else
                {
                    ExLog.d("androidStayRankABTest : " + new JSONObject(androidStayRankABTest).toString());
                }

                if (DailyTextUtils.isTextEmpty(androidOBSearchKeyword) == true)
                {
                    ExLog.d("androidOBSearchKeyword : ");
                } else
                {
                    ExLog.d("androidOBSearchKeyword : " + new JSONObject(androidOBSearchKeyword).toString());
                }

                if (DailyTextUtils.isTextEmpty(androidRewardSticker) == true)
                {
                    ExLog.d("androidReward : ");
                } else
                {
                    ExLog.d("androidReward : " + new JSONObject(androidRewardSticker).toString());
                }

                ExLog.d("androidAppResearch : " + androidAppResearch);
                ExLog.d("androidGourmetSearchKeyword : " + androidGourmetSearchKeyword);
                ExLog.d("androidPaymentCardEvent : " + androidPaymentCardEvent);
            } catch (Exception e)
            {
                ExLog.d(e.toString());
            }
        }

        // 버전
        String currentVersion = null, forceVersion = null;

        try
        {
            JSONObject versionJSONObject = new JSONObject(androidUpdateVersion);
            JSONObject versionCode = versionJSONObject.getJSONObject("versionCode");

            switch (Setting.getStore())
            {
                case PLAY_STORE:
                {
                    JSONObject jsonObject = versionCode.getJSONObject("play");
                    currentVersion = jsonObject.getString("current");
                    forceVersion = jsonObject.getString("force");
                    break;
                }

                case T_STORE:
                {
                    JSONObject jsonObject = versionCode.getJSONObject("one");
                    currentVersion = jsonObject.getString("current");
                    forceVersion = jsonObject.getString("force");
                    break;
                }
            }
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }

        writeCompanyInformation(mContext, companyInfo);
        writePaymentType(mContext, androidPaymentType);

        //
        DailyRemoteConfigPreference.getInstance(mContext).setRemoteConfigText(androidText);
        writeTextFiled(mContext, androidText);

        // 이미지 로딩 관련(추후 진행)
        processSplashImage(mContext, androidSplashImageUpdateTime, androidSplashImageUrl);

        // default Event link
        writeHomeEventDefaultLink(mContext, androidHomeEventDefaultLink);

        // boutique BM - test BM
        writeBoutiqueBM(mContext, androidBoutiqueBM);

        // androidStaticUrl
        writeStaticUrl(mContext, androidStaticUrl);

        // Android Stay Rank A/B Test
        writeStayRankTest(mContext, androidStayRankABTest);

        writeOBSearchKeyword(mContext, androidOBSearchKeyword);

        // Reward Sticker
        writeRewardSticker(mContext, androidRewardSticker);

        // 앱조사
        writeAppResearch(mContext, androidAppResearch);

        // 고메 키워드
        writeGourmetSearchKeyword(mContext, androidGourmetSearchKeyword);

        // 카드 이벤트 업체
        writePaymentCardEvent(mContext, androidPaymentCardEvent);

        if (listener != null)
        {
            listener.onComplete(currentVersion, forceVersion);
        }
    }

    void processSplashImage(Context context, String updateTime, String imageUrl)
    {
        if (DailyTextUtils.isTextEmpty(updateTime, imageUrl) == true)
        {
            return;
        }

        // 이미지 로딩 관련
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        String dpi;

        if (densityDpi < 240)
        {
            dpi = "hdpi";
        } else if (densityDpi <= 480)
        {
            dpi = "xhdpi";
        } else
        {
            dpi = "xxxhdpi";
        }

        try
        {
            JSONObject updateTimeJSONObject = new JSONObject(updateTime);
            JSONObject imageUrlJSONObject = new JSONObject(imageUrl);

            // 아직은 고려하지 않도록 한다."default"
            //            String defaultType = imageUrlJSONObject.getString("default");
            String url = imageUrlJSONObject.getJSONObject("image").getString(dpi);
            String currentVersion = DailyRemoteConfigPreference.getInstance(context).getRemoteConfigIntroImageVersion();
            String newVersion = updateTimeJSONObject.getString("time");

            if (DailyTextUtils.isTextEmpty(newVersion) == true)
            {
                // 리모트 컨피그에 new version 을 실수로 넣지 않았을때 - 기존 버전이 있을 경우 유지! 없을때는 디폴트 버전 셋팅!
                if (DailyTextUtils.isTextEmpty(currentVersion) == true)
                {
                    DailyRemoteConfigPreference.getInstance(context).setRemoteConfigIntroImageVersion(Constants.DAILY_INTRO_DEFAULT_VERSION);
                } else
                {
                    DailyRemoteConfigPreference.getInstance(context).setRemoteConfigIntroImageVersion(currentVersion);
                }
            } else if (Constants.DAILY_INTRO_DEFAULT_VERSION.equalsIgnoreCase(newVersion) == true)
            {
                // 앱의 스플래쉬 버전이 기본 버전일때
                DailyRemoteConfigPreference.getInstance(context).setRemoteConfigIntroImageVersion(Constants.DAILY_INTRO_DEFAULT_VERSION);
            } else if (DailyTextUtils.isTextEmpty(currentVersion) == true || currentVersion.compareTo(newVersion) < 0)
            {
                // 앱의 스플래쉬 기존 버전이 없거나 새버전이 현재 버전보다 클 경우 다운로드를 시도
                new SplashImageDownloadAsyncTask(context).execute(url, newVersion);
            } else
            {
                // 앱의 스플래쉬 버전이 현재 버전과 같거나 작을때
                DailyRemoteConfigPreference.getInstance(context).setRemoteConfigIntroImageVersion(currentVersion);
            }
        } catch (JSONException e)
        {
            ExLog.d(e.toString());
        }
    }

    void writeCompanyInformation(Context context, String companyInfo)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(companyInfo);

            String companyName = jsonObject.getString("name");
            String companyCEO = jsonObject.getString("ceo");
            String companyBizRegNumber = jsonObject.getString("bizRegNumber");
            String companyItcRegNumber = jsonObject.getString("itcRegNumber");
            String address = jsonObject.getString("address1");
            String phoneNumber = jsonObject.getString("phoneNumber1");
            String fax = jsonObject.getString("fax1");
            String privacyEmail = jsonObject.getString("privacyManager");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigCompanyInformation(companyName//
                , companyCEO, companyBizRegNumber, companyItcRegNumber, address, phoneNumber, fax, privacyEmail);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void writePaymentType(Context context, String androidPaymentType)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(androidPaymentType);
            JSONObject stayJSONObject = jsonObject.getJSONObject("stay");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStaySimpleCardPaymentEnabled(stayJSONObject.getBoolean("easyCard"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStayCardPaymentEnabled(stayJSONObject.getBoolean("card"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStayPhonePaymentEnabled(stayJSONObject.getBoolean("phoneBill"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStayVirtualPaymentEnabled(stayJSONObject.getBoolean("virtualAccount"));

            JSONObject gourmetJSONObject = jsonObject.getJSONObject("gourmet");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigGourmetSimpleCardPaymentEnabled(gourmetJSONObject.getBoolean("easyCard"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigGourmetCardPaymentEnabled(gourmetJSONObject.getBoolean("card"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigGourmetPhonePaymentEnabled(gourmetJSONObject.getBoolean("phoneBill"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigGourmetVirtualPaymentEnabled(gourmetJSONObject.getBoolean("virtualAccount"));

            JSONObject stayOutboundJSONObject = jsonObject.getJSONObject("stayOutbound");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStayOutboundSimpleCardPaymentEnabled(stayOutboundJSONObject.getBoolean("easyCard"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStayOutboundCardPaymentEnabled(stayOutboundJSONObject.getBoolean("card"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigStayOutboundPhonePaymentEnabled(stayOutboundJSONObject.getBoolean("phoneBill"));
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void writeTextFiled(Context context, String textInformation)
    {
        if (context == null || DailyTextUtils.isTextEmpty(textInformation) == true)
        {
            return;
        }

        try
        {
            JSONObject jsonObject = new JSONObject(textInformation);

            String version = jsonObject.getString("version");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigTextVersion(version);
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigTextLoginText01(jsonObject.getString("loginText01"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigTextSignUpText01(jsonObject.getString("signupText01"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigTextSignUpText02(jsonObject.getString("signupText02"));

            // 홈 메시지 추가 영역
            JSONObject homeJSONObject = jsonObject.getJSONObject("home");
            JSONObject messageAreaJSONObject = homeJSONObject.getJSONObject("messageArea");
            JSONObject loginJSONObject = messageAreaJSONObject.getJSONObject("login");
            JSONObject logoutJSONObject = messageAreaJSONObject.getJSONObject("logout");
            JSONObject categoryAreaJsonObject = homeJSONObject.getJSONObject("categoryArea");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeMessageAreaLoginEnabled(loginJSONObject.getBoolean("enabled"));

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeMessageAreaLogoutEnabled(logoutJSONObject.getBoolean("enabled"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeMessageAreaLogoutTitle(logoutJSONObject.getString("title"));
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeMessageAreaLogoutCallToAction(logoutJSONObject.getString("callToAction"));

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeCategoryEnabled(categoryAreaJsonObject.getBoolean("enabled"));

            // 업데이트 메시지
            JSONObject updateJSONObject = jsonObject.getJSONObject("updateMessage");
            JSONObject optionalJSONObject = updateJSONObject.getJSONObject("optional");
            JSONObject forceJSONObject = updateJSONObject.getJSONObject("force");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigUpdateOptional(optionalJSONObject.toString());
            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigUpdateForce(forceJSONObject.toString());

            JSONObject operationLunchTimeJsonObject = jsonObject.getJSONObject("operationLunchTime");
            String startTime = operationLunchTimeJsonObject.getString("startTime");
            String endTime = operationLunchTimeJsonObject.getString("endTime");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigOperationLunchTime(String.format(Locale.KOREA, "%s,%s", startTime, endTime));
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void writeHomeEventDefaultLink(final Context context, String androidHomeEventDefaultLink)
    {
        String title;
        String eventUrl;
        int index;
        try
        {
            JSONObject eventJSONObject = new JSONObject(androidHomeEventDefaultLink);

            title = eventJSONObject.getString("title");
            eventUrl = eventJSONObject.getString("eventUrl");
            index = eventJSONObject.getInt("index");
        } catch (Exception e)
        {
            ExLog.e(e.toString());
            title = null;
            eventUrl = null;
            index = -1;
        }

        final String eventTitle = title;
        final String eventLinkUrl = eventUrl;
        final int eventIndex = index;

        final String clientHomeEventCurrentVersion = DailyRemoteConfigPreference.getInstance(context).getRemoteConfigHomeEventCurrentVersion();

        processImage(context, clientHomeEventCurrentVersion, androidHomeEventDefaultLink, new ImageDownloadAsyncTask.OnCompletedListener()
        {
            @Override
            public void onCompleted(boolean result, String version)
            {
                if (result == true)
                {
                    // 이전 파일 삭제
                    if (DailyTextUtils.isTextEmpty(clientHomeEventCurrentVersion) == false)
                    {
                        String fileName = Util.makeImageFileName(clientHomeEventCurrentVersion);
                        File currentFile = new File(context.getCacheDir(), fileName);
                        if (currentFile.exists() == true && currentFile.delete() == false)
                        {
                            currentFile.deleteOnExit();
                        }
                    }

                    DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeEventCurrentVersion(version);
                    DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeEventTitle(eventTitle);
                    DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeEventUrl(eventLinkUrl);
                    DailyRemoteConfigPreference.getInstance(context).setRemoteConfigHomeEventIndex(eventIndex);
                }
            }
        });
    }

    void processImage(Context context, String clientVersion, String jsonObject, ImageDownloadAsyncTask.OnCompletedListener onCompleteListener)
    {
        if (DailyTextUtils.isTextEmpty(jsonObject) == true)
        {
            return;
        }

        // 이미지 로딩 관련
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        String dpi;

        if (densityDpi <= 480)
        {
            dpi = "lowResolution";
        } else
        {
            dpi = "highResolution";
        }

        if (DailyTextUtils.isTextEmpty(clientVersion) == true)
        {
            clientVersion = "";
        }

        try
        {
            JSONObject imageJSONObject = new JSONObject(jsonObject);
            String version = imageJSONObject.getString("version");

            if (clientVersion.equalsIgnoreCase(version) == true)
            {
                return;
            }

            String url = imageJSONObject.getString(dpi);

            new ImageDownloadAsyncTask(context, version, onCompleteListener).execute(url);
        } catch (JSONException e)
        {
            ExLog.d(e.toString());
        }
    }

    void writeBoutiqueBM(final Context context, String androidBoutiqueBM)
    {
        if (context == null || DailyTextUtils.isTextEmpty(androidBoutiqueBM) == true)
        {
            return;
        }

        try
        {
            JSONObject jsonObject = new JSONObject(androidBoutiqueBM);

            boolean enabled = jsonObject.getBoolean("enabled");

            DailyRemoteConfigPreference.getInstance(context).setRemoteConfigBoutiqueBMEnabled(enabled);

        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void writeStaticUrl(Context context, String jsonString)
    {
        if (context == null || DailyTextUtils.isTextEmpty(jsonString) == true)
        {
            return;
        }

        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);

            String privacyUrl = jsonObject.getString("privacy");
            String collectPersonalInformation = jsonObject.getString("collectPersonalInformation");
            String termsUrl = jsonObject.getString("terms");
            String aboutUrl = jsonObject.getString("about");
            String locationUrl = jsonObject.getString("location");
            String childProtectUrl = jsonObject.getString("childProtect");
            String bonusUrl = jsonObject.getString("bonus");
            String couponUrl = jsonObject.getString("coupon");
            String prodCouponNoteUrl = jsonObject.getString("prodCouponNote");
            String devCouponNoteUrl = jsonObject.getString("devCouponNote");
            String faqUrl = jsonObject.getString("faq");
            String licenseUrl = jsonObject.getString("license");
            String reviewUrl = jsonObject.getString("review");
            String lifeStyleProjectUrl = jsonObject.getString("lifeStyleProject");
            String dailyReward = jsonObject.getString("dailyReward");
            String dailyRewardTerms = jsonObject.getString("dailyRewardTerms");
            String dailyRewardCouponTerms = jsonObject.getString("dailyRewardCouponTerms");
            String dailyTrueAwards = jsonObject.getString("dailyTrueAwards");

            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlPrivacy(privacyUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlTerms(termsUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlAbout(aboutUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlLocation(locationUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlChildProtect(childProtectUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlBonus(bonusUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlCoupon(couponUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlProdCouponNote(prodCouponNoteUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlDevCouponNote(devCouponNoteUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlFaq(faqUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlLicense(licenseUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlReview(reviewUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlLifeStyleProject(lifeStyleProjectUrl);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlCollectPersonalInformation(collectPersonalInformation);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlDailyReward(dailyReward);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlDailyRewardTerms(dailyRewardTerms);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlDailyRewardCouponTerms(dailyRewardCouponTerms);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStaticUrlDailyTrueAwards(dailyTrueAwards);
        } catch (Exception e)
        {
            ExLog.e(e.toString());
        }
    }

    void writeStayRankTest(Context context, String jsonString)
    {
        if (context == null)
        {
            return;
        }

        if (DailyTextUtils.isTextEmpty(jsonString) == true)
        {
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStayRankTestName(null);
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStayRankTestType(null);
        } else
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonString);

                String name;
                String type;
                boolean enabled = jsonObject.getBoolean("enabled");

                if (enabled == true)
                {
                    name = jsonObject.getString("name");
                    type = jsonObject.getString("type");
                } else
                {
                    name = null;
                    type = null;
                }

                if (Constants.DEBUG == true)
                {
                    ExLog.d("pinkred - name " + name + ", " + type);
                }

                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStayRankTestName(name);
                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigStayRankTestType(type);
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }

    void writeOBSearchKeyword(Context context, String jsonString)
    {
        if (context == null)
        {
            return;
        }

        if (DailyTextUtils.isTextEmpty(jsonString) == true)
        {
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigObSearchKeyword(null);
        } else
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonString);

                if (Constants.DEBUG == true)
                {
                    ExLog.d("pinkred - keyword " + jsonObject);
                }

                if (jsonObject.has("keyword") == false)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigObSearchKeyword(null);
                    return;
                }

                JSONArray jsonArray = jsonObject.getJSONArray("keyword");

                if (jsonArray != null && jsonArray.length() > 0)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigObSearchKeyword(jsonArray.toString());
                }
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }

    void writeRewardSticker(Context context, String jsonString)
    {
        if (context == null)
        {
            return;
        }

        if (DailyTextUtils.isTextEmpty(jsonString) == false)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonString);

                if (Constants.DEBUG == true)
                {
                    ExLog.d("pinkred - reward sticker " + jsonObject);
                }

                String cardTitleMessage = jsonObject.getString("cardTitleMessage");
                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerCardTitleMessage(cardTitleMessage);

                String rewardTitleMessage = jsonObject.getString("rewardTitleMessage");
                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerRewardTitleMessage(rewardTitleMessage);

                boolean campaignEnabled = jsonObject.getBoolean("campaignEnabled");
                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerCampaignEnabled(campaignEnabled);

                JSONArray guidesJSONArray = jsonObject.getJSONArray("guides");

                if (guidesJSONArray == null || guidesJSONArray.length() == 0)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerGuides(null);
                } else
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerGuides(guidesJSONArray.toString());
                }

                JSONObject nonMemberJSONObject = jsonObject.getJSONObject("nonMember");
                JSONObject nonMemberMessageJSONObject = nonMemberJSONObject.getJSONObject("message");
                String nonMemberDefaultMessage = nonMemberMessageJSONObject.getString("default");
                String nonMemberCampaignMessage = nonMemberMessageJSONObject.getString("campaign");
                int campaignFreeNights = nonMemberJSONObject.getInt("campaignFreeNights");

                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerNonMemberDefaultMessage(nonMemberDefaultMessage);
                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerNonMemberCampaignMessage(nonMemberCampaignMessage);
                DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerNonmemberCampaignFreeNights(campaignFreeNights);

                JSONObject memberMessageJSONObject = jsonObject.getJSONObject("member").getJSONObject("message");
                final int MAX_NIGHTS = 9;

                for (int i = 0; i <= MAX_NIGHTS; i++)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigRewardStickerMemberMessage(i, memberMessageJSONObject.getString(Integer.toString(i)));
                }
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }

    private void writeAppResearch(Context context, String jsonString)
    {
        if (context == null)
        {
            return;
        }

        DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigAppResearch(jsonString);
    }

    private void writeGourmetSearchKeyword(Context context, String jsonString)
    {
        if (context == null)
        {
            return;
        }

        if (DailyTextUtils.isTextEmpty(jsonString) == true)
        {
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigGourmetSearchKeyword(null);
        } else
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonString);

                if (Constants.DEBUG == true)
                {
                    ExLog.d("pinkred - keyword " + jsonObject);
                }

                if (jsonObject.has("keyword") == false)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigGourmetSearchKeyword(null);
                    return;
                }

                JSONArray jsonArray = jsonObject.getJSONArray("keyword");

                if (jsonArray != null && jsonArray.length() > 0)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigGourmetSearchKeyword(jsonArray.toString());
                }
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }

    private void writePaymentCardEvent(Context context, String jsonString)
    {
        if (context == null)
        {
            return;
        }

        if (DailyTextUtils.isTextEmpty(jsonString) == true)
        {
            DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigPaymentCardEvent(null);
        } else
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonString);

                if (Constants.DEBUG == true)
                {
                    ExLog.d("pinkred - " + jsonObject);
                }

                if (jsonObject.getBoolean("enabled") == false)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigPaymentCardEvent(null);
                    return;
                }

                JSONArray jsonArray = jsonObject.getJSONArray("cardEvents");

                if (jsonArray != null && jsonArray.length() > 0)
                {
                    DailyRemoteConfigPreference.getInstance(context).setKeyRemoteConfigPaymentCardEvent(jsonArray.toString());
                }
            } catch (Exception e)
            {
                ExLog.e(e.toString());
            }
        }
    }
}