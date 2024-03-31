package com.example.project.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ContextModule {
    @Provides
    @ViewModelScoped
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}

