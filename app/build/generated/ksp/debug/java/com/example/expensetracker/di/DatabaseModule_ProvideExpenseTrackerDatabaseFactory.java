package com.example.expensetracker.di;

import android.content.Context;
import com.example.expensetracker.data.database.ExpenseTrackerDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideExpenseTrackerDatabaseFactory implements Factory<ExpenseTrackerDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideExpenseTrackerDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ExpenseTrackerDatabase get() {
    return provideExpenseTrackerDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideExpenseTrackerDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideExpenseTrackerDatabaseFactory(contextProvider);
  }

  public static ExpenseTrackerDatabase provideExpenseTrackerDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideExpenseTrackerDatabase(context));
  }
}
