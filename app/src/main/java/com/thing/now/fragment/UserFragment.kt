package com.thing.now.fragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.thing.now.NowHelper

import com.thing.now.R
import com.thing.now.listener.OnSwitchListener
import com.thing.now.model.User
import kotlinx.android.synthetic.main.circle_timer.*
import kotlinx.android.synthetic.main.header.*
import java.util.*
import kotlin.concurrent.timerTask

private const val ARG_UID = "uid"

class UserFragment : Fragment(), View.OnClickListener {
    private var uid: String? = null
    private var listener: OnSwitchListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }

    fun load(shouldLoadUser: Boolean = true) {
//        progressBar.isIndeterminate = shouldLoadUser
//        progressBar.visibility = if (shouldLoadUser) View.VISIBLE else View.GONE

        if (shouldLoadUser) {
            userStatus.text = getString(R.string.loading_main)
        } else {
            userStatus.text = NowHelper.user?.task?.name ?: getString(R.string.task_default_name)
        }
    }

    private var timer: Timer? = null
    private fun startTimer(elapsed: Long = 0) {
        var i = elapsed
        timer = Timer().apply {
            scheduleAtFixedRate(timerTask {
                Handler(context?.mainLooper).post {
                    nowTimer.text = "${++i / 60} : ${i % 60}"
                }
            }, 0, 1000)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
    }


    fun startTask() {
        load(true)
        val editText = EditText(context!!).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = context.getString(R.string.task_name_hint)
        }
        AlertDialog.Builder(context!!)
            .setTitle(getString(R.string.task_start))
            .setPositiveButton(getString(R.string.start_timer)) { _, _ ->
                var str = editText.text.toString()
                if (str.isEmpty()) {
                    editText.error = getString(R.string.task_invalid)
                    str = getString(R.string.task_default_name)
                }
                val callback = NowHelper.startTask(str)
                if (callback == null) {
                    toast(getString(R.string.task_unknown_error))
                    load(false)
                    return@setPositiveButton
                }
                callback.addOnSuccessListener {
                    //                    load(false)
                    resumeTask()
                }.addOnFailureListener {
                    //                    load(false)
                    toast("starting task failed")
                }
            }
            .setView(editText).show()
    }

    private fun resumeTask() {
        val task = NowHelper.user?.task ?: return
        startTimer((Date().time - task.startedOn.time).div(1000))
        userStatus.text = task.name
    }


    private fun endTask() {
        AlertDialog.Builder(context!!).setTitle(getString(R.string.confirm_task_end)).setPositiveButton("Ok") { _, _ ->
            val callback = NowHelper.endTask()
            if (callback == null) {
                toast(getString(R.string.task_stop_error))
                return@setPositiveButton
            }
            callback.addOnSuccessListener {
                stopTimer()
                nowTimer.text = getString(R.string.start_timer)
                userStatus.text = getString(R.string.user_status_idle)

            }.addOnFailureListener {
                toast(getString(R.string.task_network_error))
            }
        }.show()
    }

    private fun settleTasks() {
        val elapsedTime = NowHelper.timeElapsed()
        if (elapsedTime != null) {
            resumeTask()
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.appInfoBtn -> {
                listener?.onSwitch(R.id.appInfoBtn)
            }
            R.id.userName -> {
                val editText = EditText(context!!).apply {
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                AlertDialog.Builder(context!!)
                    .setTitle("Change User name")
                    .setPositiveButton("Ok") { _, _ ->
                        val str = editText.text.toString()
                        if (str.isEmpty()) {
                            editText.error = "Invalid user name"
                            return@setPositiveButton
                        }
                        NowHelper.user?.name = str
                        NowHelper.updateUser().addOnSuccessListener {
                            toast("User name updated successfully")
                            userName.text = str
                        }.addOnFailureListener {
                            toast("User name failed to update")
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }
                    .setView(editText).show()
            }
            R.id.circleTimer -> {
                if (NowHelper.timeElapsed() == null) startTask() else endTask()
            }
        }
    }


    private fun settleUser(user: User) {
        //dp
//        if (user.dp.isNotBlank()) {
//            Picasso.get().load(user.dp)
//                .error(R.drawable.ic_user)
//                .placeholder(R.drawable.ic_user)
//        }
        //name
        userName.text = if (user.name.isEmpty()) getString(R.string.default_user_name) else user.name
        userStatus.text = if (user.status.isEmpty()) getString(R.string.user_status_idle) else user.status
        load(false)
        settleTasks()
    }


    fun show(
        message: String,
        listener: DialogInterface.OnClickListener? = null,
        dismissListener: DialogInterface.OnDismissListener? = null
    ) {
        AlertDialog.Builder(context!!).setPositiveButton("Ok", listener)
            .setOnDismissListener(dismissListener)
            .setMessage(message).create().show()
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    private fun settleFriends() {
        fragmentManager?.beginTransaction()
            ?.replace(R.id.bottomFrameLayout, NowFriendsFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })?.commit()
    }

    private fun loadUser() {
        var status = NowHelper.loadUser(FirebaseAuth.getInstance().currentUser!!.uid)
            ?.addOnSuccessListener {
                val user = it.toObject(User::class.java)
                if (user == null) {
                    load()
                    show(getString(R.string.app_fatal_error),
                        DialogInterface.OnClickListener { dialog, which -> fragmentManager?.popBackStack() })
                    return@addOnSuccessListener
                }
                settleUser(NowHelper.user!!)
                settleFriends()

                //register clicks
                userName.setOnClickListener(this)
                circleTimer.setOnClickListener(this)
                appInfoBtn.setOnClickListener(this)
            }?.addOnFailureListener {
                load()
                show(getString(R.string.app_unknown_error))
            }
        if (status == null) {
            load()
            show(getString(R.string.app_network_unreachable_error),
                DialogInterface.OnClickListener { _, _ -> fragmentManager?.popBackStack() })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSwitchListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnSwitchListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUser()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, param1)
                }
            }
    }
}
