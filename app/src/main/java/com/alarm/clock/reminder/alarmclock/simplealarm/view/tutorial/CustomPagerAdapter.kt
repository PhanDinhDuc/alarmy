package com.alarm.clock.reminder.alarmclock.simplealarm.view.tutorial

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemTutorialBinding
import com.bumptech.glide.Glide

class CustomPagerAdapter(
    private val context: Context,
    private val textIntros1: List<Int>,
    private val textIntros2: List<Int>,
    private val imageIntros: List<Int>
) : PagerAdapter() {
    private lateinit var binding: ItemTutorialBinding
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        binding = ItemTutorialBinding.inflate(LayoutInflater.from(context), container, false)
        val view = binding.root
        binding.textTutorial.setText(textIntros1[position])
        binding.textTutorial2.setText(textIntros2[position])
        binding.imgTutorial.loadImage(imageIntros[position])
        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imageIntros.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}

fun ImageView.loadImage(source: Int) {
    this.post {
        Glide.with(this).asBitmap()
            .override(this.width, this.height)
            .load(source).into(this)
    }
}

