package com.example.project.prototype

import com.example.project.models.Bill
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse

interface PaymentRepository {
    suspend fun getBills(): List<Bill>
    suspend fun requestPayment(paymentRequest: PaymentRequest): Result<PaymentResponse>
    suspend fun makePaiement(amount:Double, motif: String)
}
