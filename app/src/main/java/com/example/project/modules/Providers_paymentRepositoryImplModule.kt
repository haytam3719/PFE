package com.example.project.modules

import com.example.project.models.Paiement
import com.example.project.models.PaymentApiClientImpl
import com.example.project.models.PaymentApiServiceImpl
import com.example.project.prototype.PaymentApiClient
import com.example.project.prototype.PaymentApiService
import com.example.project.prototype.PaymentRepository
import com.example.project.repositories.PaymentRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object PaymentRepositoryImplModule {
    @Provides
    @ViewModelScoped
    fun providePaymentApiClient(impl:PaymentApiClientImpl): PaymentApiClient {
        return impl
    }

    @Provides
    @ViewModelScoped
    fun providePaymentApiClientImpl(): PaymentApiClientImpl {
        return PaymentApiClientImpl()
    }

    @Provides
    fun providePaymentApiService(paymentApiClientImpl: PaymentApiClientImpl): PaymentApiService {
        return PaymentApiServiceImpl(paymentApiClientImpl)
    }

    @Provides
    fun providePaiement() : Paiement{
        return Paiement()
    }

    @Provides
    fun providePaymentRepository(paymentApiService: PaymentApiService,paiement: Paiement): PaymentRepository {
        return PaymentRepositoryImpl(paymentApiService,paiement)
    }
}
