package com.thing.now.fragment

import android.support.v4.app.Fragment
import org.greenrobot.eventbus.EventBus

open class BaseFragment : Fragment() {
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}