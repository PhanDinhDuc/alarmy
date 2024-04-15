package com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.BedTimeData
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivitySettingSoundBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemBedtimePageBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.BaseViewHolder
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.Util.BEDTIME_DATA
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.adapter.TypeSoundAdapter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.SnoozeActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.data.SoundBedtime
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingSoundActivity : BaseVMActivity<ActivitySettingSoundBinding, BedTimeSettingViewModel>(),
    SnoozeActivity.CallBackSnoozeListener {
    override val viewModel: BedTimeSettingViewModel by viewModels()

    private var soundPath: Int? = null
    private var soundName: Int? = null
    private var bedTimeData: BedTimeData? = null
    private var duration: Long? = null
    private var soundLevel = 0

    private var mediaPlayer: MediaPlayer? = null
    private var selectedSound: Int? = null
    private var selectedPlaying: Int? = null


    override fun makeBinding(layoutInflater: LayoutInflater): ActivitySettingSoundBinding {
        return ActivitySettingSoundBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        SnoozeActivity.listener = this
        bedTimeData = intent.parcelable(BEDTIME_DATA)
        selectedSound = bedTimeData?.soundName?.toIntOrNull() ?: 0

        bedTimeData?.let {
            soundName = it.soundName?.toIntOrNull() ?: 0
            soundPath = it.sound?.let { it1 -> getResourceIdFromUri(it1) }
            duration = it.timeSound
            soundLevel = it.soundLevel
        }

        //add tab layout
        for (title in SoundType.listTxtType) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title.type))
        }

        binding.viewPagerSound.apply {
            offscreenPageLimit = SoundType.values().size
            adapter = object : RecyclerView.Adapter<BaseViewHolder<ItemBedtimePageBinding>>() {
                val listItem = SoundType.values().toList()

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): BaseViewHolder<ItemBedtimePageBinding> {
                    return BaseViewHolder(
                        ItemBedtimePageBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }

                override fun getItemCount(): Int = listItem.size

                override fun onBindViewHolder(
                    holder: BaseViewHolder<ItemBedtimePageBinding>,
                    position: Int
                ) {
                    holder.binding.recSound.apply {
                        val item = listItem.getOrNull(position)

                        layoutManager = GridLayoutManager(this@SettingSoundActivity, 2)
                        val typeSoundAdapter = TypeSoundAdapter(
                            if (item?.type == SoundType.ALL.type) SoundBedtime.listSound() else
                                SoundBedtime.listSound()
                                    .filter { it.type == item?.index },
                            this@SettingSoundActivity,
                            onClick = {
                                selectedPlaying = it.id
//                                soundPath = it.soundRaw
//                                soundName = it.id
                                mediaPlayer?.stop()
                                mediaPlayer?.release()

                                mediaPlayer =
                                    MediaPlayer.create(this@SettingSoundActivity, it.soundRaw)
                                mediaPlayer?.isLooping = true
                                mediaPlayer?.start()
                            }
                        ) {
                            selectedSound = it.id
                            soundPath = it.soundRaw
                            soundName = it.id
                        }

                        typeSoundAdapter.setCurrentSelected(selectedSound ?: 0)
                        adapter = typeSoundAdapter
                    }
                }

            }
        }


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                getSoundAdapterAt(tab?.position ?: 0)?.apply {
                    setCurrentPlaying(selectedPlaying ?: -1)
                    setCurrentSelected(selectedSound ?: 0)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        TabLayoutMediator(binding.tabLayout, binding.viewPagerSound) { tab, position ->
            tab.text = getString(SoundType.listTxtType[position].type)
        }.attach()

        binding.viewClickTimer.setOnSingleClickListener {
            val intent = Intent(this, SnoozeActivity::class.java)
            intent.putExtra("keySnoozeBedTime", duration)
            startActivity(intent)
            resetPlayerMedia()
        }

        binding.imageView20.setOnSingleClickListener {
            finish()
        }

        binding.txtDuration.text =
            getString(R.string.not_space_minute, (bedTimeData!!.timeSound) / 60000)

        binding.seekBar.progress = bedTimeData!!.soundLevel

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                soundLevel = p1

                binding.imageView23.setImageResource(if (p1 == 0) R.drawable.ic_off_sound else R.drawable.ic_sound)

                val volume = soundLevel / 100.0f

                if (mediaPlayer == null) {
                    SoundBedtime.listSound().firstOrNull { it.id == selectedPlaying }?.let {
                        mediaPlayer =
                            MediaPlayer.create(this@SettingSoundActivity, it.soundRaw)
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.start()

                        SoundType.values().forEachIndexed { index, soundType ->
                            getSoundAdapterAt(index)?.apply {
                                setCurrentPlaying(selectedPlaying ?: -1)
                                setCurrentSelected(selectedSound ?: -1)
                            }
                        }
                    }

                }

                mediaPlayer?.setVolume(volume, volume)

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.imageView23.setOnClickListener {
            if (soundLevel > 0) {
                binding.seekBar.progress = 0
            } else {
                binding.seekBar.progress = bedTimeData?.soundLevel ?: 50
            }
        }

        binding.imageView17.setOnClickListener {
            getData()?.let { it1 -> viewModel.updateBedtime(it1) }
            finish()
        }

    }

    fun getSoundAdapterAt(index: Int): TypeSoundAdapter? {
        return (((binding.viewPagerSound[0] as? RecyclerView)?.layoutManager?.findViewByPosition(
            index
        ) as? RecyclerView)?.adapter as? TypeSoundAdapter)
    }

    private fun getData(): BedTimeData? {
        return duration?.let {
            bedTimeData?.copy(
                sound = "android.resource://${packageName}/${soundPath}",
                soundName = soundName.toString(),
                timeSound = it,
                soundLevel = soundLevel
            )
        }
    }

    //convert sound path to int
    private fun getResourceIdFromUri(uri: String): Int? {
        val segments = uri.split("/")
        val resourceIdStr = segments.lastOrNull()

        return resourceIdStr?.toIntOrNull()
    }

    private fun resetPlayerMedia() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        SoundType.values().forEachIndexed { index, soundType ->
            getSoundAdapterAt(index)?.apply {
                setCurrentPlaying(-1)
                setCurrentSelected(selectedSound ?: 0)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onSnoozeListener(snoozeDuration: Long) {
        duration = snoozeDuration
        binding.txtDuration.text = getString(R.string.minute, snoozeDuration / 60000)
    }
}

enum class SoundType(val type: Int, val index: Int) {
    ALL(R.string.all, 0),
    NATURE(R.string.nature, 1),
    MEDITATION(R.string.meditation, 2),
    WHITE_MUSIC(R.string.white_music, 3),
    ASMR(R.string.ASMR, 4);

    companion object {
        val listTxtType: List<SoundType> = values().toList()

        fun get(position: Int) = SoundType.values().getOrNull(position)
    }
}