package com.pro.shopfee.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.pro.shopfee.R
import com.pro.shopfee.activity.*
import com.pro.shopfee.utils.GlobalFunction.startActivity
import com.pro.shopfee.viewmodel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModels()

    private var mView: View? = null
    private var layoutFeedback: LinearLayout? = null
    private var layoutContact: LinearLayout? = null
    private var layoutChangePassword: LinearLayout? = null
    private var layoutSignOut: LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_account, container, false)
        initToolbar()
        initUi()
        initListener()
        observeViewModel()
        return mView
    }

    private fun initToolbar() {
        val imgToolbarBack = mView!!.findViewById<ImageView>(R.id.img_toolbar_back)
        val tvToolbarTitle = mView!!.findViewById<TextView>(R.id.tv_toolbar_title)
        imgToolbarBack.setOnClickListener { backToHomeScreen() }
        tvToolbarTitle.text = getString(R.string.nav_account)
    }

    private fun backToHomeScreen() {
        val mainActivity = activity as MainActivity? ?: return
        mainActivity.setMainPage(0)
    }

    private fun initUi() {
        layoutFeedback = mView!!.findViewById(R.id.layout_feedback)
        layoutContact = mView!!.findViewById(R.id.layout_contact)
        layoutChangePassword = mView!!.findViewById(R.id.layout_change_password)
        layoutSignOut = mView!!.findViewById(R.id.layout_sign_out)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                user?.let {
                    val tvUsername = mView?.findViewById<TextView>(R.id.tv_username)
                    tvUsername?.text = it.email
                    val tvRank = mView?.findViewById<TextView>(R.id.tv_rank)
                    tvRank?.text = it.rankName
                }
            }
        }
    }

    private fun initListener() {
        layoutFeedback!!.setOnClickListener {
            startActivity(
                activity!!, FeedbackActivity::class.java
            )
        }
        layoutContact!!.setOnClickListener {
            startActivity(
                activity!!, ContactActivity::class.java
            )
        }
        layoutChangePassword!!.setOnClickListener {
            startActivity(
                activity!!, ChangePasswordActivity::class.java
            )
        }
        layoutSignOut!!.setOnClickListener { onClickSignOut() }
    }

    private fun onClickSignOut() {
        if (activity == null) return
        viewModel.signOut()
        startActivity(activity!!, LoginActivity::class.java)
        activity!!.finishAffinity()
    }
}