package com.alarm.clock.reminder.alarmclock.simplealarm.view.task

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.alarm.clock.reminder.alarmclock.simplealarm.R
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.entities.Alarm
import com.alarm.clock.reminder.alarmclock.simplealarm.view.addAlarm.NewAlarmTakModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.first_alarm.FirstTaskModel
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.EasiestMathEquation
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.EasyMathEquation
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.HardMathEquation
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.HardestMathEquation
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.MathEquation
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math.MediumMathEquation
import kotlinx.parcelize.Parcelize

@Keep
enum class TaskLevel(val id: Int) {
    NONE(0), EASIEST(1), EASY(2), MEDIUM(3), HARD(4), HARDEST(5);

    @get:StringRes
    val title: Int
        get() {
            return when (this) {
                NONE -> R.string.none
                EASIEST -> R.string.easiest
                EASY -> R.string.easy
                MEDIUM -> R.string.medium
                HARD -> R.string.hard
                HARDEST -> R.string.hardest
            }
        }
    val math: MathEquation
        get() = when (this) {
            NONE -> EasyMathEquation()
            EASIEST -> EasiestMathEquation()
            EASY -> EasyMathEquation()
            MEDIUM -> MediumMathEquation()
            HARD -> HardMathEquation()
            HARDEST -> HardestMathEquation()
        }
    val memories: Pair<Int, Int>
        get() = when (this) {
            NONE -> Pair(0, 0)
            EASIEST -> Pair(3, 4)
            EASY -> Pair(4, 6)
            MEDIUM -> Pair(5, 8)
            HARD -> Pair(6, 10)
            HARDEST -> Pair(7, 12)
        }
    val rewrite: Int
        get() = when (this) {
            NONE -> 0
            EASIEST -> 2
            EASY -> 6
            MEDIUM -> 10
            HARD -> 15
            HARDEST -> 20
        }

    companion object {
        fun get(id: Int): TaskLevel? = TaskLevel.values().find { it.id == id }
        fun getListLevel() = TaskLevel.values().toList()
    }
}

@Parcelize
@Keep
data class TaskSettingModel(
    val type: TaskType,
    var level: TaskLevel = TaskLevel.NONE,
    var repeatTime: Int = 1,
    var targetValue: Int? = null,
    var barcodes: List<BarCodes>? = null,
    var isPreview: Boolean = false
) : Parcelable, NewAlarmTakModel

@Parcelize
@Keep
data class BarCodes(val nameQr: String, val idQr: String) : Parcelable

@Parcelize
@Keep
data class ListTask(val list: List<TaskSettingModel> = emptyList()) : Parcelable

@Keep
enum class TaskType(val id: Int) {
    MATH(0), MEMORY(1), REWRITE(2), SHAKE(3), STEP(4), SQUAT(5), SCANQR(6);

    val defaultData: TaskSettingModel
        get() {
            return when (this) {
                MATH -> TaskSettingModel(this, TaskLevel.EASY, 3)
                MEMORY -> TaskSettingModel(this, TaskLevel.EASY, 3)
                REWRITE -> TaskSettingModel(this, TaskLevel.EASY, 3)
                SHAKE -> TaskSettingModel(this, targetValue = 20)
                STEP -> TaskSettingModel(this, targetValue = 20)
                SQUAT -> TaskSettingModel(this, targetValue = 20)
                SCANQR -> TaskSettingModel(this, barcodes = emptyList())
            }
        }

    @get:StringRes
    val title: Int
        get() {
            return when (this) {
                MATH -> R.string.math
                MEMORY -> R.string.memorize
                REWRITE -> R.string.rewrite
                SHAKE -> R.string.shake_the_phone
                STEP -> R.string.step
                SQUAT -> R.string.squat
                SCANQR -> R.string.qr_code
            }
        }

    @get:StringRes
    val titleChoseTask: Int
        get() {
            return when (this) {
                MATH -> R.string.math
                MEMORY -> R.string.memorize
                REWRITE -> R.string.rewrite
                SHAKE -> R.string.shake_the_phone
                STEP -> R.string.step
                SQUAT -> R.string.squat
                SCANQR -> R.string.qr_code
            }
        }

    @get:DrawableRes
    val imageSetting: Int
        get() {
            return when (this) {
                MATH -> R.drawable.ic_math
                MEMORY -> R.drawable.ic_memories
                REWRITE -> R.drawable.ic_memories
                SHAKE -> R.drawable.ic_shake_phone
                STEP -> R.drawable.ic_shake_phone
                SQUAT -> R.drawable.ic_shake_phone
                SCANQR -> R.drawable.ic_shake_phone
            }
        }

    @get:DrawableRes
    val imageChoose: Int
        get() {
            return when (this) {
                MATH -> R.drawable.ic_task_math
                MEMORY -> R.drawable.ic_task_memory
                REWRITE -> R.drawable.ic_task_review
                SHAKE -> R.drawable.ic_task_shake
                STEP -> R.drawable.ic_task_step
                SQUAT -> R.drawable.ic_task_squat
                SCANQR -> R.drawable.ic_task_scanqr
            }
        }

    @IdRes
    fun getAlarmTaskIcon(alarm: Alarm): Int {
        return when (this) {
            MATH -> if (alarm.isON) R.drawable.ic_task_math else R.drawable.ic_task_math_1
            MEMORY -> if (alarm.isON) R.drawable.ic_task_memory else R.drawable.ic_task_memory_1
            REWRITE -> if (alarm.isON) R.drawable.ic_task_review else R.drawable.ic_task_review_1
            SHAKE -> if (alarm.isON) R.drawable.ic_shake_phone_blue else R.drawable.ic_shake_phone_white
            STEP -> if (alarm.isON) R.drawable.ic_task_step else R.drawable.ic_task_step_1
            SQUAT -> if (alarm.isON) R.drawable.ic_task_squat else R.drawable.ic_task_squat_1
            SCANQR -> if (alarm.isON) R.drawable.ic_task_scanqr else R.drawable.ic_task_scanqr_1
        }
    }

    @get:IdRes
    val settingFragmentId: Int
        get() = when (this) {
            MATH -> R.id.action_to_math
            MEMORY -> R.id.action_to_memories
            REWRITE -> R.id.action_to_rewrite
            SHAKE -> R.id.action_to_shake
            STEP -> R.id.action_to_step
            SQUAT -> R.id.action_to_squat
            SCANQR -> R.id.action_to_qr
        }

    @get:IdRes
    val actionFragmentId: Int
        get() = when (this) {
            MATH -> R.id.action_to_math
            MEMORY -> R.id.action_to_memories
            REWRITE -> R.id.action_to_rewrite
            SHAKE -> R.id.action_to_shake
            STEP -> R.id.action_to_step
            SQUAT -> R.id.action_to_squat
            SCANQR -> R.id.action_to_qrCode
        }

    companion object {
        fun get(id: Int): TaskType? = TaskType.values().find { it.id == id }
        fun getTutorial(): List<TaskType> = listOf(MATH, MEMORY, SHAKE)

        fun getFirstTaskSetting(): List<FirstTaskModel> {
            val tasks = getTutorial().map { FirstTaskModel(it.title, it.imageSetting, it) }
            val updatedTasks = ArrayList(tasks)
            updatedTasks.add(
                FirstTaskModel(
                    R.string.turn_off,
                    R.drawable.ic_turn_off
                )
            )
            return updatedTasks
        }
    }
}

enum class GroupTaskType(val id: Int) {
    BRAIN(0), BODY(1);

    @get:StringRes
    val title: Int
        get() {
            return when (this) {
                BRAIN -> R.string.awaken_the_brain
                BODY -> R.string.awaken_the_body
            }
        }

    val image: Int
        get() = when (this) {
            BRAIN -> R.drawable.brain2
            BODY -> R.drawable.ic_body2
        }

    val tasks: List<TaskType>
        get() {
            return when (this) {
                BRAIN -> listOf(TaskType.MATH, TaskType.MEMORY, TaskType.REWRITE)
                BODY -> listOf(TaskType.SHAKE, TaskType.STEP, TaskType.SQUAT, TaskType.SCANQR)
            }
        }

    companion object {
        fun get(id: Int): TaskType? = TaskType.values().find { it.id == id }

//        fun getListTask(): List<Any> {
//            val tasks = mutableListOf<Any>()
//            GroupTaskType.values().forEach {
//                tasks.add(it)
//                tasks.addAll(it.tasks)
//            }
//            return tasks
//        }
    }
}