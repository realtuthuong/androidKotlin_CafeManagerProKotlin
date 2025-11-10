package com.pro.shopfee.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.pro.shopfee.MyApplication
import com.pro.shopfee.R
import com.pro.shopfee.model.Rating
import com.pro.shopfee.model.RatingReview
import com.pro.shopfee.utils.Constant
import com.pro.shopfee.utils.GlobalFunction.encodeEmailUser
import com.pro.shopfee.utils.Utils
import kotlin.collections.set

class RatingReviewActivity : BaseActivity() {

    private var ratingBar: RatingBar? = null
    private var edtReview: EditText? = null
    private var tvSendReview: TextView? = null
    private var ratingReview: RatingReview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating_review)
        loadDataIntent()
        initToolbar()
        initUi()
        initListener()
    }

    private fun loadDataIntent() {
        val bundle = intent.extras ?: return
        ratingReview = bundle[Constant.RATING_REVIEW_OBJECT] as RatingReview?
    }

    private fun initUi() {
        ratingBar = findViewById(R.id.ratingbar)
        ratingBar?.rating = 5f
        edtReview = findViewById(R.id.edt_review)
        tvSendReview = findViewById(R.id.tv_send_review)
        val tvMessageReview = findViewById<TextView>(R.id.tv_message_review)
        if (RatingReview.TYPE_RATING_REVIEW_DRINK == ratingReview!!.type) {
            tvMessageReview.text = getString(R.string.label_rating_review_drink)
        } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview!!.type) {
            tvMessageReview.text = getString(R.string.label_rating_review_order)
        }
    }

    private fun initToolbar() {
        val imgToolbarBack = findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { finish() }
        tvToolbarTitle.text = getString(R.string.ratings_and_reviews)
    }

    private fun initListener() {
        tvSendReview!!.setOnClickListener {
            val rate = ratingBar!!.rating
            val review = edtReview!!.text.toString().trim { it <= ' ' }
            val rating = Rating(review, rate.toString().toDouble())
            if (RatingReview.TYPE_RATING_REVIEW_DRINK == ratingReview!!.type) {
                sendRatingDrink(rating)
            } else if (RatingReview.TYPE_RATING_REVIEW_ORDER == ratingReview!!.type) {
                sendRatingOrder(rating)
            }
        }
    }

    private fun sendRatingDrink(rating: Rating) {
        MyApplication[this].getRatingDrinkDatabaseReference(ratingReview!!.id)
            ?.child(encodeEmailUser().toString())
            ?.setValue(rating) { _: DatabaseError?, _: DatabaseReference? ->
                showToastMessage(getString(R.string.msg_send_review_success))
                ratingBar!!.rating = 5f
                edtReview!!.setText("")
                Utils.hideSoftKeyboard(this@RatingReviewActivity)
            }
    }

    private fun sendRatingOrder(rating: Rating) {
        val map: MutableMap<String, Any?> = HashMap()
        map["rate"] = rating.rate
        map["review"] = rating.review
        MyApplication[this].getOrderDatabaseReference()
            ?.child(ratingReview!!.id)
            ?.updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                showToastMessage(getString(R.string.msg_send_review_success))
                ratingBar!!.rating = 5f
                edtReview!!.setText("")
                Utils.hideSoftKeyboard(this@RatingReviewActivity)
            }
    }
}