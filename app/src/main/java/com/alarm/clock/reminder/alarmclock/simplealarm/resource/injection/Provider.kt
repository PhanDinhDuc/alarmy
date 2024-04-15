package com.alarm.clock.reminder.alarmclock.simplealarm.resource.injection

import android.content.Context
import androidx.room.Room
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.AlarmTypeConverters
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DAO
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.DiaryDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.FeelingDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.dao.ReminderDao
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.database.AlarmDatabase
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.AlarmRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.AlarmRepositoryImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.BedtimeRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.BedtimeRepositoryImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.DiaryRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.DiaryRepositoryImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.MyDayFeelingRepo
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.MyDayFeelingRepoImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.ReminderRepository
import com.alarm.clock.reminder.alarmclock.simplealarm.data.local.repository.ReminderRepositoryImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.HoroscopeFetch
import com.alarm.clock.reminder.alarmclock.simplealarm.data.network.HoroscopeFetchImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.AlarmHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.BedtimeHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.receiver.helper.ReminderHelper
import com.alarm.clock.reminder.alarmclock.simplealarm.view.shake.ShakeCounter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.shake.ShakeCounterImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.view.step.StepCounter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.step.StepCounterImpl
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.squat.SquatCounter
import com.alarm.clock.reminder.alarmclock.simplealarm.view.task.squat.SquatCounterImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Provider {

    @Singleton
    @Provides
    fun provideAlarmDatabase(
        @ApplicationContext context: Context,
        alarmTypeConverters: AlarmTypeConverters
    ): AlarmDatabase {
        return Room.databaseBuilder(context, AlarmDatabase::class.java, "Alarm_DB")
            .addTypeConverter(alarmTypeConverters)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideAlarmDao(alarmDatabase: AlarmDatabase): DAO = alarmDatabase.dao()

    @Singleton
    @Provides
    fun provideReminderDao(alarmDatabase: AlarmDatabase): ReminderDao = alarmDatabase.reminderDao()

    @Singleton
    @Provides
    fun provideAlarmRepository(dao: DAO): AlarmRepository = AlarmRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideBedtimeRepository(dao: DAO): BedtimeRepository = BedtimeRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideFellingDao(alarmDatabase: AlarmDatabase): FeelingDao = alarmDatabase.mydayDao()

    @Singleton
    @Provides
    fun provideDiaryDao(alarmDatabase: AlarmDatabase): DiaryDao = alarmDatabase.mydayDiaryDao()

    @Singleton
    @Provides
    fun provideMyDayFeelingRepo(dao: FeelingDao): MyDayFeelingRepo = MyDayFeelingRepoImpl(dao)

    @Singleton
    @Provides
    fun provideDiaryRepository(dao: DiaryDao): DiaryRepository = DiaryRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideReminderRepository(
        reminderDao: ReminderDao,
        reminderHelper: ReminderHelper
    ): ReminderRepository = ReminderRepositoryImpl(reminderDao, reminderHelper)

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .generateNonExecutableJson()
            .create()
    }

    @Provides
    fun provideConverters(gson: Gson): AlarmTypeConverters {
        return AlarmTypeConverters(gson)
    }

    @Provides
    fun provideAlarmHelper(@ApplicationContext context: Context) = AlarmHelper(context)

    @Provides
    fun provideReminderHelper(@ApplicationContext context: Context, reminderDao: ReminderDao) =
        ReminderHelper(context, reminderDao)

    @Provides
    fun provideBedtimeHelper(@ApplicationContext context: Context) = BedtimeHelper(context)

    @Provides
    fun provideContext(@ApplicationContext context: Context) = context.applicationContext

    @Provides
    fun provideSquatCounter(@ApplicationContext context: Context): SquatCounter {
        return SquatCounterImpl(context)
    }

    @Provides
    fun provideStepCounter(@ApplicationContext context: Context): StepCounter {
        return StepCounterImpl(context)
    }

    @Provides
    fun provideShakeCounter(@ApplicationContext context: Context): ShakeCounter {
        return ShakeCounterImpl(context)
    }

    @Provides
    @Singleton
    fun provideHoroScope(horoScope: HoroscopeFetchImpl): HoroscopeFetch {
        return horoScope
    }
}