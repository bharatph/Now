package com.thing.now.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thing.now.NowHelper

import com.thing.now.R
import kotlinx.android.synthetic.main.fragment_user_add.*

class UserAddFragment : Fragment() {
    private var listener: OnUserResolveListener? = null

    fun output(string: String?) {
        userAddMessage.text = string ?: context!!.getString(R.string.user_add_unknow_error)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var clipboard =
            context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        var inviteLink: String? = clipboard.primaryClip?.getItemAt(0)?.text as String?
        if (inviteLink.isNullOrEmpty()) {
            output(context!!.getString(R.string.invite_link_empty))
            return
        }
        listener?.onUserResolve(false)
        var callback = NowHelper.completeConnection(inviteLink)
        if (callback == null) {
            listener?.onUserResolve(true)
            output(context!!.getString(R.string.invite_link_invalid))
            return
        }
        callback.addOnSuccessListener {
            output(context!!.getString(R.string.invite_link_user_added))
            listener?.onUserResolve(true)
        }.addOnFailureListener {
            listener?.onUserResolve(true)
            output(context!!.getString(R.string.invite_link_error))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUserResolveListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnUserResolveListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnUserResolveListener {
        fun onUserResolve(enableBtn: Boolean)
    }
}
