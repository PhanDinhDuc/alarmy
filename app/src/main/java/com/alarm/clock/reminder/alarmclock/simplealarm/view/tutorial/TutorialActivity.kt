package com.alarm.clock.reminder.alarmclock.simplealarm.view.tutorial

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.viewpager.widget.ViewPager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityTutorialBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.permission.PermissionActivity

class TutorialActivity : BaseActivity<ActivityTutorialBinding>() {
    override var navHostId: Int? = R.id.nav_host_fragment
    override fun makeBinding(layoutInflater: LayoutInflater): ActivityTutorialBinding {
        return ActivityTutorialBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        hideSystemUI()
        setupView()
    }

    private fun hideSystemUI() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInset ->
            val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = inset.left
                bottomMargin = inset.bottom
                rightMargin = inset.right
                topMargin = 0
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    fun completeTutorial() {
        val intent = Intent(this, PermissionActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun setupView() {
        binding.txtStarted.visibility = View.INVISIBLE
        val textIntros = listOf(R.string.text_intro1, R.string.text_intro2, R.string.text_intro3)
        val textIntros2 =
            listOf(R.string.text1_intro1, R.string.text2_intro1, R.string.text3_intro1)
        val imgIntros = listOf(R.drawable.intro1, R.drawable.intro2, R.drawable.intro3)
        val tutorialAdapter =
            CustomPagerAdapter(this, textIntros, textIntros2, imgIntros)
        binding.viewPagerTutorial.adapter = tutorialAdapter
        binding.viewPagerTutorial.offscreenPageLimit = textIntros2.size
        binding.dotsIndicator.attachTo(binding.viewPagerTutorial)
        binding.textViewNext.setOnSingleClickListener {
            binding.viewPagerTutorial.setCurrentItem(
                binding.viewPagerTutorial.currentItem + 1,
                true
            )
        }
        binding.txtGetStarted.setOnSingleClickListener {
            completeTutorial()
        }
        binding.txtStarted.setOnSingleClickListener {
            completeTutorial()
        }
        binding.viewPagerTutorial.apply {
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int, positionOffset: Float, positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    if (position < imgIntros.size - 1) {
                        binding.txtStarted.visibility = View.INVISIBLE
                        binding.textViewNext.visibility = View.VISIBLE

                    } else {
                        binding.txtStarted.visibility = View.VISIBLE
                        binding.textViewNext.visibility = View.INVISIBLE

                    }
                    binding.txtStarted.isClickable = position >= imgIntros.size - 1
                    binding.textViewNext.isClickable = position < imgIntros.size - 1

                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
        }
    }


    companion object
}