package com.example.project.modules

import com.example.project.prototype.ClientRepository
import com.example.project.repositories.ClientRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ClientRepositoryModule {
    @Provides
    @ViewModelScoped
    fun provideClientRepository(): ClientRepository {
        return ClientRepositoryImpl()
    }
}