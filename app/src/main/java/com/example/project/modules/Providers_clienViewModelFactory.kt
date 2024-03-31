package com.example.project.modules

import com.example.project.viewmodels.ClientViewModel
import com.google.firebase.functions.dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ClientViewModelFactoryModule {

    @Provides
    @ViewModelScoped
    fun provideClientViewModelFactory(): ClientViewModel.Factory {
        return ClientViewModel.Factory()
    }
}