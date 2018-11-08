package com.thing.now.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thing.now.NowHelper

import com.thing.now.R
import com.thing.now.model.User
import kotlinx.android.synthetic.main.fragment_add_user.*


class AddUserFragment : Fragment() {
    private var listener: OnUserAddListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        NowHelper.completeConnection(addUserInput.text.toString())?.addOnSuccessListener {
            
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_user, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUserAddListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnUserAddListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnUserAddListener {
        fun onUserAdd(user: User)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddUserFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
