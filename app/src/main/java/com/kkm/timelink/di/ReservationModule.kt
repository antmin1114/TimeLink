package com.kkm.timelink.di

import com.kkm.timelink.data.reservation.FirestoreReservationRepository
import com.kkm.timelink.domain.repository.ReservationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReservationModule {

    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        repository: FirestoreReservationRepository
    ): ReservationRepository
}
