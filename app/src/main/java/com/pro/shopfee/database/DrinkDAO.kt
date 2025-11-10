package com.pro.shopfee.database

import androidx.room.*
import com.pro.shopfee.model.Drink

@Dao
interface DrinkDAO {
    @Insert
    fun insertDrink(drink: Drink?)

    @get:Query("SELECT * FROM drink")
    val listDrinkCart: MutableList<Drink>?

    @Query("SELECT * FROM drink WHERE id=:id")
    fun checkDrinkInCart(id: Long): List<Drink>?

    @Delete
    fun deleteDrink(drink: Drink?)

    @Update
    fun updateDrink(drink: Drink?)

    @Query("DELETE from drink")
    fun deleteAllDrink()
}