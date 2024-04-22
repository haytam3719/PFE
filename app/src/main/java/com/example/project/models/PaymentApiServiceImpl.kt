package com.example.project.models

import com.example.project.prototype.PaymentApiClient
import com.example.project.prototype.PaymentApiService
import javax.inject.Inject

class PaymentApiServiceImpl @Inject constructor(private val apiClient: PaymentApiClient) : PaymentApiService {
    override suspend fun getBills(): List<Bill> {
        return apiClient.getBills()
    }

    override suspend fun requestPayment(paymentRequest: PaymentRequest): Result<PaymentResponse> {
        return apiClient.requestPayment(paymentRequest)
    }
}