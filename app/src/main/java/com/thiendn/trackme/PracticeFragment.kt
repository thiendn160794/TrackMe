 package com.thiendn.trackme

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.thiendn.trackme.TrackLocationService.Companion.ACTION_BROADCAST
import com.thiendn.trackme.TrackLocationService.Companion.EXTRA_LOCATION
import kotlinx.android.synthetic.main.fragment_practice.*
import java.util.*


 class PracticeFragment: Fragment() {

    private var mService: TrackLocationService? = null
    private var isBound = false

     private var mMap: GoogleMap? = null
     private var mReceiver: BroadcastReceiver? = null

     private var mListLocations = LinkedList<Location>()

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         mReceiver = LocationReceiver()
     }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_practice, container, false)
    }

    private val mServiceConnection by lazy {
        object : ServiceConnection{
            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mService = (service as? TrackLocationService.TrackLocationBinder)?.getService()
                mService?.getLocationUpdate()
                isBound = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(activity)) {
            if (!checkPermissions()) {
                requestPermissions()
            }
        }
        mapView.onCreate(null)
        mapView.getMapAsync{
            mMap = it.apply {
                setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                "thiendn",
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                view!!,
                "R.string.permission_rationale",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("R.string.ok", View.OnClickListener { // Request permission
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
                .show()
        } else {
            Log.i("thiendn", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i("thiendn", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i("thiendn", "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    mService?.getLocationUpdate()
                }
                else -> {
                    // Permission denied.
        //                setButtonsState(false)
                    Snackbar.make(
                        view!!,
                        "R.string.permission_denied_explanation",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(
                            "R.string.settings",
                            View.OnClickListener { // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                    "package",
                                    BuildConfig.APPLICATION_ID, null
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            })
                        .show()
                }
            }
        }
    }

     override fun onResume() {
         super.onResume()
         mapView.onResume()
         context?.let {
             mReceiver?.let { it1 ->
                 LocalBroadcastManager.getInstance(it).registerReceiver(
                     it1,
                     IntentFilter(ACTION_BROADCAST)
                 )
             }
         }
     }

     override fun onStart() {
         super.onStart()
         activity?.bindService(Intent(activity, TrackLocationService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
         mapView.onStart()
     }

     override fun onStop() {
         activity?.unbindService(mServiceConnection)
         mapView.onStop()
         super.onStop()
     }

     override fun onPause() {
         mapView.onPause()
         context?.let {
             mReceiver?.let { it1 ->
                 LocalBroadcastManager.getInstance(it).unregisterReceiver(
                     it1
                 )
             }
         }
         super.onPause()
     }

     override fun onDestroy() {
         mapView.onDestroy()
         super.onDestroy()
     }

     override fun onLowMemory() {
         super.onLowMemory()
         mapView.onLowMemory()
     }

     /**
      * Receiver for broadcasts sent by [TrackLocationService].
      */
     inner class LocationReceiver : BroadcastReceiver() {
         override fun onReceive(
             context: Context,
             intent: Intent
         ) {
             Toast.makeText(context, "newLocation", Toast.LENGTH_SHORT).show()
             intent.getParcelableExtra<Location>(EXTRA_LOCATION)?.let {
                 mListLocations.push(it)
             }
             mListLocations.firstOrNull()?.let {
                 markMarker(it.latitude, it.longitude, R.drawable.ic_android_black_24dp, 30, 30, "")
             }
             mListLocations.lastOrNull()?.let {
                 markMarker(it.latitude, it.longitude, R.drawable.ic_android_black_24dp, 30, 30, "")
             }
             mMap?.animateCamera(autoZoomLevel())
             mMap?.addPolyline(configRoute().apply {
                 for (location in mListLocations){
                     this.add(LatLng(location.latitude, location.longitude))
                 }
             })
         }
     }

     private fun autoZoomLevel(): CameraUpdate? {
         val builder = LatLngBounds.Builder()
         for (location in mListLocations){
             builder.include(LatLng(location.latitude, location.longitude))
         }
         val bounds = builder.build()
         val padding = 200 // offset from edges of the map in pixels
         return CameraUpdateFactory.newLatLngZoom(bounds.center, 17f).apply {

         }
     }

     private fun markMarker(
         latitude: Double,
         longitude: Double,
         drawable: Int,
         width: Int,
         height: Int,
         displayAddress: String
     ): Marker? {
         val bitmap = drawableToBitmap(ContextCompat.getDrawable(context!!, R.drawable.ic_android_black_24dp)!!)
         val smallMarker = Bitmap.createScaledBitmap(bitmap!!, width, height, false)
         val latLng = LatLng(latitude, longitude)
         val markerOptions = MarkerOptions()
         markerOptions.position(latLng)
         markerOptions.title(displayAddress)
         markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
         return mMap?.addMarker(markerOptions)
     }


     fun drawableToBitmap(drawable: Drawable): Bitmap? {
         var bitmap: Bitmap? = null
         if (drawable is BitmapDrawable) {
             val bitmapDrawable = drawable as BitmapDrawable
             if (bitmapDrawable.bitmap != null) {
                 return bitmapDrawable.bitmap
             }
         }
         bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
             Bitmap.createBitmap(
                 1,
                 1,
                 Bitmap.Config.ARGB_8888
             ) // Single color bitmap will be created of 1x1 pixel
         } else {
             Bitmap.createBitmap(
                 drawable.getIntrinsicWidth(),
                 drawable.getIntrinsicHeight(),
                 Bitmap.Config.ARGB_8888
             )
         }
         val canvas = Canvas(bitmap)
         drawable.setBounds(0, 0, canvas.width, canvas.getHeight())
         drawable.draw(canvas)
         return bitmap
     }

     private fun configRoute() : PolylineOptions{
         val polylineOptions = PolylineOptions()
         polylineOptions.width(7f) //dp

         polylineOptions.geodesic(true)
         polylineOptions.color(ContextCompat.getColor(context!!, R.color.colorAccent))
         return polylineOptions
     }
 }