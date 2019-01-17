package com.thing.now.helper

import com.google.android.gms.tasks.Task
import com.thing.now.fragment.UserFragment
import com.thing.now.model.User
import org.greenrobot.eventbus.EventBus
import java.util.*

object TaskHelper {
    init {

    }

    //null indicates no task is running
    fun timeElapsed(user: User): Long? {
        return when {
            user.task == null -> null
            user.task == null -> return null
            user.task!!.endedOn != null -> null
            Date().time - user.task!!.startedOn.time > 3600000 -> null
            else -> Date().time - user.task!!.startedOn.time
        }
    }

    fun startTask(user: User, taskTitle: String) {
        user.task = com.thing.now.model.Task(taskTitle, Date(), null)
        NowHelper.updateUser(user)
    }

    fun endTask(user: User) {
        if (NowHelper.getUid() == null)
            return EventBus.getDefault().post(UserFragment.TaskEndEvent(null))
        val endDate = Date()
        if (user.task == null)
            EventBus.getDefault().post(UserFragment.TaskEndEvent(null))
        else {
            user.task?.endedOn = endDate
            NowHelper.updateUser(user)
        }
    }
}