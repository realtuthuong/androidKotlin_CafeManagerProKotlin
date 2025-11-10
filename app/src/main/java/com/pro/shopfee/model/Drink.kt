package com.pro.shopfee.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import java.io.Serializable
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Entity(tableName = "drink")
class Drink : Serializable {
    @PrimaryKey
    var id: Long = 0
    var name: String? = null
    var description: String? = null
    var price = 0
    var image: String? = null
    var banner: String? = null
    var category_id: Long = 0
    var category_name: String? = null
    var sale = 0
    @get:PropertyName("featured")
    @set:PropertyName("featured")
    var isFeatured = false

    @Ignore
    var rating: HashMap<String, Rating>? = null
    var count = 0
    var totalPrice = 0
    var priceOneDrink = 0
    var option: String? = null
    var variant: String? = null
    var size: String? = null
    var sugar: String? = null
    var ice: String? = null
    var toppingIds: String? = null
    var note: String? = null
    val realPrice: Int
        get() = if (sale <= 0) {
            price
        } else price - price * sale / 100
    val countReviews: Int
        get() = if (rating == null || rating!!.isEmpty()) 0 else rating!!.size
    val rate: Double
        get() {
            if (rating == null || rating!!.isEmpty()) return 0.0
            var sum = 0.0
            for (ratingEntity in rating!!.values) {
                sum += ratingEntity.rate
            }
            val symbols = DecimalFormatSymbols()
            symbols.decimalSeparator = '.'
            val formatter = DecimalFormat("#.#")
            formatter.decimalFormatSymbols = symbols
            return formatter.format(sum / rating!!.size).toDouble()
        }
}