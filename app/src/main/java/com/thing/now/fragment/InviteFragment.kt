package com.thing.now.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thing.now.NowHelper

import com.thing.now.R
import kotlinx.android.synthetic.main.fragment_invite.*
import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.support.v4.content.ContextCompat.getSystemService
import android.widget.Toast

class InviteFragment : Fragment() {

    fun output(link: String?) {
        if (isVisible) {
            inviteLink.text = if (link == null) context!!.getString(R.string.invite_error_msg) else link

            if (link != null) {
                inviteLink.setOnClickListener {
                    val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("invite key", link)
                    clipboard?.primaryClip = clip
                    Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NowHelper.createConnection().addOnSuccessListener {
            output(it.id)

        }.addOnFailureListener {
            output(null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_invite, container, false)
    }


}
