package com.example.expensetracker.di;

import com.example.expensetracker.data.dao.ChatMessageDao;
import com.example.expensetracker.data.repository.ChatRepository;
import com.example.expensetracker.data.service.GeminiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class RepositoryModule_ProvideChatRepositoryFactory implements Factory<ChatRepository> {
  private final Provider<ChatMessageDao> chatMessageDaoProvider;

  private final Provider<GeminiService> geminiServiceProvider;

  public RepositoryModule_ProvideChatRepositoryFactory(
      Provider<ChatMessageDao> chatMessageDaoProvider,
      Provider<GeminiService> geminiServiceProvider) {
    this.chatMessageDaoProvider = chatMessageDaoProvider;
    this.geminiServiceProvider = geminiServiceProvider;
  }

  @Override
  public ChatRepository get() {
    return provideChatRepository(chatMessageDaoProvider.get(), geminiServiceProvider.get());
  }

  public static RepositoryModule_ProvideChatRepositoryFactory create(
      Provider<ChatMessageDao> chatMessageDaoProvider,
      Provider<GeminiService> geminiServiceProvider) {
    return new RepositoryModule_ProvideChatRepositoryFactory(chatMessageDaoProvider, geminiServiceProvider);
  }

  public static ChatRepository provideChatRepository(ChatMessageDao chatMessageDao,
      GeminiService geminiService) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideChatRepository(chatMessageDao, geminiService));
  }
}
