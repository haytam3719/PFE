package com.example.project.modules

import com.example.project.models.AccountCreationServiceImpl
import com.example.project.prototype.AccountCreationService
import com.example.project.prototype.Compte
import com.example.project.prototype.TypeCompte
import com.example.project.repositories.AccountRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object AccountCreationModule {

    @Provides
    fun provideAccountCreationService(impl: AccountCreationServiceImpl): AccountCreationService {
        return impl
    }

    @Provides
    fun provideAccountCreationServiceImpl(): AccountCreationServiceImpl {
        return AccountCreationServiceImpl()
    }

    @Provides
    fun provideAccountRepository():AccountRepositoryImpl{
        return AccountRepositoryImpl(provideAccountCreationServiceImpl())
    }

    @Provides
    fun provideCompteImpl(
        numero: String,
        idProprietaire: String,
        typeCompte: TypeCompte,
        soldeInitial: Double
    ): Compte {
        return provideCompteImpl(numero, idProprietaire, typeCompte, soldeInitial)
    }


}
