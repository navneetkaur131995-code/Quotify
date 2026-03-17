package com.quotify.core.di

import com.quotify.core.data.network.APIService
import com.quotify.core.data.network.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/* Diff between retrofit and okhttp3
* Retrofit : Helps to talk to APIs using okHttp3 internally but hides ugly details (okhttp working which is very ugly or complicated inside
* OkHttp: It is what that actually sends or receives data from the server (Like a truck actually carrying material between producer & consumer)
*
* Retrofit can actually work here without us setting okHttp but we lose control:
* Why do we need to setup our custom OkHttpClient?
* - Interceptors (Very important): Interceptors let you :
*             a. Add auth headers
*             b. Log requests
*             c. Handle errors globally
*             d. Refresh tokens
* - Logging: bcz debugging APIs is very painful process.
* - Timeout & Retries
* - Caching
* */

@Module
@InstallIn(SingletonComponent::class) // This tells that where does this component live?
class NetworkModule {

    @Provides
    @Singleton  // This tells how many instances exist of this class exists?
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {
        // In NetworkModule.kt
        // This is a "Trust All" manager - ONLY for debugging!
        val trustAllCerts = arrayOf<TrustManager>(object :
            X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Ignore hostname mismatches
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        //        return OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }).build()
    }

    @Provides
    @Singleton
    fun provideAPIService(retrofit: Retrofit): APIService {
        return retrofit.create(APIService::class.java)
    }
}
