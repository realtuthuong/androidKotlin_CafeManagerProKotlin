package com.pro.shopfee.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pro.shopfee.model.Drink

@Database(entities = [Drink::class], version = 1)
abstract class DrinkDatabase : RoomDatabase() {
    abstract fun drinkDAO(): DrinkDAO?

    companion object {
        private const val DATABASE_NAME = "drink.db"
        private var instance: DrinkDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): DrinkDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrinkDatabase::class.java, DATABASE_NAME
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return instance
        }
    }
}