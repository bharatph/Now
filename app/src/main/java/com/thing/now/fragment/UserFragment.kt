package com.thing.now.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.thing.now.MainActivity
import com.thing.now.MainActivity.Companion.PROGRESS_HIDE
import com.thing.now.MainActivity.Companion.PROGRESS_SHOW
import com.thing.now.helper.NowHelper

import com.thing.now.R
import com.thing.now.helper.ConnectionHelper
import com.thing.now.helper.TaskHelper
import com.thing.now.model.Task
import com.thing.now.model.User
import kotlinx.android.synthetic.main.circle_timer.*
import kotlinx.android.synthetic.main.header.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.concurrent.timerTask

class UserFragment : BaseFragment(), View.OnClickListener {
    private var listener: OnUserFragmentInteractionListener? = null
    private var user: User? = null

    private var timer: Timer? = null
    private fun startTimer(elapsed: Long = 0) {
        var i = elapsed
        timer = Timer().apply {
            scheduleAtFixedRate(timerTask {
                Handler(context?.mainLooper).post {
                    nowTimer?.text = String.format(getString(R.string.time_format), ++i / 60, i % 60)
                }
            }, 0, 1000)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        nowTimer.text = getString(R.string.start_timer)
    }

    fun showLoading(shouldLoad: Boolean = true) {
        if (shouldLoad)
            listener?.onUserFragmentInteraction(PROGRESS_SHOW)
        else listener?.onUserFragmentInteraction(PROGRESS_HIDE)
    }

    private fun resumeTask(user: User) {
        val task = user.task ?: return
        startTimer((Date().time - task.startedOn.time).div(1000))
        userStatus.text = task.name
    }


    private fun endTask() {
        showLoading(true)
        AlertDialog.Builder(context!!).setTitle(getString(R.string.confirm_task_end)).setPositiveButton("Ok") { _, _ ->
            TaskHelper.endTask(user!!)
        }.setOnCancelListener {
            showLoading(false)
        }.show()
    }

    private fun startTask() {
        showLoading(true)
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
                TaskHelper.startTask(user!!, str)
            }.setOnCancelListener {
                showLoading(false)
            }
            .setView(editText).show()
    }

    private fun updateUser() {
        showLoading(true)
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
                if (user == null) {
                    return@setPositiveButton
                }
                NowHelper.updateUser(user!!.apply { name = str })
            }.setOnCancelListener {
                showLoading(false)
            }
            .setView(editText).show()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.appInfoBtn -> {
                listener?.onUserFragmentInteraction(R.id.appInfoBtn)
            }
            R.id.userName -> {
                updateUser()
            }
            R.id.circleTimer -> {
                if (TaskHelper.timeElapsed(user!!) == null) startTask() else endTask()
            }
        }
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
        if (context is OnUserFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnSwitchListener")
        }
    }

    fun startOperation() {
        showLoading(true)
        NowHelper.loadExistingUser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startOperation()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserLoad(event: MainActivity.OnUserLoadEvent) {
        showLoading(false)
        if (event.user == null) {
            AlertDialog.Builder(context!!)
                .setMessage(getString(R.string.user_not_found))
                .setOnCancelListener {
                    startOperation()
                }.create().show()
        } else {
            user = event.user;
            val elapsedTime = TaskHelper.timeElapsed(user!!)
            if (elapsedTime == null) {
                stopTimer()
            } else {
                resumeTask(user!!)
            }
            userName.text =
                    if (event.user?.name!!.isEmpty()) getString(R.string.default_user_name) else event.user!!.name
            userStatus.text =
                    if (event.user!!.status.isEmpty()) getString(R.string.task_default_name) else event.user!!.status

            //register clicks
            circleTimer.setOnClickListener(this)
            userName.setOnClickListener(this)
            appInfoBtn.setOnClickListener(this)

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserUpdate(event: MainActivity.OnUserUpdateEvent) {
        onUserLoad(MainActivity.OnUserLoadEvent(event.user))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskStart(startEvent: TaskStartEvent) {
        showLoading(false)
        if (startEvent.task == null) {
            toast(getString(R.string.task_unknown_error))
            showLoading(false)
        } else {
            resumeTask(user!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskEnd(endEvent: TaskEndEvent) {
        showLoading(false)
        if (endEvent.task == null) {
            toast(getString(R.string.task_network_error))
        } else {
            stopTimer()
            nowTimer.text = getString(R.string.start_timer)
            userStatus.text = getString(R.string.task_default_name)
        }
    }

    data class TaskStartEvent(var task: Task?)
    data class TaskEndEvent(var task: Task?)

    interface OnUserFragmentInteractionListener {
        fun onUserFragmentInteraction(i: Int)
    }
}
