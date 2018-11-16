package com.thing.now.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Fade
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thing.now.R
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnboardingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingRootView.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }
}
