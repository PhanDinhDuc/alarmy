@file:Suppress("DEPRECATION")

package com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.alarm.clock.reminder.alarmclock.simplealarm.BuildConfig
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.MyActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object Constant {
    val screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels

    val screenHeight: Int = Resources.getSystem().displayMetrics.heightPixels
}

fun Context.getAppCompactColor(int: Int): Int {
    return ContextCompat.getColor(this, int)
}

fun Context.getAppCompactDrawable(int: Int): Drawable? {
    return ContextCompat.getDrawable(this, int)
}

object Converter {
    fun dpFromPx(px: Int): Float {
        return px / Resources.getSystem().displayMetrics.density
    }

    fun pixelsToSp(pixels: Int): Float {
        val scaledDensity = Resources.getSystem().displayMetrics.scaledDensity
        return pixels / scaledDensity
    }

    fun asPixels(value: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        val dpAsPixels = (value * scale + 0.5f)
        return dpAsPixels.toInt()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDateTime.string(pattern: String = "yyyy-MM-dd HH:mm"): String {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
    return this.format(formatter)
}

fun MyActivity.delay(timeMillis: Long, completion: () -> Unit) {
    activityScope.launch {
        kotlinx.coroutines.delay(timeMillis)
        completion.invoke()
    }
}

fun View.visible() {
    this.visibility = View.VISIBLE
    this.isEnabled = true
}

fun View.hidden() {
    this.visibility = View.INVISIBLE
    this.isEnabled = false
}

fun View.gone() {
    this.visibility = View.GONE
    this.isEnabled = false
}

fun View.animeFade(isShow: Boolean, duration: Long = 0) {
    if (isShow == isVisible) {
        return
    }
    val toAlpha = if (isShow) 1f else 0f
    this.visible()
    this.alpha = if (isShow) 0f else 1f
    animate()
        .alpha(toAlpha)
        .setDuration(duration)
        .setComplete { if (isShow) visible() else gone() }
        .start()
}

fun Context.showSingleActionAlert(
    title: String, message: String,
    actionTitle: String = "OK",
    completion: () -> Unit
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(actionTitle) { _, _ ->
            completion()
        }
        .setCancelable(false)
        .create()
        .apply {
            setCanceledOnTouchOutside(false)
            show()
        }
}

fun Context.goToStore() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    }
}

fun Context.showTwoActionAlert(
    title: String, message: String,
    positiveTitle: String = "OK",
    negativeTitle: String = "Cancel",
    positiveAction: (() -> Unit)? = null,
    negativeAction: (() -> Unit)? = null
) {
    CoroutineScope(Dispatchers.Main).launch {
        AlertDialog.Builder(this@showTwoActionAlert)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveTitle) { _, _ ->
                positiveAction?.let { it() }
            }
            .setNegativeButton(negativeTitle) { _, _ ->
                negativeAction?.let { it() }
            }
            .setCancelable(false)
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }
}

suspend fun Context.showTwoActionAlert(
    title: String, message: String,
    positiveTitle: String = "OK",
    negativeTitle: String = "Cancel"
) = suspendCoroutine<Boolean> { continuation ->
    showTwoActionAlert(title, message, positiveTitle, negativeTitle, positiveAction = {
        continuation.resume(true)
    })
}

fun Context.hasPermissions(permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

fun ViewPropertyAnimator.setComplete(completion: (Animator?) -> Unit): ViewPropertyAnimator {
    return setListener(object : Animator.AnimatorListener {

        override fun onAnimationStart(p0: Animator) {

        }

        override fun onAnimationEnd(p0: Animator) {
            completion(p0)
        }

        override fun onAnimationCancel(p0: Animator) {

        }

        override fun onAnimationRepeat(p0: Animator) {

        }
    })
}


@SuppressLint("ClickableViewAccessibility")
fun ImageView.setOnHoldInView(isHold: (Boolean) -> Unit) {
    setOnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                isHold.invoke(
                    !isTransparentPixel(
                        getBitmapFromView(view),
                        motionEvent.x.toInt(),
                        motionEvent.y.toInt()
                    )
                )
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> {
                isHold.invoke(false)
            }

            else -> {
                isHold.invoke(
                    !isTransparentPixel(
                        getBitmapFromView(view),
                        motionEvent.x.toInt(),
                        motionEvent.y.toInt()
                    )
                )
            }
        }

        return@setOnTouchListener true
    }
}

fun getBitmapFromView(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(
        view.width, view.height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun isTransparentPixel(bitmap: Bitmap, x: Int, y: Int): Boolean {
    if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) {
        return true
    }
    val pixel = bitmap.getPixel(x, y)
    return Color.alpha(pixel) == 0
}

class NonRepeatingLiveData<T>(init: T) : MutableLiveData<T>() {

    init {
        postValue(init)
    }

    override fun setValue(value: T) {
        if (value != getValue()) {
            super.setValue(value)
        }
    }

    override fun postValue(value: T) {
        if (value != getValue()) {
            super.postValue(value)
        }
    }
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

fun Toast.showCustomToast(message: String, activity: Activity) {
    val layout = activity.layoutInflater.inflate(
        R.layout.custom_toast,
        activity.findViewById(R.id.cl_customToastContainer)
    )

    val textView = layout.findViewById<TextView>(R.id.tv_toast)
    textView.text = message

    this.apply {
        setGravity(Gravity.BOTTOM, 0, 100)
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}


