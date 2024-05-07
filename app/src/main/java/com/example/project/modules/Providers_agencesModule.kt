package com.example.project.modules

import com.example.project.prototype.AgencesRepository
import com.example.project.prototype.ApiAgencesService
import com.example.project.repositories.AgencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object AgencesModule {

    @Provides
    @ViewModelScoped
    fun provideApiService(): ApiAgencesService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://gsrv.itafrica.mobi/geoserver/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiAgencesService::class.java)
    }


    @Provides
    @ViewModelScoped
    fun provideAgenceRepository(apiService: ApiAgencesService): AgencesRepository {
        return AgencesRepositoryImpl(apiService)
    }
}
