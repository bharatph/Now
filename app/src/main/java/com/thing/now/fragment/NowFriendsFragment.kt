package com.thing.now.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.thing.now.MainActivity
import com.thing.now.helper.ConnectionHelper

import com.thing.now.R
import com.thing.now.adapter.NowFriendsAdapter
import com.thing.now.helper.NowHelper
import com.thing.now.model.User
import kotlinx.android.synthetic.main.fragment_now_friends.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NowFriendsFragment : BaseFragment(), View.OnClickListener {

    private var user: User? = null

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sendInviteBtn -> {
                enableInviteBtn(false)
                ConnectionHelper.createInvite(user!!)
            }
            R.id.addFriendBtn -> {
                val clipboard =
                    context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val inviteLink: String? = clipboard.primaryClip?.getItemAt(0)?.text as String?
                if (inviteLink.isNullOrEmpty()) {
                    toast(context!!.getString(R.string.invite_link_empty))
                } else {
                    ConnectionHelper.acceptInvite(user!!, inviteLink)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_now_friends, container, false)
    }


    fun showLoading(b: Boolean) {
        friendsProgressBar.isIndeterminate = b
        friendsProgressBar.visibility = if (b) View.VISIBLE else View.GONE
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoading(true)
        NowHelper.loadExistingUser()
    }

    fun linkToClipboard(link: String?) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("invite key", link)
        clipboard?.primaryClip = clip
    }

    private fun enableInviteBtn(b: Boolean = true) {
        sendInviteBtn.isEnabled = !b
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val FRIENDS = 0
        const val NO_FRIENDS = 1
        const val ERR_FRIENDS = 3
    }

    fun show(i: Int) {
        emptyList.visibility = View.GONE
        friendsError.visibility = View.GONE
        nowFriendsRecyclerView.visibility = View.GONE
        when (i) {
            FRIENDS -> {
                nowFriendsRecyclerView.visibility = View.VISIBLE
            }
            NO_FRIENDS -> {
                emptyList.visibility = View.VISIBLE
            }
            ERR_FRIENDS -> {
                friendsError.visibility = View.VISIBLE
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFriendsLoad(event: FriendsLoadEvent) {
        showLoading(false)
        if (event.friends == null) {
            show(ERR_FRIENDS)
            return
        }
        if (event.friends!!.size == 0) {
            show(NO_FRIENDS)
        } else {
            show(FRIENDS)
            val a = NowFriendsAdapter(context!!, event.friends!!)
            nowFriendsRecyclerView.adapter = a
            nowFriendsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInviteCreate(event: InviteCreateEvent) {
        enableInviteBtn(true)
        if (event.conId == null) {
            toast(getString(R.string.invite_error_msg))
        } else {
            linkToClipboard(event.conId)
            toast(getString(R.string.invite_key_copied))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInviteAccept(event: InviteAcceptEvent) {
        if (event.conId.isNullOrEmpty()) {
            toast(context!!.getString(R.string.invite_link_invalid))
            return
        } else {
            toast(context!!.getString(R.string.invite_link_user_added))
            ConnectionHelper.loadFriends(user!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserLoad(event: MainActivity.OnUserLoadEvent) {
        user = event.user
        if (user == null) {
            showLoading(false)
            show(ERR_FRIENDS)
            return
        }
        sendInviteBtn.setOnClickListener(this)
        addFriendBtn.setOnClickListener(this)
        ConnectionHelper.loadFriends(user!!)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserUpdate(event: MainActivity.OnUserUpdateEvent) {
        if (event.user == null) return
        user = event.user
    }

    data class InviteAcceptEvent(var conId: String?)
    data class FriendsLoadEvent(var friends: List<User>?)
    data class InviteCreateEvent(var conId: String?)
}
