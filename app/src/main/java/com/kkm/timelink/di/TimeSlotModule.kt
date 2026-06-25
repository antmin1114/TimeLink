package com.kkm.timelink.di

import com.kkm.timelink.data.timeslot.FirestoreTimeSlotRepository
import com.kkm.timelink.domain.repository.TimeSlotRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeSlotModule {

    @Binds
    @Singleton
    abstract fun bindTimeSlotRepository(
        repository: FirestoreTimeSlotRepository
    ): TimeSlotRepository
}
