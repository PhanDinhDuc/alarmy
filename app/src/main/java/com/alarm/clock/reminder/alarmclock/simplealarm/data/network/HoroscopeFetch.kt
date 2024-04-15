package com.alarm.clock.reminder.alarmclock.simplealarm.data.network

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.Keep
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.Settings
import com.alarm.clock.reminder.alarmclock.simplealarm.resource.customView.Language
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface HoroscopeFetch {
    enum class Zodiac(val param: Int) {
        aries(1), taurus(2), gemini(3), cancer(4), leo(5), virgo(6), libra(7), scorpio(8), sagittarius(
            9
        ),
        capricorn(10), aquarius(11), pisces(12);

        fun rangeDate() = when (this) {
            aries -> Pair(
                LocalDate.of(LocalDate.now().year, 3, 21), LocalDate.of(LocalDate.now().year, 4, 19)
            )

            taurus -> Pair(
                LocalDate.of(LocalDate.now().year, 4, 20), LocalDate.of(LocalDate.now().year, 5, 20)
            )

            gemini -> Pair(
                LocalDate.of(LocalDate.now().year, 5, 21), LocalDate.of(LocalDate.now().year, 6, 21)
            )

            cancer -> Pair(
                LocalDate.of(LocalDate.now().year, 6, 22), LocalDate.of(LocalDate.now().year, 7, 22)
            )

            leo -> Pair(
                LocalDate.of(LocalDate.now().year, 7, 23), LocalDate.of(LocalDate.now().year, 8, 22)
            )

            virgo -> Pair(
                LocalDate.of(LocalDate.now().year, 8, 23), LocalDate.of(LocalDate.now().year, 9, 22)
            )

            libra -> Pair(
                LocalDate.of(LocalDate.now().year, 9, 23),
                LocalDate.of(LocalDate.now().year, 10, 23)
            )

            scorpio -> Pair(
                LocalDate.of(LocalDate.now().year, 10, 24),
                LocalDate.of(LocalDate.now().year, 11, 21)
            )

            sagittarius -> Pair(
                LocalDate.of(LocalDate.now().year, 11, 22),
                LocalDate.of(LocalDate.now().year, 12, 21)
            )

            aquarius -> Pair(
                LocalDate.of(LocalDate.now().year, 1, 20), LocalDate.of(LocalDate.now().year, 2, 18)
            )

            pisces -> Pair(
                LocalDate.of(LocalDate.now().year, 2, 19), LocalDate.of(LocalDate.now().year, 3, 20)
            )

            capricorn -> Pair(
                LocalDate.of(LocalDate.now().year, 12, 22),
                LocalDate.of(LocalDate.now().year, 1, 19)
            )
        }

        val luckyColor: LuckyColor
            get() = when (this) {
                aries -> LuckyColor.RED
                taurus -> LuckyColor.Green
                gemini -> LuckyColor.Yellow
                cancer -> listOf(LuckyColor.White, LuckyColor.Silver).random()
                leo -> listOf(LuckyColor.Gold).random()
                virgo -> listOf(LuckyColor.Green, LuckyColor.Brown).random()
                libra -> listOf(LuckyColor.Pink, LuckyColor.Blue).random()
                scorpio -> listOf(LuckyColor.Black).random()
                sagittarius -> listOf(LuckyColor.Purple).random()
                capricorn -> listOf(LuckyColor.Brown, LuckyColor.Grey).random()
                aquarius -> listOf(LuckyColor.Blue).random()
                pisces -> listOf(LuckyColor.LIGHT_GREEN).random()
            }

        fun getTitle(context: Context) = when (this) {
            aries -> context.getString(R.string.aries)
            taurus -> context.getString(R.string.taurus)
            gemini -> context.getString(R.string.gemini)
            cancer -> context.getString(R.string.cancer)
            leo -> context.getString(R.string.leo)
            virgo -> context.getString(R.string.virgo)
            libra -> context.getString(R.string.libra)
            scorpio -> context.getString(R.string.scorpio)
            sagittarius -> context.getString(R.string.sagittarius)
            capricorn -> context.getString(R.string.capricorn)
            aquarius -> context.getString(R.string.aquarius)
            pisces -> context.getString(R.string.pisces)
        }

        val luckyNumber: Int
            get() = when (this) {
                aries -> listOf(6, 24, 64).random()
                taurus -> listOf(6, 11, 17).random()
                gemini -> listOf(3, 5).random()
                cancer -> listOf(1, 58, 24).random()
                leo -> listOf(9, 19, 49).random()
                virgo -> listOf(0, 14, 49).random()
                libra -> listOf(11, 77, 88, 99, 66, 8, 2).random()
                scorpio -> listOf(5, 18, 69).random()
                sagittarius -> listOf(5, 7, 15, 28).random()
                capricorn -> listOf(3, 5, 14, 95).random()
                aquarius -> listOf(2, 17, 77, 35).random()
                pisces -> listOf(15, 36, 21, 5, 7).random()
            }

        companion object {
            fun get(id: Int) = values().firstOrNull { it.param == id } ?: aries

            fun saveDateOfBirth(date: LocalDate) {
                Settings.DATEOFBIRTH.put(date.toEpochDay())
            }

            fun saveGender(@IntRange(0, 1) gender: Int) {
                Settings.GENDER.put(gender)
            }

            fun getGender(): Int {
                return Settings.GENDER.get(-1)
            }

            fun getDateOfBirth(): LocalDate {
                val date = Settings.DATEOFBIRTH.get(0L)
                if (date == 0L) return LocalDate.now()
                return LocalDate.ofEpochDay(date)
            }

            fun getZodiacLocal(): Zodiac {
                val birth = getDateOfBirth()
                return check(
                    LocalDate.of(
                        LocalDate.now().year, birth.month.value, birth.dayOfMonth
                    )
                )
            }

            fun check(date: LocalDate): Zodiac {
                var zodiac = Zodiac.capricorn
                for (value in values()) {
                    val range = value.rangeDate()
                    if (range.first <= date && date <= range.second) {
                        zodiac = value
                        break
                    }
                }

                return zodiac
            }
        }
    }

    enum class HoroScopeDay(val param: String) {
        TODAY("today"), TOMORROW("tomorrow")
    }

    suspend fun getHoroscope(zodiac: Zodiac, day: HoroScopeDay): HoroscopeData?
}

@Singleton
class HoroscopeFetchImpl @Inject constructor(@ApplicationContext val context: Context) :
    HoroscopeFetch {
    private val today = LocalDate.now(ZoneId.of("America/Los_Angeles"))
    fun Context.isNetworkConnection(): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals(
                    "WIFI", ignoreCase = true
                )
            ) if (ni.isConnected) haveConnectedWifi = true
            if (ni.typeName.equals(
                    "MOBILE", ignoreCase = true
                )
            ) if (ni.isConnected) haveConnectedMobile = true
        }
        return haveConnectedWifi || haveConnectedMobile
    }

    private suspend fun fetchAllLocal(): SaveHoroscopeData? = withContext(Dispatchers.IO) {
        val dataString = Settings.HOROSCOPE_DATA.get("")
        if (dataString.isEmpty() || dataString.isBlank()) return@withContext null
        return@withContext gson.fromJson<SaveHoroscopeData>(dataString)
    }

    private suspend fun fetchLocal(
        zodiac: HoroscopeFetch.Zodiac, day: HoroscopeFetch.HoroScopeDay
    ): HoroscopeData? = withContext(Dispatchers.IO) {
        return@withContext fetchAllLocal()?.data?.firstOrNull {
            val compareDay: Long = if (day == HoroscopeFetch.HoroScopeDay.TODAY) {
                today.toEpochDay()
            } else {
                today.plusDays(1).toEpochDay()
            }

            compareDay == it.date && it.zodiac == zodiac.param
        }
    }

    private suspend fun saveLocal(data: HoroscopeData) = withContext(Dispatchers.IO) {
        val data1 = fetchAllLocal() ?: SaveHoroscopeData(emptyList())
        val saveData = data1.data.toMutableList()
        saveData.removeAll {
            (it.zodiac == data.zodiac && it.date == data.date) || it.date < today.toEpochDay()
        }
        saveData.add(data)
        Settings.HOROSCOPE_DATA.put(gson.toJson(data1.copy(data = saveData)))
    }

    override suspend fun getHoroscope(
        zodiac: HoroscopeFetch.Zodiac, day: HoroscopeFetch.HoroScopeDay
    ): HoroscopeData? = withContext(Dispatchers.IO) {

        fetchLocal(zodiac, day)?.let { return@withContext it.translate(Language.current) }

        val localDay = if (day == HoroscopeFetch.HoroScopeDay.TODAY) today else today.plusDays(1)

        val todayDes = async { getDescription(day, zodiac) }.await()
        return@withContext HoroscopeData(
            zodiac.param,
            (todayDes?.second ?: localDay).toEpochDay(),
            listOf(LanguageDescribe(Language.ENGLISH.localizeCode, todayDes?.first)),
            zodiac.luckyColor.id,
            zodiac.luckyNumber
        ).apply {
            saveLocal(this)
        }.translate(
            Language.current
        )
    }

    private val gson by lazy {
        return@lazy GsonBuilder().setLenient().create()
    }

    private suspend fun HoroscopeData.translate(
        lang: Language
    ): HoroscopeData? = withContext(Dispatchers.IO) {
        if (!context.isNetworkConnection()) return@withContext null

        this@translate.describe.firstOrNull { it.lang == lang.localizeCode }
            ?.let { return@withContext this@translate }

        val describeEn =
            this@translate.describe.firstOrNull { it.lang == Language.ENGLISH.localizeCode }
                ?: return@withContext null

        try {
            val json = URL(
                Uri.parse("https://api.mymemory.translated.net/get").buildUpon()
                    .appendQueryParameter("q", describeEn.content)
                    .appendQueryParameter("langpair", "en|${lang.localizeCode}").build().toString()
            ).openStream().bufferedReader().use { it.readText() }
            val translate: Translate = gson.fromJson(json)
            val resultText = this@translate.describe.toMutableList()
            resultText.add(
                LanguageDescribe(
                    lang.localizeCode, translate.responseData.translateText
                )
            )
            val result = this@translate.copy(describe = resultText)
            saveLocal(result)
            return@withContext result
        } catch (e: IOException) {
            return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private suspend fun getDescription(
        day: HoroscopeFetch.HoroScopeDay, zodiac: HoroscopeFetch.Zodiac
    ): Pair<String?, LocalDate?>? = withContext(Dispatchers.IO) {
        if (!context.isNetworkConnection()) return@withContext null
        try {
            val doc =
                Jsoup.connect("https://www.horoscope.com/us/horoscopes/general/horoscope-general-daily-${day.param}.aspx?sign=${zodiac.param}")
                    .get()
            val text = doc.select("p").first()?.text()?.trim()
            val list = text?.split("-")?.toMutableList()
            val date = list?.firstOrNull()?.let {
                return@let LocalDate.parse(it.trim(), DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US))
            }
            list?.removeFirst()
            return@withContext list?.joinToString("-")?.trim() to date
        } catch (e: Exception) {
            return@withContext null
        }
    }
}

enum class LuckyColor(val id: Int) {
    RED(1), Green(2), Yellow(3), White(4), Silver(5), Gold(6), Brown(7), Pink(8), Blue(9), Black(10), Purple(
        11
    ),
    Grey(12), LIGHT_GREEN(13);

    val color: Int
        get() = when (this) {
            RED -> Color.RED
            Green -> Color.GREEN
            Yellow -> Color.YELLOW
            White -> Color.WHITE
            Silver -> Color.parseColor("#C0C0C0")
            Gold -> Color.parseColor("#FFD700")
            Brown -> Color.RED
            Pink -> Color.parseColor("#FFC0CB")
            Blue -> Color.BLUE
            Black -> Color.BLACK
            Purple -> Color.parseColor("#800080")
            Grey -> Color.GRAY
            LIGHT_GREEN -> Color.parseColor("#90EE90")
        }

    fun getName(context: Context): String {
        return when (this) {
            RED -> context.getString(R.string.red)
            Green -> context.getString(R.string.green)
            Yellow -> context.getString(R.string.yellow)
            White -> context.getString(R.string.white)
            Silver -> context.getString(R.string.silver)
            Gold -> context.getString(R.string.gold)
            Brown -> context.getString(R.string.brown)
            Pink -> context.getString(R.string.pink)
            Blue -> context.getString(R.string.blue)
            Black -> context.getString(R.string.black)
            Purple -> context.getString(R.string.purple)
            Grey -> context.getString(R.string.grey)
            LIGHT_GREEN -> context.getString(R.string.light_green)
        }
    }

    companion object {
        fun get(id: Int) = values().firstOrNull { it.id == id } ?: RED
    }
}

@Keep
data class LanguageDescribe(
    @SerializedName("lang") val lang: String, @SerializedName("content") var content: String? = null
)

@Keep
data class HoroscopeData(
    @SerializedName("zodiac") val zodiac: Int,
    @SerializedName("date") val date: Long,
    @SerializedName("describe") val describe: List<LanguageDescribe> = emptyList(),
    @SerializedName("luckyColor") val luckyColor: Int,
    @SerializedName("luckyNumber") val luckyNumber: Int
)

@Keep
data class SaveHoroscopeData(@SerializedName("data") var data: List<HoroscopeData>)

@Keep
data class Translate(
    @SerializedName("responseData") @Expose var responseData: TranslateText,
)

@Keep
data class TranslateText(@SerializedName("translatedText") @Expose var translateText: String)

inline fun <reified T> Gson.fromJson(json: String): T =
    fromJson(json, object : TypeToken<T>() {}.type)
