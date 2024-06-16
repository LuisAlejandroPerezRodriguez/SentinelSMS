package com.pucmm.sentinelsms.Adapter

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

class SmsAdapter(private val smsList: List<SmsMessage>) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivContactIcon: ImageView = itemView.findViewById(R.id.ivContactIcon)
        val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        val tvBody: TextView = itemView.findViewById(R.id.tvBody)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    // Create a new ViewHolder object
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {

        // Reverse the position to display messages from bottom to top
        val reversedPosition =smsList.size - 1 - position

        val sms = smsList[reversedPosition]
        holder.tvSender.text = sms.address
        holder.tvBody.text = sms.body
        holder.tvDate.text = DateFormat.getDateTimeInstance().format(Date(sms.date))

        // Optionally, you can customize the contact icon based on the sender or other criteria
        holder.ivContactIcon.setImageResource(R.drawable.ic_contact_default)
    }

    override fun getItemCount() = smsList.size // Return the number of items in the list
}
