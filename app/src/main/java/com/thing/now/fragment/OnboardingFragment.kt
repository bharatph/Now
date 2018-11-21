package com.thing.now.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thing.now.R
import com.thing.now.listener.OnSwitchListener
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnboardingFragment : Fragment() {
    private var listener: OnSwitchListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSwitchListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnSwitchListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentManager?.beginTransaction()
            ?.replace(R.id.bottomFrameLayout, AcknowledgementsFragment().apply {
                enterTransition = Fade(Fade.MODE_IN)
            })?.commit()
        onboardingRootView.setOnClickListener {
            listener?.onSwitch(R.id.bottomFrameLayout) //FIXME?
        }
    }
}
