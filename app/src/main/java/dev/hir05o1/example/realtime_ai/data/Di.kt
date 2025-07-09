package dev.hir05o1.example.realtime_ai.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().pingInterval(15, TimeUnit.SECONDS)  // 15 秒ごとに Ping
            .build()

    @Provides
    @Singleton
    fun provideWebSocketManager(
        client: OkHttpClient,
    ) = WebSocketManager(client)
}
