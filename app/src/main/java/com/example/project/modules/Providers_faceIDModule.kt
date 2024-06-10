package com.example.project.modules

import com.example.project.repositories.FaceIdRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object FaceIDImplModule {
    @Provides
    @ViewModelScoped
    fun provideFaceIDRepository(): FaceIdRepository {
        return FaceIdRepository()
    }
}