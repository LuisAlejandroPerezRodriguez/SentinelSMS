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
import androidx.recyclerview.widget.DiffUtil


class SmsAdapter(private var smsList: MutableList<SmsMessage>, private val myNumber: String) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBody: TextView = itemView.findViewById(R.id.tvBody)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = smsList[position]

        holder.tvBody.text = sms.body
        holder.tvDate.text = DateFormat.getDateTimeInstance().format(Date(sms.date))

        val params = holder.messageContainer.layoutParams as LinearLayout.LayoutParams
        if (sms.isSent) {
            params.gravity = Gravity.END
            holder.messageContainer.setBackgroundResource(R.drawable.message_background_sent)
        } else {
            params.gravity = Gravity.START
            holder.messageContainer.setBackgroundResource(R.drawable.message_background_received)
        }
        holder.messageContainer.layoutParams = params
    }


    override fun getItemCount() = smsList.size

    fun addMessage(message: SmsMessage) {
        smsList.add(message)
        notifyItemInserted(smsList.size - 1)
    }

    fun updateMessages(newMessages: List<SmsMessage>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = smsList.size
            override fun getNewListSize(): Int = newMessages.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return smsList[oldItemPosition].id == newMessages[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return smsList[oldItemPosition] == newMessages[newItemPosition]
            }
        })

        smsList.clear()
        smsList.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }
}


