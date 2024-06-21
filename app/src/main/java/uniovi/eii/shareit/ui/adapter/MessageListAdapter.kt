package uniovi.eii.shareit.ui.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.LineRecyclerViewMessageReceivedBinding
import uniovi.eii.shareit.databinding.LineRecyclerViewMessageSentBinding
import uniovi.eii.shareit.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Locale

class MessageListAdapter(
    private var messageList: List<ChatMessage> = emptyList(),
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENT = 0
    private val RECEIVED = 1
    private val RECEIVED_SAME_SENDER = 2

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].senderId == "0") return SENT
        if (position == 0) return RECEIVED
        return if (messageList[position].senderName == messageList[position - 1].senderName) RECEIVED_SAME_SENDER else RECEIVED
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RECEIVED -> ReceivedMessageViewHolder(
                LineRecyclerViewMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            RECEIVED_SAME_SENDER -> ReceivedMessageViewHolder(
                LineRecyclerViewMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            SENT -> SentMessageViewHolder(
                LineRecyclerViewMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw IllegalStateException("Invalid viewType for the message")
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        Log.i("Lista", "Visualiza elemento: $message")
        when (holder.itemViewType) {
            RECEIVED -> (holder as ReceivedMessageViewHolder).assignValuesToComponents(message)
            RECEIVED_SAME_SENDER -> (holder as ReceivedMessageViewHolder).assignValuesToComponents(message, false)
            SENT -> (holder as SentMessageViewHolder).assignValuesToComponents(message)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(messageList: List<ChatMessage>) {
        this.messageList = messageList
        notifyDataSetChanged()
    }

    inner class ReceivedMessageViewHolder(private val binding: LineRecyclerViewMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun assignValuesToComponents(chatMessage: ChatMessage, showSender: Boolean = true) {
            binding.message.text = chatMessage.message
            binding.sender.text = chatMessage.senderName
            if (!showSender) binding.sender.visibility = View.GONE
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.isLenient = false

            binding.time.text = sdf.format(chatMessage.timestamp)
        }
    }

    inner class SentMessageViewHolder(private val binding: LineRecyclerViewMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun assignValuesToComponents(chatMessage: ChatMessage) {
            binding.message.text = chatMessage.message
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.time.text = sdf.format(chatMessage.timestamp)
        }
    }
}