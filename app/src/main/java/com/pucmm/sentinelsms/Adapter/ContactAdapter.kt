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

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var filteredContacts = groupContacts(contacts)

    private fun groupContacts(contacts: List<Contact>): List<Any> {
        return contacts.groupBy { it.name.first().toUpperCase() }
            .flatMap { (initial, contactsForInitial) ->
                listOf(initial.toString()) + contactsForInitial
            }
    }

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val phoneTextView: TextView = view.findViewById(R.id.phoneTextView)
        val initialTextView: TextView = view.findViewById(R.id.initialTextView)
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTextView: TextView = view.findViewById(R.id.headerTextView)
    }

    override fun getItemViewType(position: Int): Int {
        return when (filteredContacts[position]) {
            is String -> VIEW_TYPE_HEADER
            is Contact -> VIEW_TYPE_CONTACT
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_CONTACT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.contact_item, parent, false)
                ContactViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = filteredContacts[position] as String
                holder.headerTextView.text = header
            }
            is ContactViewHolder -> {
                val contact = filteredContacts[position] as Contact
                holder.nameTextView.text = contact.name
                holder.phoneTextView.text = contact.phoneNumber
                holder.initialTextView.text = contact.name.first().toString()

                holder.itemView.setOnClickListener {
                    onContactClick(contact)
                }
            }
        }
    }

    override fun getItemCount() = filteredContacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        filteredContacts = groupContacts(newContacts)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    filterResults.values = groupContacts(contacts)
                } else {
                    val filteredList = contacts.filter {
                        it.name.contains(constraint, true) || it.phoneNumber.contains(constraint, true)
                    }
                    filterResults.values = groupContacts(filteredList)
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredContacts = results?.values as? List<Any> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTACT = 1
    }
}
