package com.thing.now.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.design.drawable.DrawableUtils
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.util.DataUtils
import com.squareup.picasso.Picasso
import com.thing.now.FirestoreList
import com.thing.now.R
import com.thing.now.model.User
import kotlinx.android.synthetic.main.user_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class NowFriendsAdapter(var context: Context, var firestoreList: FirestoreList<User>) :
    RecyclerView.Adapter<NowFriendsAdapter.NowFriendViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NowFriendViewHolder {
        val viewGroup = LayoutInflater.from(context).inflate(R.layout.user_item, p0, false)
        return NowFriendViewHolder(viewGroup)
    }

    override fun getItemCount(): Int {
        return firestoreList.size
    }

    override fun onBindViewHolder(p0: NowFriendViewHolder, p1: Int) {
        val user = firestoreList.keyAt(p1)
        if (user.dp.isBlank()) {

        } else {
            Picasso.get()
                .load(user.dp)
                .error(R.drawable.ic_user)
                .into(p0.userImage)
        }
        p0.userName.text = user.name
        p0.userStatus.text = user.status
        p0.userNowStatus.setCircleBackgroundColorResource(R.color.colorNotNow)

        if (user.event == null) {
        } else {
            val event = user.event
            p0.userNowTime.text = SimpleDateFormat("hh:mm").format(Date()).toString() + " H"
        }
    }

    class NowFriendViewHolder(itemView: View) : ViewHolder(itemView) {
        var userImage = itemView.userImage!!
        var userName = itemView.userName!!
        var userStatus = itemView.userStatus!!
        var userNowTime = itemView.userNowTime!!
        var userNowStatus = itemView.userNowStatus!!
    }
}