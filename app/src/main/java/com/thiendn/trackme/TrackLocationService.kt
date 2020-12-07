package com.thiendn.trackme

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.thiendn.trackme.Constants.BUNDLE_LOCATION
import com.thiendn.trackme.Constants.BUNDLE_STARTED_FROM_NOTIFICATION
import com.thiendn.trackme.Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
import com.thiendn.trackme.Constants.FOREGROUND_SERVICE_CHANNEL_ID
import com.thiendn.trackme.Constants.INTENT_FILTER_BROADCAST
import com.thiendn.trackme.Constants.UPDATE_INTERVAL_IN_MILLISECONDS

class TrackLocationService: Service() {

    private val TAG = TrackLocationService::class.java.simpleName

    private val mLocationRequest by lazy {
        LocationRequest().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private val mLocationCallback by lazy {
        object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                p0?.lastLocation?.apply {
                    onNewLocation(this)
                }
            }
        }
    }

    private fun onNewLocation(location: Location) {
        mLocation = location

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            Intent(INTENT_FILTER_BROADCAST)
                .putExtra(BUNDLE_LOCATION, location)
        )
    }


    private val mFusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var mLocation: Location? = null
    private var mNotificationManager: NotificationManager? = null
    private var mServiceHandler: Handler? = null
    private val mBinder: IBinder = TrackLocationBinder()

    override fun onBind(intent: Intent?): IBinder? {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.

        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        println("thiendn: serviceOnBind")
        stopForeground(true)
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        startForeground(Constants.FOREGROUND_SERVICE_NOTIFICATION_ID, getNotification())
        println("thiendn: serviceOnUnBind")
        return true
    }

    override fun onRebind(intent: Intent?) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        stopForeground(true)
        println("thiendn: serviceOnReBind")
        super.onRebind(intent)
    }

    override fun onCreate() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(
                FOREGROUND_SERVICE_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager?.createNotificationChannel(mChannel)
        }
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
    }

    override fun onDestroy() {
        mServiceHandler?.removeCallbacksAndMessages(null)
    }

    fun getLocationUpdate(){
        Log.i(TAG, "Requesting location updates")
        Utils.setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, TrackLocationService::class.java))
        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Utils.setRequestingLocationUpdates(this, false)
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    private fun getNotification(): Notification? {
        val intent = Intent(this, TrackLocationService::class.java)

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(BUNDLE_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), 0
        )
        val builder = NotificationCompat.Builder(this)
            .addAction(R.mipmap.ic_launcher, getString(R.string.app_name), activityPendingIntent)
            .addAction(R.mipmap.ic_launcher, getString(R.string.app_name), servicePendingIntent)
            .setContentText("text")
            .setContentTitle("ccc")
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("text")
            .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(FOREGROUND_SERVICE_CHANNEL_ID) // Channel ID
        }
        return builder.build()
    }

    inner class TrackLocationBinder : Binder(){
        fun getService(): TrackLocationService{
            return this@TrackLocationService
        }
    }
}