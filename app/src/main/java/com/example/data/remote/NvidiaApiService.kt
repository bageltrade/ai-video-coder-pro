package com.example.data.remote

import com.example.data.model.NvidiaChatRequest
import com.example.data.model.NvidiaChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NvidiaApiService {
    @POST("chat/completions")
    suspend fun generateChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: NvidiaChatRequest
    ): Response<NvidiaChatResponse>

    companion object {
        const val BASE_URL = "https://integrate.api.nvidia.com/v1/"
        const val DEFAULT_MODEL = "nvidia/nemotron-3-super-120b-a12b"
        const val DEFAULT_API_KEY = "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"
    }
}
