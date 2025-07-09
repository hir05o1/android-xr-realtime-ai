package dev.hir05o1.example.realtime_ai.data.gpt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenAiModule {

    @Provides
    @Singleton
    fun provideGptWebSocketManager(client: OkHttpClient) = GptWebSocketManager(client)

    @Provides
    @Singleton
    fun provideGptRepository(ws: GptWebSocketManager) = GptRepository(ws)
}
