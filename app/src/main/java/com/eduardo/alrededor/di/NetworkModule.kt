package com.eduardo.alrededor.di

import com.eduardo.alrededor.data.remote.WikipediaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val USER_AGENT = "GuiaLoQueTeRodea/0.1 (eduardo@alrededor.com)"

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder().header("User-Agent", USER_AGENT).build()
            )
        }
        .build()

    @Provides @Singleton
    fun provideWikipediaApi(client: OkHttpClient, json: Json): WikipediaApi =
        Retrofit.Builder()
            .baseUrl("https://es.wikipedia.org/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WikipediaApi::class.java)
}
