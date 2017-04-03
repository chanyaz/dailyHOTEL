package com.daily.dailyhotel.screen.mydaily.profile;

import android.content.Context;
import android.support.annotation.NonNull;

import com.daily.base.BaseException;
import com.daily.dailyhotel.domain.ProfileInterface;
import com.daily.dailyhotel.entity.User;
import com.twoheart.dailyhotel.network.DailyMobileAPI;

import io.reactivex.Observable;

public class ProfileNetworkImpl implements ProfileInterface
{
    private Context mContext;

    public ProfileNetworkImpl(@NonNull Context context)
    {
        mContext = context;
    }

    @Override
    public Observable<User> getProfile()
    {
        //        return DailyMobileAPI.getInstance(mContext).getUserProfile().map(new Function<BaseDto<UserData>, User>()
        //        {
        //            @Override
        //            public User apply(BaseDto<UserData> userDataBaseDto) throws Exception
        //            {
        //                User user = null;
        //
        //                if (userDataBaseDto != null)
        //                {
        //                    if (userDataBaseDto.msgCode == 100 && userDataBaseDto.data != null)
        //                    {
        //                        user = userDataBaseDto.data.getUser();
        //                    } else
        //                    {
        //                        throw new BaseException(userDataBaseDto.msgCode, userDataBaseDto.msg);
        //                    }
        //                } else
        //                {
        //                    throw new BaseException(-1, null);
        //                }
        //
        //                return user;
        //            }
        //        });

        return DailyMobileAPI.getInstance(mContext).getUserProfile().map((userDataBaseDto) ->
        {
            User user = null;

            if (userDataBaseDto != null)
            {
                if (userDataBaseDto.msgCode == 100 && userDataBaseDto.data != null)
                {
                    user = userDataBaseDto.data.getUser();
                } else
                {
                    throw new BaseException(userDataBaseDto.msgCode, userDataBaseDto.msg);
                }
            } else
            {
                throw new BaseException(-1, null);
            }

            return user;
        });
    }
}
