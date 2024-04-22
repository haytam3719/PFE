package com.example.project.prototype

import com.example.project.models.Bill
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PaymentApiClient {
    @POST("/biller/bills")
    suspend fun getBills(): List<Bill>
    @POST("/request_payment") // Adjust the endpoint accordingly
    suspend fun requestPayment(@Body paymentRequest: PaymentRequest): Result<PaymentResponse>
    @GET("/merchant_approval")
    suspend fun getMerchantApproval(@Query("merchant_id") merchantId: String): Result<Unit>
}
