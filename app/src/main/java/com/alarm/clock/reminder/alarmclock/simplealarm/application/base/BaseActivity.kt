package com.alarm.clock.reminder.alarmclock.simplealarm.application.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.AnimBuilder
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.viewbinding.ViewBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.MyContextWrapper
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.hideNavBar


typealias MyActivity = BaseActivity<*>

abstract class BaseActivity<B : ViewBinding> : AppCompatActivity() {
    val activityScope: CoroutineLauncher by lazy {
        return@lazy CoroutineLauncher()
    }

    val navContainer: NavController? by lazy {
        (navHostId?.let {
            supportFragmentManager.findFragmentById(
                it
            )
        } as? NavHostFragment)?.navController
    }
    open val binding: B by lazy { makeBinding(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavBar()
        setContentView(binding.root)
        setupView(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(MyContextWrapper.wrap(newBase, Language.current.localizeCode))
    }


    abstract fun makeBinding(layoutInflater: LayoutInflater): B

    open fun setupView(savedInstanceState: Bundle?) {}


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideNavigationBar()
        }
    }

    val permissionsResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                onPermissionGranted(permissions)
            } else {
                onPermissionDenied(permissions)
            }
        }

    open fun onPermissionGranted(permissions: Map<String, Boolean>) {}

    open fun onPermissionDenied(permissions: Map<String, Boolean>) {}

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancelCoroutines()
    }

    override fun onResume() {
        super.onResume()
        MainApplication.ACTIVITY_CONTEXT = this
    }

    @IdRes
    open var rootDes: Int? = null

    @IdRes
    open var navHostId: Int? = null
}

abstract class BaseVMActivity<B : ViewBinding, VM : BaseViewModel> : BaseActivity<B>() {
    abstract val viewModel: VM

}

fun BaseActivity<*>.pushTo(
    @IdRes resId: Int,
    args: Bundle? = null,
    anim: PushType = PushType.SLIDE
) {
    navContainer?.currentDestination?.getAction(resId)?.navOptions?.let {
        navContainer?.navigate(
            resId,
            args,
            navOptions {
                anim {
                    enter = anim.anim.enter
                    exit = anim.anim.exit
                    popEnter = anim.anim.popEnter
                    popExit = anim.anim.popExit
                }
                popUpTo(it.popUpToId) {
                    inclusive = it.isPopUpToInclusive()
                }
            }
        )
    }
}

fun AppCompatActivity.hideNavigationBar() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)
    controllerCompat.hide(WindowInsetsCompat.Type.navigationBars())
    controllerCompat.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controllerCompat.isAppearanceLightStatusBars = true
}

fun BaseActivity<*>.popTo(@IdRes destinationId: Int? = null, inclusive: Boolean = false) {
    navContainer?.apply {
        if (destinationId == null) popBackStack()
        else popBackStack(destinationId, inclusive)
    }
}

fun BaseActivity<*>.popToRoot() {
    rootDes?.let { popTo(it, false) }
}

enum class PushType(val anim: AnimBuilder) {
    NONE(AnimBuilder().apply {}),
    SLIDE(
        AnimBuilder().apply {
            enter = R.anim.enter_from_right
            exit = R.anim.exit_to_left
            popEnter = R.anim.enter_from_left
            popExit = R.anim.exit_to_right
        }
    ),
    FADE(AnimBuilder().apply {
        enter = R.anim.fade_in
        exit = R.anim.fade_out
    })
}