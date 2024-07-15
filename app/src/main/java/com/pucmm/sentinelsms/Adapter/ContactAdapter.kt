package com.pucmm.sentinelsms.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import com.pucmm.sentinelsms.Contact
import com.pucmm.sentinelsms.R

class ContactAdapter(private var contacts: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>(), Filterable {

    private var filteredContacts = contacts

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val phoneTextView: TextView = view.findViewById(R.id.phoneTextView)
        val initialTextView: TextView = view.findViewById(R.id.initialTextView)
    }

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        filteredContacts = newContacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = filteredContacts[position]
        holder.nameTextView.text = contact.name
        holder.phoneTextView.text = contact.phoneNumber
        holder.initialTextView.text = contact.name.firstOrNull()?.toString() ?: ""
    }



    override fun getItemCount() = filteredContacts.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    filterResults.values = contacts
                } else {
                    val filteredList = contacts.filter {
                        it.name.contains(constraint, true) || it.phoneNumber.contains(constraint, true)
                    }
                    filterResults.values = filteredList
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredContacts = results?.values as? List<Contact> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}








