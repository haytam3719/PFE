package com.example.project.modules

import com.example.project.repositories.AuthRepository
import com.example.project.viewmodels.AuthViewModel
import com.google.firebase.functions.dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object AuthModule {

    @Provides
    @ViewModelScoped
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(authRepository: AuthRepository): AuthViewModel {
        return AuthViewModel(authRepository)
    }


}





