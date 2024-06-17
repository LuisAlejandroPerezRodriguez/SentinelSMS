package com.pucmm.sentinelsms.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.sentinelsms.ChatActivity
import com.pucmm.sentinelsms.R
import java.text.DateFormat
import java.util.Date

class ConversationAdapter(private val conversations: List<Conversation>) :
    RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    class ConversationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvContactName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvLatestMessage: TextView = itemView.findViewById(R.id.tvLatestMessage)
        val tvLatestMessageDate: TextView = itemView.findViewById(R.id.tvLatestMessageDate)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.tvContactName.text = conversation.contactName

        // Display only the latest message
        val latestMessage = conversation.messages.lastOrNull()
        latestMessage?.let {
            holder.tvLatestMessage.text = it.body
            holder.tvLatestMessageDate.text = DateFormat.getDateTimeInstance().format(Date(it.date))
        }

        // Set click listener to open ChatActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("contactNumber", conversation.messages[0].address)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = conversations.size
}