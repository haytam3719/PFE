package com.example.project.prototype

import com.example.project.models.Bill
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse

//For
interface PaymentApiService {
    suspend fun getBills(): List<Bill>

    suspend fun requestPayment(paymentRequest: PaymentRequest): Result<PaymentResponse>

}
