package com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.data

import android.os.Parcelable
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.view.bedtime.SoundType
import kotlinx.parcelize.Parcelize

@Parcelize
data class SoundBedtime(
    val name: Int,
    val thumb: Int,
    val soundRaw: Int,
    var type: Int,
    var id: Int = 0
) :
    Parcelable {
    companion object {

        private var listSound: List<SoundBedtime>? = null
        fun listSound(): List<SoundBedtime> {
            if (listSound == null) {
                val a = listOf(
                    //white music type
                    SoundBedtime(
                        R.string.alpha_wave,
                        R.drawable.alpha_brain,
                        R.raw.alpha_wave,
                        SoundType.WHITE_MUSIC.index
                    ),
                    SoundBedtime(
                        R.string.beta_wave,
                        R.drawable.maxresdefault,
                        R.raw.beta_wave,
                        SoundType.WHITE_MUSIC.index
                    ),
                    SoundBedtime(
                        R.string.brain_wave,
                        R.drawable.brainwave,
                        R.raw.brain_wave,
                        SoundType.WHITE_MUSIC.index
                    ),
                    SoundBedtime(
                        R.string.heartbeat,
                        R.drawable.heartbeat,
                        R.raw.heartbeat,
                        SoundType.WHITE_MUSIC.index
                    ),
                    SoundBedtime(
                        R.string.light_white_noise,
                        R.drawable.light_white_noise_m,
                        R.raw.light_white_noise,
                        SoundType.WHITE_MUSIC.index
                    ),
                    SoundBedtime(
                        R.string.white_noise,
                        R.drawable.white_noise,
                        R.raw.white_noise,
                        SoundType.WHITE_MUSIC.index
                    ),

                    //nature type
                    SoundBedtime(
                        R.string.slightly_rain,
                        R.drawable.slightly_rain,
                        R.raw.slighty_rain,
                        SoundType.NATURE.index
                    ),
                    SoundBedtime(
                        R.string.fireplace,
                        R.drawable.fireplace,
                        R.raw.fireplace,
                        SoundType.NATURE.index
                    ),
                    SoundBedtime(
                        R.string.wave,
                        R.drawable.waves,
                        R.raw.wave,
                        SoundType.NATURE.index
                    ),
                    SoundBedtime(
                        R.string.moderate_rain,
                        R.drawable.moderate_rain,
                        R.raw.moderate_rain,
                        SoundType.NATURE.index
                    ),
                    SoundBedtime(
                        R.string.river_in_forest,
                        R.drawable.river_in_the_forest,
                        R.raw.river_in_the_forest,
                        SoundType.NATURE.index
                    ),
                    SoundBedtime(
                        R.string.heavy_rain_1,
                        R.drawable.heavy_rain,
                        R.raw.heavy_rain,
                        SoundType.NATURE.index
                    ),
                    SoundBedtime(
                        R.string.heavy_rain_2,
                        R.drawable.forest,
                        R.raw.heavy_rain2,
                        SoundType.NATURE.index
                    ),

                    //meditation type
                    SoundBedtime(
                        R.string.temple_bell,
                        R.drawable.temple_bell,
                        R.raw.temple_bell,
                        SoundType.MEDITATION.index
                    ),
                    SoundBedtime(
                        R.string.wind_chimes,
                        R.drawable.wind_chimes,
                        R.raw.wind_chimes,
                        SoundType.MEDITATION.index
                    ),
                    SoundBedtime(
                        R.string.zen,
                        R.drawable.zen,
                        R.raw.zen,
                        SoundType.MEDITATION.index
                    ),
                    SoundBedtime(
                        R.string.light_white_noise,
                        R.drawable.light_white_noise,
                        R.raw.light_white_noise,
                        SoundType.MEDITATION.index
                    ),

                    //ASMR type
                    SoundBedtime(
                        R.string.soluble_bath,
                        R.drawable.soluble_bath_salts,
                        R.raw.slighty_rain,
                        SoundType.ASMR.index
                    ),
                    SoundBedtime(
                        R.string.type_the_keyboard,
                        R.drawable.type_the_keyboard,
                        R.raw.type_the_keyboard,
                        SoundType.ASMR.index
                    ),
                    SoundBedtime(
                        R.string.tea,
                        R.drawable.tea,
                        R.raw.temple_bell,
                        SoundType.ASMR.index
                    ),
                    SoundBedtime(
                        R.string.relaxing_bath,
                        R.drawable.relaxing_bath,
                        R.raw.relaxing_bath,
                        SoundType.ASMR.index
                    ),
                    SoundBedtime(
                        R.string.type_writer,
                        R.drawable.typewriter,
                        R.raw.typewriter,
                        SoundType.ASMR.index
                    ),
                    SoundBedtime(
                        R.string.coloring_book,
                        R.drawable.oloring_book,
                        R.raw.wind_chimes,
                        SoundType.ASMR.index
                    ),
                    SoundBedtime(
                        R.string.pencil,
                        R.drawable.pencil,
                        R.raw.pencil,
                        SoundType.ASMR.index
                    ),
                )
                a.forEachIndexed { index, soundBedtime ->
                    soundBedtime.id = index
                }
                listSound = a
                return a
            } else {
                return listSound ?: emptyList()
            }

        }
    }
}
