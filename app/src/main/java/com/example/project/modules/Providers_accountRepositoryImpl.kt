package com.example.project.modules

import com.example.project.models.AccountCreationServiceImpl
import com.example.project.prototype.AccountRepository
import com.example.project.repositories.AccountRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object AccountCreateServiceImplModule {
    @Provides
    @ViewModelScoped
    fun provideAccountRepository(accountService: AccountCreationServiceImpl): AccountRepository {
        return AccountRepositoryImpl(accountService)
    }
}

