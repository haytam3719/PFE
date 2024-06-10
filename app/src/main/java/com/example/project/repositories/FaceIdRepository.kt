package com.example.project.repositories

import com.example.project.models.RetrofitClientML
import com.example.project.prototype.FinalDecisionResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FaceIdRepository {

    fun uploadReferenceImage(filePath: String, callback: (Boolean, String?) -> Unit) {
        val file = File(filePath)
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val call = RetrofitClientML.apiService.uploadReference(body)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true, "Reference image uploaded successfully.")
                } else {
                    callback(false, "Failed to upload reference image.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false, "Error: ${t.message}")
            }
        })
    }

    fun getFinalDecision(callback: (Boolean, String?) -> Unit) {
        val call = RetrofitClientML.apiService.getFinalDecision()
        call.enqueue(object : Callback<FinalDecisionResponse> {
            override fun onResponse(call: Call<FinalDecisionResponse>, response: Response<FinalDecisionResponse>) {
                if (response.isSuccessful) {
                    val decision = response.body()?.final_decision
                    callback(true, "Final Decision: $decision")
                } else {
                    callback(false, "Failed to get final decision.")
                }
            }

            override fun onFailure(call: Call<FinalDecisionResponse>, t: Throwable) {
                callback(false, "Error: ${t.message}")
            }
        })
    }
}
