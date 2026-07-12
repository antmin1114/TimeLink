package com.kkm.timelink.di

import com.google.firebase.messaging.FirebaseMessaging
import com.kkm.timelink.data.messaging.FirebaseNotificationRepository
import com.kkm.timelink.domain.repository.NotificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        repository: FirebaseNotificationRepository
    ): NotificationRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseMessagingModule {

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
}
