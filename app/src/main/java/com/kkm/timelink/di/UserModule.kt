package com.kkm.timelink.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kkm.timelink.data.user.FirestoreUserRepository
import com.kkm.timelink.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        repository: FirestoreUserRepository
    ): UserRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseFirestoreModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
