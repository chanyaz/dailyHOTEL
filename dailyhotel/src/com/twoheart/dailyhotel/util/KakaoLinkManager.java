package com.twoheart.dailyhotel.util;

import com.kakao.AppActionBuilder;
import com.kakao.AppActionInfoBuilder;
import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoTalkLinkMessageBuilder;
import com.kakao.internal.Action;
import com.kakao.internal.AppActionInfo;
import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.model.HotelDetail;

import android.content.Context;

public class KakaoLinkManager implements Constants{
	private static final String TAG = "KakaloLinkManager";

	public static KakaoLinkManager newInstance(Context context) {
		return new KakaoLinkManager(context);
	}

	private KakaoLink kkLink;
	private KakaoTalkLinkMessageBuilder kkMsgBuilder;
	private Context mContext;

	private KakaoLinkManager(Context context) {
		try {
			mContext = context;
			kkLink = KakaoLink.getKakaoLink(mContext);
			kkMsgBuilder = kkLink.createKakaoTalkLinkMessageBuilder();
		} catch (KakaoParameterException e) {
			e.printStackTrace();
		}
	}

	public void sendInviteMsgKakaoLink(String text) {
		try {
			kkMsgBuilder.addText(text);
			kkMsgBuilder.addAppButton(mContext.getString(R.string.kakao_btn_move));
			kkLink.sendMessage(kkMsgBuilder.build(), mContext);
			android.util.Log.e(TAG +" / " + "sendInviteMsgKakaoLink", "text = " + text);
		} catch (KakaoParameterException e) {
			e.printStackTrace();
		}
	}

	public void shareHotelInfo(HotelDetail hotel, String region) {
		try {
			/**
			 * TODO : �����ϱ� �ÿ� ��Ȯ�ϰ� ������ �Ͽ����� ī��Ʈ�� �ϱ� ����.
			 *        īī����ũ���� �ݹ��� ������.
			 *        ������ư�� ������ ��츦 ī��Ʈ�ϸ� ��Ȯ���� ����.(�߰��� ���� ���Ҽ���) 
			 */
			String schemeParams = "hotelIdx="+hotel.getHotel().getIdx()+"&region="+region;
			android.util.Log.e("sche",schemeParams);
			
			kkMsgBuilder.addAppButton(mContext.getString(R.string.kakao_btn_move),
					new AppActionBuilder()
						.addActionInfo(AppActionInfoBuilder
								.createAndroidActionInfoBuilder()
								.setExecuteParam(schemeParams)
								.build())
						.addActionInfo(AppActionInfoBuilder
								.createiOSActionInfoBuilder()
								.setExecuteParam(schemeParams)
								.build())
						.build());
			kkMsgBuilder.addImage(hotel.getHotel().getImage(), 300, 200);
			kkMsgBuilder.addText(hotel.getHotel().getName());
			kkLink.sendMessage(kkMsgBuilder.build(), mContext);
			android.util.Log.e(TAG +" / " + "shareHotelInfo", "schmeParams = " + schemeParams);

		} catch (KakaoParameterException e) {
			e.printStackTrace();
		}
	}


}
