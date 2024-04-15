package com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.application.base.BaseFragment
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AudioModel
import com.alarm.clock.reminder.alarmclock.simplealarm.databinding.ItemSoundViewpagerBinding
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.gone
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.utils.visible
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmDefault
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.AlarmSoundActivity
import com.alarm.clock.reminder.alarmclock.simplealarm.view.sound.adapter.AdapterMusic

class FragmentListSound(
    private val onSelected: (music: AudioModel) -> Unit
) :
    BaseFragment<ItemSoundViewpagerBinding>(), OnListAudioListener {
    override fun makeBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): ItemSoundViewpagerBinding {
        return ItemSoundViewpagerBinding.inflate(inflater)
    }

    private var listener: OnListAudioListener? = null
    var adapter: AdapterMusic? = null
    override fun setupView() {
        super.setupView()
        val id = arguments?.getInt("id")
        val soundPath = arguments?.getString("selectedSoundPath")
        listener = this
        adapter = AdapterMusic(activity as? AlarmSoundActivity) { it, _ ->
            if (it.aPath == null) {
                it.realPath?.let { it1 -> adapter?.setSelected(it1) }
            } else {
                it.aPath?.let { it1 -> adapter?.setSelected(it1) }
            }
            onSelected.invoke(it)
        }
        adapter?.setSelected(soundPath ?: AlarmDefault(requireContext()))

        binding.rc.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rc.adapter = adapter
        if (id == 0) {
            adapter?.setupData(returnListRingTone())
            adapter?.checkRingtone(true)
        }
    }

    fun updateListData(tabPosition: Int, newList: List<AudioModel>) {
        if (tabPosition == 1) {
            if (newList.isNotEmpty()) {
                adapter?.checkRingtone(false)
                adapter?.setupData(newList)
            } else {
                listener?.listAudioListener(newList)
            }
        } else {
            adapter?.setupData(returnListRingTone())
            adapter?.checkRingtone(true)
        }

    }

    fun returnListRingTone(): List<AudioModel> {
        val list = mutableListOf<AudioModel>()
        list.add(AudioModel(raw = R.raw.air_horn, aName = "air horn"))
        list.add(AudioModel(raw = R.raw.alarm, aName = "alarm"))
        list.add(AudioModel(raw = R.raw.peaceful, aName = "peaceful"))
        list.add(AudioModel(raw = R.raw.happy_sound, aName = "happy"))
        list.add(AudioModel(raw = R.raw.loud_sound, aName = "loud"))
        list.add(AudioModel(raw = R.raw.cat, aName = "cat"))
        list.add(AudioModel(raw = R.raw.cavalry, aName = "cavalry"))
        list.add(AudioModel(raw = R.raw.chicken, aName = "chicken"))
        list.add(AudioModel(raw = R.raw.dog, aName = "dog"))
        list.add(AudioModel(raw = R.raw.laugh, aName = "laugh"))
        list.add(AudioModel(raw = R.raw.doorbell, aName = "doorbell"))
        list.add(AudioModel(raw = R.raw.melody, aName = "melody"))
        list.add(AudioModel(raw = R.raw.party_horn, aName = "party horn"))
        list.add(AudioModel(raw = R.raw.police, aName = "police"))
        list.forEach {
            it.handlePath(this.requireContext())
        }
        return list
    }

    override fun listAudioListener(newList: List<AudioModel>) {
        if (newList.isEmpty()) {
            binding.rc.gone()
            binding.tvEmpty.visible()
        } else {
            binding.rc.visible()
            binding.tvEmpty.gone()
        }
    }
}

interface OnListAudioListener {
    fun listAudioListener(newList: List<AudioModel>)
}

