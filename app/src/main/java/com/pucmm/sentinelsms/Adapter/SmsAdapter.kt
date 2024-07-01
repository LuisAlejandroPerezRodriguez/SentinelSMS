package com.pucmm.sentinelsms.Adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.sentinelsms.R
import com.pucmm.sentinelsms.SmsMessage
import java.text.DateFormat
import java.util.*
import android.widget.LinearLayout


class SmsAdapter(private var smsList: MutableList<SmsMessage>, private val myNumber: String) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivContactIcon: ImageView = itemView.findViewById(R.id.ivContactIcon)
        val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        val tvBody: TextView = itemView.findViewById(R.id.tvBody)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val reversedPosition = smsList.size - 1 - position
        val sms = smsList[reversedPosition]

        if (sms.address == myNumber) {
            holder.tvSender.text = "Me"
            holder.tvBody.setBackgroundResource(R.drawable.message_background_sent)
            holder.messageContainer.gravity = Gravity.END
        } else {
            holder.tvSender.text = sms.address
            holder.tvBody.setBackgroundResource(R.drawable.message_background_received)
            holder.messageContainer.gravity = Gravity.START
        }

        holder.tvBody.text = sms.body
        holder.tvDate.text = DateFormat.getDateTimeInstance().format(Date(sms.date))
        holder.ivContactIcon.setImageResource(R.drawable.ic_contact_default)
    }

    override fun getItemCount() = smsList.size

    fun addMessage(message: SmsMessage) {
        smsList.add(0, message) // Add to the beginning for reversed display
        notifyItemInserted(0)
    }

    fun updateMessages(newMessages: MutableList<SmsMessage>) {
        smsList = newMessages
        notifyDataSetChanged()
    }
}


