package com.alarm.clock.reminder.alarmclock.simplealarm.view.sound

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.MainApplication
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseVMActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AudioModel
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings.*
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings.Companion.alarmSettings
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ActivityAlarmSoundBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnBackClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.setOnImageRightClick
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.AudioUtils
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.hasPermissions
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.setOnSingleClickListener
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.adapter.AdapterSoundViewPager
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.fragment.FragmentListSound
import com.alarm.clock.reminder.alarmclock.simplealarm.view.viewmodels.AlarmViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmSoundActivity : BaseVMActivity<ActivityAlarmSoundBinding, AlarmViewModel>() {
    override val viewModel: AlarmViewModel by viewModels()
    override fun makeBinding(layoutInflater: LayoutInflater): ActivityAlarmSoundBinding {
        return ActivityAlarmSoundBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val readAudioPermission = Manifest.permission.READ_MEDIA_AUDIO
    private val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val titles = listOf(R.string.ringtone, R.string.txt_music)

    var listRingMusic: List<AudioModel> = mutableListOf()
    private var adapterMusic: AdapterSoundViewPager? = null

    private var tabs = 0
    private var isVibrate = false
    private var idAlarm: Int = 0
    private var soundVolume: Int = 0
    private var currentAudioModel: AudioModel? = null
    var selectedItemPosition: String? = null
    private var mAudioManager: AudioManager? = null

    @Inject
    lateinit var soundManager: SoundManager
    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        if (intent.getBooleanExtra("isReminder", false)) {
            binding.mHeader.title = R.string.alert_tone
        }
        idAlarm = intent.getIntExtra("ID_ALARM", 0)
        selectedItemPosition = intent.getStringExtra("sounds")

        isVibrate = intent.getBooleanExtra("isVibrate", IS_VIBRATE.get(false))
        val isSettings = intent.getBooleanExtra("setting_sound", false)
        initSeekBarVolume()
        setupTabLayoutAndViewpager()
        setupIconVibrate()
        binding.mHeader.setOnImageRightClick {
            if (isSettings) {
                IS_VIBRATE.put(isVibrate)
                alarmSettings = alarmSettings.copy(soundPath = selectedItemPosition.toString())
            }
            val resultIntent = Intent()
            resultIntent.putExtra(KEY_SOUND_PATH, selectedItemPosition)
            resultIntent.putExtra(KEY_IS_VIBRATE, isVibrate)
            resultIntent.putExtra(KEY_VOLUME, soundVolume)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        binding.mHeader.setOnBackClick { finish() }
    }

    override fun onResume() {
        super.onResume()
        returnArrayPermissions().forEach {
            if (this.hasPermissions(arrayOf(it))) {
                onPermissionGranted(mapOf(it to true))
            } else {
                onPermissionDenied(mapOf(it to false))
            }
        }
    }

    fun returnArrayPermissions(): Array<String> {
        val arr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(storagePermission, readAudioPermission)
        } else {
            arrayOf(storagePermission)
        }
        return arr
    }

    override fun onPermissionGranted(permissions: Map<String, Boolean>) {
        super.onPermissionGranted(permissions)
        if (permissions.isNotEmpty()) {
            listRingMusic = AudioUtils.getAllAudioFromDevice(this, true)
            adapterMusic?.updateDataForTab(1, listRingMusic)
        }
    }

    private fun setupMedia(audioModel: AudioModel) {
        currentAudioModel = if (currentAudioModel != audioModel) {
            soundManager.playSound(audioModel)
            if (isVibrate) {
                soundManager.activeVibrator()
            } else {
                soundManager.cancelVibrator()
            }
            audioModel
        } else {
            soundManager.apply {
                pauseSound()
                cancelVibrator()
            }
            null
        }
    }

    private fun setupTabLayoutAndViewpager() {
        adapterMusic = AdapterSoundViewPager(this)

        for (i in titles.indices) {
            val bundle = Bundle()
            bundle.putInt("id", i)
            bundle.putString("selectedSoundPath", selectedItemPosition)
            val blankFragment = FragmentListSound {
                setupMedia(it)
            }

            blankFragment.arguments = bundle
            adapterMusic?.addFragment(blankFragment, getString(titles[i]))
        }
        binding.mViewpager.adapter = adapterMusic
        binding.mViewpager.offscreenPageLimit = 2

        TabLayoutMediator(binding.mTabarLayout, binding.mViewpager) { tab, position ->
            tab.text = adapterMusic!!.getPageTitle(position)
        }.attach()
        for (i in 0 until binding.mTabarLayout.tabCount) {
            val tab = binding.mTabarLayout.getTabAt(i)
            tab?.let {
                val tabView =
                    LayoutInflater.from(this).inflate(R.layout.layout_text_tablayout, null)
                val tv =
                    tabView.findViewById<TextView>(R.id.tabText)

                tv.text = getString(titles[i])
                it.customView = tabView
            }
        }

        binding.mTabarLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    tabs = tab.position
                    if (tab.position == 1) {

                        returnArrayPermissions().forEach {
                            if (!this@AlarmSoundActivity.hasPermissions(arrayOf(it))) {
                                requestAudioPermission()
                            } else {
                                onPermissionGranted(mapOf(it to true))

                            }
                        }
                    } else {
                        adapterMusic?.updateDataForTab(tab.position, listRingMusic)
                    }
                }


            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                soundManager.onDestroy()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!shouldShowRequestPermissionRationale(readAudioPermission)) {
                permissionsResult.launch(arrayOf(readAudioPermission, storagePermission))
            } else {
                goToSettingApp()
            }
        } else {
            permissionsResult.launch(arrayOf(storagePermission))
        }
    }

    private fun goToSettingApp() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        this.startActivity(intent)
    }

    private fun initSeekBarVolume() {
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        soundVolume = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekBar.max = soundVolume
        VOLUME.put(mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC))

        val currVolume = VOLUME.get(100)
        changeImageSound(mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        binding.seekBar.progress = currVolume
        setupIconVolume(currVolume)
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                VOLUME.put(i)
                setupIconVolume(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    private fun setupIconVibrate() {
        binding.imgVibrate.isSelected = isVibrate
        binding.imgVibrate.setOnClickListener {
            isVibrate = !isVibrate
            it.isSelected = isVibrate
            getVibrate(isVibrate)
        }
    }

    private fun getVibrate(isVibrate: Boolean) {
        if (isVibrate && soundManager.isPlaying()) {
            soundManager.activeVibrator()
        } else {
            soundManager.cancelVibrator()
        }
    }

    private fun changeImageSound(volume: Int) {
        binding.imgSound.setOnSingleClickListener {
            if (binding.seekBar.progress != 0) {
                setupIconVolume(0)
                binding.seekBar.progress = 0
            } else {
                setupIconVolume(volume)
                binding.seekBar.progress = volume
            }
        }
    }

    private fun setupIconVolume(volume: Int) {
        if (volume == 0) {
            soundManager.pauseSound()
        } else {
            soundManager.startSound()
        }
        mAudioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        binding.imgSound.isSelected = volume != 0
        soundVolume = volume
    }

    override fun onStop() {
        super.onStop()
        soundManager.apply {
            onDestroy()
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager.apply {
            onDestroy()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.apply {
            onDestroy()
        }
    }

    companion object {
        const val KEY_SOUND_PATH = "KEY_SOUND_PATH"
        const val KEY_IS_VIBRATE = "KEY_IS_VIBRATE"
        const val KEY_VOLUME = "KEY_VOLUME"
    }
}


fun Int.convertRawToPath(context: Context): String {
    val resourceName = context.resources.getResourceName(this)
    val resourceNameParts = resourceName.split("/")

    if (resourceNameParts.size == 2) {
        val rawFileName = resourceNameParts[1]
        return "android.resource://${context.packageName}/raw/${rawFileName}"
    }
    return "android.resource://${context.packageName}/raw/${"air_horn"}"
}

fun String.getNameSound(context: Context): String {

    val resourceNameParts = this.split("/")
    if (resourceNameParts.size > 1) {
        if (resourceNameParts[resourceNameParts.size - 1] == "air_horn") {
            return context.getString(R.string.txtDefault)
        }
        return resourceNameParts[resourceNameParts.size - 1]
    }
    return context.getString(R.string.txtDefault)
}

fun AlarmDefault(context: Context = MainApplication.CONTEXT): String {
    val raw = R.raw.air_horn
    return raw.convertRawToPath(context)
}