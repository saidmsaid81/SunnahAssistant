package com.thesunnahrevival.sunnahassistant.data.remote

import com.thesunnahrevival.sunnahassistant.data.model.ResourceLinks
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ResourceApiInterface {
    @GET("resources/links")
    suspend fun getResourceLinks(): Response<ResourceLinks?>

    @GET
    @Streaming
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>
}