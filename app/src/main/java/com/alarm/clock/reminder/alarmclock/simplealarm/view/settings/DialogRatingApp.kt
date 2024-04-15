package com.alarm.clock.reminder.alarmclock.simplealarm.view.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentManager
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseDialogFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.DialogRatingBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogRatingApp : BaseDialogFragment<DialogRatingBinding>() {

    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): DialogRatingBinding {
        return DialogRatingBinding.inflate(inflater, container, false)
    }

    var indexStar = 4
    var isSelected = false
    private val onRateClickListener: OnRateClickListener
        get() = (parentFragment as? OnRateClickListener)
            ?: (activity as? OnRateClickListener)
            ?: error("No callback for ConfirmDialog")

    //review in app
    private lateinit var reviewManager: ReviewManager
    private var reviewInfo: ReviewInfo? = null

    override fun setupView() {
        super.setupView()

        initReviewInApp()

        val listStars = arrayOf(
            binding.imgStar1,
            binding.imgStar2,
            binding.imgStar3,
            binding.imgStar4,
            binding.img5Star,
        )

        listStars.forEachIndexed { index, view ->
            view.setOnSingleClickListener {
                if (indexStar == index && isSelected) {
                    return@setOnSingleClickListener
                }
                indexStar = index
                isSelected = true

//                binding.txtMaybe.visibility = View.GONE
                binding.txtOhno.visibility = View.VISIBLE
//                binding.txtRateUs.text =
//                    if (index == 4) resources.getText(R.string.rate_on_gg) else resources.getText(R.string.rate_us)
                checkStarRate(index)

                val isSelected = BooleanArray(listStars.size)
                if (!isSelected[index]) {
                    if (view.scaleX == 1f && view.scaleY == 1f) {
                        view.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.zoom_out
                            )
                        )
                    } else {
                        view.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.zoom_in
                            )
                        )
                    }
                }
            }
        }

        binding.txtMaybe.setOnSingleClickListener {
            dismiss()
        }

        binding.txtRateUs.setOnSingleClickListener {
            rateUS(indexStar + 1)
            onRateClickListener.onRateClick()
        }
    }


    private fun initReviewInApp() {
        reviewManager = ReviewManagerFactory.create(requireContext())
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            }
        }
    }

    private fun rateUS(i: Int) {
        if (i == 5) {
            launchReviewFlow()
            Settings.RATE.put(i)
            dismiss()
        } else {
            Settings.RATE.put(i)
            dismiss()
        }
    }

    private fun launchReviewFlow() {
        if (reviewInfo != null) {
            val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo!!)
            flow.addOnCompleteListener { _ ->
                dismiss()
            }
        } else {
            // Review info is not yet available
        }
    }

    private fun checkStarRate(index: Int) {
        val starImages = listOf(
            R.drawable.star_fill,
            R.drawable.star_none
        )

        val statusIcons = listOf(
            R.drawable.status_icon_1,
            R.drawable.status_icon_2,
            R.drawable.status_icon_3,
            R.drawable.status_icon_4,
            R.drawable.status_icon_5
        )

        val textResources = listOf(
            R.string.oh_no,
            R.string.oh_no,
            R.string.oh_no,
            R.string.we_like_you_too,
            R.string.we_like_you_too
        )

        val desResources = listOf(
            R.string.txt_des_rate_1,
            R.string.txt_des_rate_2,
            R.string.txt_des_rate_3,
            R.string.txt_des_rate_4,
            R.string.txt_des_rate_5
        )

        val starImageViews = listOf(
            binding.imgStar1,
            binding.imgStar2,
            binding.imgStar3,
            binding.imgStar4,
            binding.img5Star
        )

        for (i in 0..4) {
            starImageViews[i].setImageResource(starImages[if (i <= index) 0 else 1])
        }

        binding.imgStatus.setImageResource(statusIcons[index])
        binding.txtOhno.text = requireActivity().resources.getText(textResources[index])
        binding.txtTheBest.text = requireActivity().resources.getText(desResources[index])
    }


    companion object {
        fun show(fragmentManager: FragmentManager) {
            val dialog = DialogRatingApp()
            dialog.show(fragmentManager, "Dialog Rating")
        }
    }
}

interface OnRateClickListener {
    fun onRateClick()
}



