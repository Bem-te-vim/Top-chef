package com.sam.topchef.feature_import_from_tiktok.retrofit

import com.sam.topchef.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HTTPClient {
    private const val TIKWM_API_BASE_URL = "https://www.tikwm.com/api/"
    private const val IGRAM_API_BASE_URL = "https://api.igramapi.com"


    private fun tikWmClient(): OkHttpClient {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return okHttpClient
    }


    fun retrofitForTiktok(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TIKWM_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(tikWmClient())
            .build()
    }


    private fun igramClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->

            val request = chain.request()
                .newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer ${BuildConfig.IGRAM_API}"
                )
                .addHeader(
                    "Accept",
                    "application/json"
                )
                .build()

            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
        return okHttpClient
    }


    fun retrofitForInstagram(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IGRAM_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(igramClient())
            .build()
    }
}