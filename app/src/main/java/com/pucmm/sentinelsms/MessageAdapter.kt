package com.pucmm.sentinelsms

/* This adapter handles the presentation of a list of SMS messages in a RecyclerView.
It creates and binds view holders, which hold references to the views for each item in the list,
and sets the text of these views to the corresponding SMS messages. */

//Imports
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Class Declaration. This is a custom adapter class for a RecyclerView.
class MessageAdapter(private val smsList: List<String>) : // The adapter takes a list of SMS messages (smsList) as a parameter.
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
        // This class extends RecyclerView.Adapter and is parameterized with MessageViewHolder,
        // which is a nested class within MessageAdapter.

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //This is an inner class that holds references to the views for each data item. It extends RecyclerView.ViewHolder.
        val messageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        //A TextView that displays the SMS message. It's found in the itemView using findViewById.
    }
    //This method is called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.messageTextView.text = smsList[position] //Sets the text of messageTextView to the SMS message at the given position.
    }

    override fun getItemCount() = smsList.size // Returns the total number of items in the data set held by the adapter.
}
