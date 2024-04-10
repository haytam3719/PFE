package com.example.project.modules


import com.example.project.models.CompteImpl
import com.example.project.models.TransactionImpl
import com.example.project.prototype.TypeCompte
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named
import javax.inject.Qualifier

@Module
@InstallIn(ActivityComponent::class)
object TransactionModule {

    @Provides
    @ViewModelScoped
    fun provideTransactionImpl(
        @Named("idTran") idTran: String,
        @Named("montant") montant: Double,
        @Named("date") date: String,
        @Named("motif") motif: String,
        @CompteModule.CompteBenefQualifier compteBenef: CompteImpl, // Inject compteBenef with the CompteBenefQualifier
        @CompteModule.CompteEmetQualifier compteEmet: CompteImpl,
        @Named("statut") statut: String,
        @Named("methodPaiement") methodPaiement: String,
        @Named("fraisTrans") fraisTrans: Double,
        @Named("typeTransaction") typeTransaction: String,
    ): TransactionImpl {
        return TransactionImpl(idTran, montant, date, motif, compteBenef, compteEmet, statut, methodPaiement, fraisTrans, typeTransaction)
    }

    @Provides
    @Named("idTran")
    fun provideIdTran(): String = "123456"

    @Provides
    @Named("montant")
    fun provideMontant(): Double = 100.0

    @Provides
    @Named("date")
    fun provideDate(): String = "2022-04-05"

    @Provides
    @Named("motif")
    fun provideMotif(): String = "Sample motif"

    @Provides
    @Named("statut")
    fun provideStatut(): String = "Completed"

    @Provides
    @Named("methodPaiement")
    fun provideMethodPaiement(): String = "Online"

    @Provides
    @Named("fraisTrans")
    fun provideFraisTrans(): Double = 5.0

    @Provides
    @Named("typeTransaction")
    fun provideTypeTransaction(): String = "Some type"



}



@Module
@InstallIn(ViewModelComponent::class)
object CompteModule {

    @Provides
    @ViewModelScoped
    fun provideCompteImpl(
        @Named("numero") numero: String,
        @Named("id_proprietaire") id_proprietaire: String,
        @Named("typeCompte") type: TypeCompte,
        @Named("solde") solde: Double,

    ): CompteImpl {
        return CompteImpl(
            numero,
            id_proprietaire,
            type,
            solde,
            mutableListOf()

        )
    }

    @Provides
    @Named("numero")
    fun provideNumero(): String = "123456"

    @Provides
    @Named("id_proprietaire")
    fun provideIdProprietaire(): String = "owner123"

    @Provides
    @Named("solde")
    fun provideSolde(): Double = 1000.0



    @Provides
    @Named("typeCompte")
    fun provideTypeCompte(): TypeCompte = TypeCompte.COURANT





    @Provides
    @ViewModelScoped
    @CompteBenefQualifier // Qualifier for compteBenef
    fun provideCompteBenef(
        @Named("numeroBenef") numero: String, // Use different named annotations for beneficiaire
        @Named("id_proprietaireBenef") id_proprietaire: String,
        @Named("typeCompteBenef") type: TypeCompte,
        @Named("soldeBenef") solde: Double,

    ): CompteImpl {
        return CompteImpl(
            numero,
            id_proprietaire,
            type,
            solde,
            mutableListOf()
        )
    }



    @Provides
    @Named("numeroBenef")
    fun provideNumeroBenef(): String = "123456"

    @Provides
    @Named("id_proprietaireBenef")
    fun provideIdProprietaireBenef(): String = "owner123"

    @Provides
    @Named("soldeBenef")
    fun provideSoldeBenef(): Double = 1000.0


    @Provides
    @Named("typeCompteBenef")
    fun provideTypeCompteBenef(): TypeCompte = TypeCompte.COURANT




    @Provides
    @ViewModelScoped
    @CompteEmetQualifier // Qualifier for compteEmet
    fun provideCompteEmet(
        @Named("numeroEmet") numero: String, // Use different named annotations for emetteur
        @Named("id_proprietaireEmet") id_proprietaire: String,
        @Named("typeCompteEmet") type: TypeCompte,
        @Named("soldeEmet") solde: Double,
    ): CompteImpl {
        return CompteImpl(
            numero,
            id_proprietaire,
            type,
            solde,
            mutableListOf()
        )
    }

    @Provides
    @Named("numeroEmet")
    fun provideNumeroEmet(): String = "123456"

    @Provides
    @Named("id_proprietaireEmet")
    fun provideIdProprietaireEmet(): String = "owner123"

    @Provides
    @Named("soldeEmet")
    fun provideSoldeEmet(): Double = 1000.0



    @Provides
    @Named("typeCompteEmet")
    fun provideTypeCompteEmet(): TypeCompte = TypeCompte.COURANT





    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CompteBenefQualifier

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CompteEmetQualifier

}



