package com.example.project.modules


import com.example.project.repositories.AuthRepository
import com.example.project.repositories.SignUpRepository
import com.example.project.viewmodels.ClientViewModel
import com.example.project.viewmodels.CollectInfoViewModel
import com.google.firebase.functions.dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object CollectionInfoModule {

    @Provides
    @ViewModelScoped
    fun provideSignRepository(): SignUpRepository {
        return SignUpRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @ViewModelScoped
    fun provideCollectInfoViewModel(signUpRepository: SignUpRepository, authRepository: AuthRepository, clientViewModelFactory: ClientViewModel.Factory): CollectInfoViewModel {
        return CollectInfoViewModel(authRepository, signUpRepository, clientViewModelFactory)
    }
}


