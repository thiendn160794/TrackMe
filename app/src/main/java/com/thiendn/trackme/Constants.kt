package com.thiendn.trackme

object Constants {

    const val FOREGROUND_SERVICE_CHANNEL_ID = "channel_track_location"
    const val FOREGROUND_SERVICE_NOTIFICATION_ID = 12345678

    const val BUNDLE_STARTED_FROM_NOTIFICATION = "BUNDLE_STARTED_FROM_NOTIFICATION"

    //location
    const val UPDATE_INTERVAL_IN_MILLISECONDS =  10000L
    const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    const val MIN_DISTANCE_TO_RECORDS_IN_METER = 5

    //broadcast receiver
    const val BUNDLE_LOCATION = "bundle_location"
    const val INTENT_FILTER_BROADCAST = "action_broadcast"

    //request code
    const val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    //mapView
    const val ZOOM_LEVEL = 17F
    const val ROUTE_WIDTH = 7F

    //share preferences
    const val PREFS_NAME = "prefs_name"
}