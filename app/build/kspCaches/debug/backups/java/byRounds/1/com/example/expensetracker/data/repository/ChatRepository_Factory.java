package com.example.expensetracker.data.repository;

import com.example.expensetracker.data.dao.ChatMessageDao;
import com.example.expensetracker.data.service.GeminiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class ChatRepository_Factory implements Factory<ChatRepository> {
  private final Provider<ChatMessageDao> chatMessageDaoProvider;

  private final Provider<GeminiService> geminiServiceProvider;

  public ChatRepository_Factory(Provider<ChatMessageDao> chatMessageDaoProvider,
      Provider<GeminiService> geminiServiceProvider) {
    this.chatMessageDaoProvider = chatMessageDaoProvider;
    this.geminiServiceProvider = geminiServiceProvider;
  }

  @Override
  public ChatRepository get() {
    return newInstance(chatMessageDaoProvider.get(), geminiServiceProvider.get());
  }

  public static ChatRepository_Factory create(Provider<ChatMessageDao> chatMessageDaoProvider,
      Provider<GeminiService> geminiServiceProvider) {
    return new ChatRepository_Factory(chatMessageDaoProvider, geminiServiceProvider);
  }

  public static ChatRepository newInstance(ChatMessageDao chatMessageDao,
      GeminiService geminiService) {
    return new ChatRepository(chatMessageDao, geminiService);
  }
}
