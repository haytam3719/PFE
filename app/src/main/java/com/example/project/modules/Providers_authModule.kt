package com.example.project.modules

import android.app.Application
import com.example.project.models.RetrofitClient
import com.example.project.oAuthRessources.SecureManager
import com.example.project.repositories.AuthRepository
import com.example.project.repositories.ClientRepositoryImpl
import com.example.project.repositories.OAuthRepository
import com.example.project.viewmodels.AuthViewModel
import dagger.Module
import dagger.Provides
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
    fun provideRetrofit(): RetrofitClient {
        return RetrofitClient
    }

    @Provides
    @ViewModelScoped
    fun provideOAuthRepository(retrofit: RetrofitClient): OAuthRepository {
        return OAuthRepository(retrofit)
    }

    @Provides
    @ViewModelScoped
    fun provideSecureManager(application: Application): SecureManager {
        return SecureManager(application.applicationContext)
    }


    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(authRepository: AuthRepository,oAuthRepository: OAuthRepository,secureManager: SecureManager, clientRepositoryImpl: ClientRepositoryImpl): AuthViewModel {
        return AuthViewModel(authRepository,oAuthRepository,secureManager,clientRepositoryImpl)
    }


}





