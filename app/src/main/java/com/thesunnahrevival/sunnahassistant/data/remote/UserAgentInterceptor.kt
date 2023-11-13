package com.thesunnahrevival.sunnahassistant.data.remote

import com.thesunnahrevival.sunnahassistant.utilities.EXPECTED_USER_AGENT
import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(private val appVersion: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val modifiedRequest = originalRequest.newBuilder()
            .header("User-Agent", EXPECTED_USER_AGENT)
            .header("App-Version", appVersion)
            .build()
        return chain.proceed(modifiedRequest)
    }
}