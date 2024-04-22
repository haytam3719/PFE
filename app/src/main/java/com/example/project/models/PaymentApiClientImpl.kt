package com.example.project.models

import RetrofitClient
import android.util.Log
import com.example.project.prototype.PaymentApiClient

class PaymentApiClientImpl : PaymentApiClient {
    override suspend fun getBills(): List<Bill> {
        return try {
            Log.d("Fetching", "Fetching bills from server")

            val response = RetrofitClient.paymentApiClient.getBills()
            if (response.isNotEmpty()) {
                val bills: List<Bill> = response

                Log.d("Success", "Bills fetched successfully: $bills")

                bills ?: emptyList()
            } else {
                Log.e("ERROR", "Failed to get bills")

                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Error fetching bills", e)

            emptyList()
        }
    }

    override suspend fun requestPayment(paymentRequest: PaymentRequest): Result<PaymentResponse> {
        return try {
            val response = RetrofitClient.paymentApiClient.requestPayment(paymentRequest)
            if (response.isSuccess) {
                Result.success(response.getOrThrow())
            } else {
                Result.failure(Exception("Failed to make payment: ${response.isFailure}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMerchantApproval(merchantId: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}
