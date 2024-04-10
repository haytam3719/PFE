package com.example.project.modules

import com.example.project.models.CompteImpl
import com.example.project.models.Virement
import com.example.project.prototype.AccountRepository
import com.example.project.viewmodels.VirementViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object VirementModule {

    @Provides
    fun provideVirement(
        @Named("idTran") idTran: String,
        @Named("montant") montant: Double,
        @Named("date") date: String,
        @Named("motif") motif: String,
        @CompteModule.CompteBenefQualifier compteBenef: CompteImpl, // Inject CompteImpl dependency
        @CompteModule.CompteEmetQualifier compteEmet: CompteImpl, // Inject CompteImpl dependency
        @Named("statut") statut: String,
        @Named("methodPaiement") methodPaiement: String,
        @Named("fraisTrans") fraisTrans: Double,
        @Named("typeTransaction") typeTransaction: String
    ): Virement {
        // Return a new instance of Virement with the provided arguments
        return Virement(idTran, montant, date, motif, compteBenef, compteEmet, statut, methodPaiement, fraisTrans, typeTransaction)
    }

    @Provides
    @ViewModelScoped
    @Named("idTran")
    fun provideIdTran(): String = "123456"

    @Provides
    @ViewModelScoped
    @Named("montant")
    fun provideMontant(): Double = 100.0

    @Provides
    @ViewModelScoped
    @Named("date")
    fun provideDate(): String = "2022-04-05"

    @Provides
    @ViewModelScoped
    @Named("motif")
    fun provideMotif(): String = "Sample motif"

    @Provides
    @ViewModelScoped
    @Named("statut")
    fun provideStatut(): String = "Completed"

    @Provides
    @ViewModelScoped
    @Named("methodPaiement")
    fun provideMethodPaiement(): String = "Online"

    @Provides
    @ViewModelScoped
    @Named("fraisTrans")
    fun provideFraisTrans(): Double = 5.0

    @Provides
    @ViewModelScoped
    @Named("typeTransaction")
    fun provideTypeTransaction(): String = "Some type"
}






@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideVirementViewModel(virement: Virement,accountRepository: AccountRepository): VirementViewModel {
        return VirementViewModel(virement,accountRepository)
    }
}

