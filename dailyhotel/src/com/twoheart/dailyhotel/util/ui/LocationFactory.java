package com.twoheart.dailyhotel.util.ui;

import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.Toast;

import com.twoheart.dailyhotel.R;
import com.twoheart.dailyhotel.widget.DailyToast;
import com.twoheart.dailyhotel.widget.FontManager;

public class LocationFactory
{
	protected static String SINGLE_LOCATION_UPDATE_ACTION = "com.twoheart.dailyhotel.places.SINGLE_LOCATION_UPDATE_ACTION";

	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int TEN_MINUTES = 1000 * 60 * 10;

	private static LocationFactory mInstance;

	private LocationManager mLocationManager = null;
	private Location mLocation = null;
	private boolean mIsMeasuringLocation = false;
	private LocationListener mLocationListener;
	private Context mContext;
	private ImageView mMyLocationView;
	private Drawable mMyLocationDrawable;

	protected PendingIntent mUpdatePendingIntent;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case 0:
					stopLocationMeasure();

					if (mContext != null)
					{
						DailyToast.showToast(mContext, R.string.message_failed_mylocation, Toast.LENGTH_SHORT);
					}
					break;

				case 1:
				{
					if (mMyLocationView != null)
					{
						mMyLocationView.setBackgroundColor(mContext.getResources().getColor(R.color.dh_theme_color));
					}

					sendEmptyMessageDelayed(2, 1000);
					break;
				}

				case 2:
				{
					if (mMyLocationView != null)
					{
						mMyLocationView.setBackgroundDrawable(mMyLocationDrawable);
					}

					sendEmptyMessageDelayed(1, 1000);
					break;
				}

				case 3:
				{
					if (mMyLocationView != null)
					{
						mMyLocationView.setBackgroundDrawable(mMyLocationDrawable);
					}
					break;
				}
			}
		};
	};

	public static LocationFactory getInstance()
	{
		if (mInstance == null)
		{
			synchronized (FontManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new LocationFactory();
				}
			}
		}
		return mInstance;
	}

	private LocationFactory()
	{

	}

	public void startLocationMeasure(Context context, final Fragment fragment, ImageView myLocation, LocationListener listener)
	{
		if (mIsMeasuringLocation)
		{
			return;
		}

		if (mLocationManager == null)
		{
			mLocationManager = (LocationManager) fragment.getActivity().getSystemService(Context.LOCATION_SERVICE);
		}

		if (mUpdatePendingIntent == null)
		{
			Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
			mUpdatePendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		mContext = context;
		mLocationListener = listener;
		mMyLocationView = myLocation;

		if (mMyLocationView != null)
		{
			mMyLocationDrawable = mMyLocationView.getBackground();
		}

		mLocation = null;

		boolean isGpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (isGpsProviderEnabled == false && isNetworkProviderEnabled == false)
		{
			mLocationListener.onProviderDisabled(null);
			return;
		}

		mIsMeasuringLocation = true;

		Location location = getLastBestLocation(context, 10, TEN_MINUTES);

		if (location != null)
		{
			mLocationListener.onLocationChanged(location);
		}

		mHandler.sendEmptyMessageDelayed(1, 1000);

		IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
		context.registerReceiver(mSingleUpdateReceiver, locIntentFilter);
		mLocationManager.requestSingleUpdate(new Criteria(), mUpdatePendingIntent);

		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 20 * 1000);
	}

	protected BroadcastReceiver mSingleUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			context.unregisterReceiver(mSingleUpdateReceiver);

			String key = LocationManager.KEY_LOCATION_CHANGED;
			Location location = (Location) intent.getExtras().get(key);

			mLocation = location;

			if (mLocationListener != null && location != null)
			{
				mLocationListener.onLocationChanged(location);
			}

			stopLocationMeasure();
		}
	};

	public Location getLastBestLocation(Context context, int minDistance, long minTime)
	{
		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;

		// Iterate through all the providers on the system, keeping
		// note of the most accurate result within the acceptable time limit.
		// If no result is found within maxTime, return the newest Location.
		List<String> matchingProviders = mLocationManager.getAllProviders();
		for (String provider : matchingProviders)
		{
			Location location = mLocationManager.getLastKnownLocation(provider);
			if (location != null)
			{
				float accuracy = location.getAccuracy();
				long time = location.getTime();

				if ((time > minTime && accuracy < bestAccuracy))
				{
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
				} else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime)
				{
					bestResult = location;
					bestTime = time;
				}
			}
		}

		return bestResult;
	}

	public void stopLocationMeasure()
	{
		mHandler.removeMessages(0);
		mHandler.removeMessages(1);
		mHandler.removeMessages(2);

		mHandler.sendEmptyMessage(3);

		mIsMeasuringLocation = false;

		if (mLocationManager != null)// && mOnLocationListener != null)
		{
			//			mLocationManager.removeUpdates(mOnLocationListener);
			mLocationManager.removeUpdates(mUpdatePendingIntent);
		}

		if (mContext != null)
		{
			try
			{
				mContext.unregisterReceiver(mSingleUpdateReceiver);
			} catch (Exception e)
			{
			}
		}
	}

	private boolean isBetterLocation(Location location, Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			return true;
		}

		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		if (isSignificantlyNewer)
		{
			return true;
		} else if (isSignificantlyOlder)
		{
			return false;
		}

		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		if (isMoreAccurate)
		{
			return true;
		} else if (isNewer && !isLessAccurate)
		{
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
		{
			return true;
		}

		return false;
	}

	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Listener
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//	private LocationListener mOnLocationListener = new LocationListener()
	//	{
	//		@Override
	//		public void onStatusChanged(String provider, int status, Bundle extras)
	//		{
	//			if (mLocationListener != null)
	//			{
	//				mLocationListener.onStatusChanged(provider, status, extras);
	//			}
	//		}
	//
	//		@Override
	//		public void onProviderEnabled(String provider)
	//		{
	//			if (mLocationListener != null)
	//			{
	//				mLocationListener.onProviderEnabled(provider);
	//			}
	//		}
	//
	//		@Override
	//		public void onProviderDisabled(String provider)
	//		{
	//			mIsMeasuringLocation = false;
	//
	//			if (mLocationListener != null)
	//			{
	//				mLocationListener.onProviderDisabled(provider);
	//			}
	//		}
	//
	//		@Override
	//		public void onLocationChanged(Location location)
	//		{
	//			if (isBetterLocation(location, mLocation))
	//			{
	//				mLocation = location;
	//			}
	//
	//			mIsMeasuringLocation = false;
	//
	//			if (mLocationListener != null)
	//			{
	//				mLocationListener.onLocationChanged(mLocation);
	//			}
	//		}
	//	};
}
