package com.thing.now.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.thing.now.R
import kotlinx.android.synthetic.main.fragment_acknowledgements.*


class AcknowledgementsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_acknowledgements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resources = context?.resources ?: return

        for (name in resources.getStringArray(R.array.osl_names)) {
            oslNameContainer.addView(TextView(context).apply { text = name })
        }

        for (iconAck in resources.getStringArray(R.array.icon_names)) {
            iconAckContainer.addView(TextView(context).apply { text = iconAck })
        }
    }
}
