package com.pro.shopfee.di

import android.content.Context
import com.pro.shopfee.MyApplication
import com.pro.shopfee.database.DrinkDAO
import com.pro.shopfee.database.DrinkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMyApplication(@ApplicationContext context: Context): MyApplication {
        return context.applicationContext as MyApplication
    }

    @Provides
    @Singleton
    fun provideDrinkDatabase(@ApplicationContext context: Context): DrinkDatabase {
        // Temporary: reuse existing singleton which allows main-thread queries.
        // We'll refactor DAO to suspend/Flow and remove main-thread access in next step.
        return requireNotNull(DrinkDatabase.getInstance(context))
    }

    @Provides
    fun provideDrinkDao(db: DrinkDatabase): DrinkDAO {
        return requireNotNull(db.drinkDAO())
    }
}
