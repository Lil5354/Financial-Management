package com.example.expensetracker.di;

import com.example.expensetracker.data.service.GeminiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ServiceModule_ProvideGeminiServiceFactory implements Factory<GeminiService> {
  @Override
  public GeminiService get() {
    return provideGeminiService();
  }

  public static ServiceModule_ProvideGeminiServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GeminiService provideGeminiService() {
    return Preconditions.checkNotNullFromProvides(ServiceModule.INSTANCE.provideGeminiService());
  }

  private static final class InstanceHolder {
    private static final ServiceModule_ProvideGeminiServiceFactory INSTANCE = new ServiceModule_ProvideGeminiServiceFactory();
  }
}
