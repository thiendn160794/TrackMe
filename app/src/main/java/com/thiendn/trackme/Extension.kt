package com.thiendn.trackme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

fun GoogleMap.makeMakers(latLng: LatLng, drawable: Drawable, title: String) : Marker?{
    val width = 30
    val height = 30
    return addMarker(MarkerOptions().apply {
        position(latLng)
        title(title)
        icon(
            BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(drawable.toBitMap(), width, height, false)
            )
        )
    })
}

fun Drawable.toBitMap(): Bitmap{
    if (this is BitmapDrawable) {
        return bitmap
    }
    val bitmap: Bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
        Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        )
    } else {
        Bitmap.createBitmap(
            intrinsicWidth,
            intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun configRoute(width: Float, color: Int): PolylineOptions{
    val polylineOptions = PolylineOptions()
    polylineOptions.width(width) //dp

    polylineOptions.geodesic(true)
    polylineOptions.color(color)
    return polylineOptions
}

fun GoogleMap.autoZoom(level: Float, vararg latLngs: LatLng): CameraUpdate{
    val builder = LatLngBounds.Builder()
    for (latLng in latLngs){
        builder.include(latLng)
    }
    val bounds = builder.build()
    return CameraUpdateFactory.newLatLngZoom(bounds.center, level).also {
        animateCamera(it)
    }
}