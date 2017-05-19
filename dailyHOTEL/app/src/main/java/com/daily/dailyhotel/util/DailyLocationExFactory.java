package com.daily.dailyhotel.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.daily.base.util.ExLog;
import com.daily.base.util.VersionUtils;

import java.util.List;

public class DailyLocationExFactory
{
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int TEN_MINUTES = 1000 * 60 * 10;
    protected static final String SINGLE_LOCATION_UPDATE_ACTION = "com.daily.dailyhotel.SINGLE_LOCATION_UPDATE_ACTION";

    protected PendingIntent mUpdatePendingIntent;
    private LocationManager mLocationManager = null;
    private boolean mIsMeasuringLocation = false;

    LocationListenerEx mLocationListener;
    Context mContext;

    private int mProviderCount;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    stopLocationMeasure();

                    if (mLocationListener != null)
                    {
                        mLocationListener.onFailed();
                    }
                    break;
            }
        }
    };

    protected BroadcastReceiver mSingleUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);

            if (mLocationListener != null)
            {
                if (location != null)
                {
                    stopLocationMeasure();
                    mLocationListener.onLocationChanged(location);
                } else
                {
                    if (++mProviderCount > 1)
                    {
                        stopLocationMeasure();
                    } else
                    {
                        return;
                    }

                    mLocationListener.onFailed();
                }
            } else
            {
                stopLocationMeasure();
            }
        }
    };

    public interface LocationListenerEx extends LocationListener
    {
        void onRequirePermission();

        void onFailed();
    }

    public DailyLocationExFactory()
    {
    }

    public void startLocationMeasure(Context context, LocationListenerEx listener)
    {
        if (context == null)
        {
            return;
        }

        mContext = context;
        mProviderCount = 0;

        if (VersionUtils.isOverAPI23() == true)
        {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if (listener != null)
                {
                    listener.onRequirePermission();
                }

                return;
            }
        }

        if (mIsMeasuringLocation)
        {
            return;
        }

        if (mLocationManager == null)
        {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        if (mUpdatePendingIntent == null)
        {
            Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
            mUpdatePendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        mLocationListener = listener;

        boolean isGpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsProviderEnabled == false && isNetworkProviderEnabled == false)
        {
            if (mLocationListener != null)
            {
                mLocationListener.onProviderDisabled(null);
            }
            return;
        }

        mIsMeasuringLocation = true;

        Location location = getLastBestLocation(context, 1000, System.currentTimeMillis() + TEN_MINUTES);

        if (location != null && mLocationListener != null)
        {
            mLocationListener.onLocationChanged(location);
            stopLocationMeasure();
            return;
        }

        try
        {
            IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
            context.registerReceiver(mSingleUpdateReceiver, locIntentFilter);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mUpdatePendingIntent);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mUpdatePendingIntent);

            mHandler.removeMessages(0);
            mHandler.sendEmptyMessageDelayed(0, 30 * 1000);
        } catch (Exception e)
        {
            ExLog.d(e.toString());

            mHandler.removeMessages(0);

            stopLocationMeasure();
        }
    }

    private Location getLastBestLocation(Context context, int minDistance, long minTime)
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

        // If the best result is beyond the allowed time limit, or the accuracy of the
        // best result is wider than the acceptable maximum distance, request a single update.
        // This check simply implements the same conditions we set when requesting regular
        // location updates every [minTime] and [minDistance].
        if (mLocationManager != null && (bestTime < minTime || bestAccuracy < minDistance))
        {
            return bestResult;
        } else
        {
            return null;
        }
    }

    public void stopLocationMeasure()
    {
        mProviderCount = 0;

        mHandler.removeMessages(0);

        if (mLocationManager != null)
        {
            mLocationManager.removeUpdates(mUpdatePendingIntent);
        }

        try
        {
            mContext.unregisterReceiver(mSingleUpdateReceiver);
        } catch (Exception e)
        {
            ExLog.d(e.toString());
        }

        mIsMeasuringLocation = false;
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
}
