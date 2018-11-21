package com.thing.now.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.thing.now.NowHelper

import com.thing.now.R
import com.thing.now.adapter.NowFriendsAdapter
import kotlinx.android.synthetic.main.fragment_now_friends.*


class NowFriendsFragment : Fragment(), View.OnClickListener {

    private fun onUserResolve(enableBtn: Boolean) {
        loadau(false)
        if (enableBtn) {
            settleFriends()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sendInviteBtn -> {
                starti()
            }
            R.id.addFriendBtn -> {
                startau()
            }
            R.id.sortBtn -> {
                //TODO
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_now_friends, container, false)
    }


    fun loadFriends(shouldLoadFriends: Boolean = true) {
//        friendsProgressBar.isIndeterminate = shouldLoadFriends
//        friendsProgressBar.visibility = if (shouldLoadFriends) View.VISIBLE else View.GONE
    }

    private fun settleFriends() {
        loadFriends()
        NowHelper.friendList {
            loadFriends(false)
            if (it == null) {
                emptyList.visibility = View.VISIBLE
                return@friendList
            }
            emptyList.visibility = View.GONE
            if (isVisible) //FIXME Necessary?
                nowFriendsRecyclerView.adapter = NowFriendsAdapter(context!!, it)
        }
        nowFriendsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendInviteBtn.setOnClickListener(this)
        addFriendBtn.setOnClickListener(this)
        sortBtn.setOnClickListener(this)

        settleFriends()

    }

    //invite

    fun linkToClipboard(link: String?) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("invite key", link)
        clipboard?.primaryClip = clip
    }


    fun starti() {
        loadi()
        NowHelper.createConnection().addOnSuccessListener {
            linkToClipboard(it.id)
            toast(getString(R.string.invite_key_copied))
            loadi(false)
        }.addOnFailureListener {
            toast(getString(R.string.invite_error_msg))
            loadi(false)
        }
    }

    private fun loadi(b:Boolean=true) {
        sendInviteBtn.isEnabled = !b
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    /////////
    // user add

    private fun loadau(b:Boolean=true) {
        addFriendBtn.isEnabled = !b
    }
    fun startau() {
        loadau()
        var clipboard =
            context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        var inviteLink: String? = clipboard.primaryClip?.getItemAt(0)?.text as String?
        if (inviteLink.isNullOrEmpty()) {
            toast(context!!.getString(R.string.invite_link_empty))
            return
        }
        onUserResolve(false)
        var callback = NowHelper.completeConnection(inviteLink)
        if (callback == null) {
            onUserResolve(false)
            toast(context!!.getString(R.string.invite_link_invalid))
            return
        }
        callback.addOnSuccessListener {
            toast(context!!.getString(R.string.invite_link_user_added))
            onUserResolve(true)
        }.addOnFailureListener {
            onUserResolve(false)
            toast(context!!.getString(R.string.invite_link_error))
        }
    }

    ////////////
}
