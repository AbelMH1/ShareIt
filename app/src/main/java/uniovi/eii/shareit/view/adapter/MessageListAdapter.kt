package uniovi.eii.shareit.view.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uniovi.eii.shareit.databinding.LineRecyclerViewMessageReceivedBinding
import uniovi.eii.shareit.databinding.LineRecyclerViewMessageSentBinding
import uniovi.eii.shareit.model.ChatMessage
import uniovi.eii.shareit.utils.areSameDay
import uniovi.eii.shareit.utils.toFormattedChatDateString
import uniovi.eii.shareit.utils.toFormattedChatHourString

class MessageListAdapter(
    private var currentUserId: String = "",
    private var messageList: List<ChatMessage> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SENT = 0
        const val SENT_WITH_DATE = 1
        const val RECEIVED = 2
        const val RECEIVED_WITH_DATE = 3
        const val RECEIVED_SAME_SENDER = 4
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].senderId == currentUserId) {
            return if (position == 0 ||
                !messageList[position].timestamp.areSameDay(messageList[position-1].timestamp))
                SENT_WITH_DATE
            else SENT
        }
        if (position == 0 ||
            !messageList[position].timestamp.areSameDay(messageList[position-1].timestamp)) return RECEIVED_WITH_DATE
        return if (messageList[position].senderName == messageList[position - 1].senderName) RECEIVED_SAME_SENDER else RECEIVED
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType >= RECEIVED)
            ReceivedMessageViewHolder(
                LineRecyclerViewMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else SentMessageViewHolder(
                LineRecyclerViewMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        Log.i("Lista", "Visualiza elemento: $message")
        when (holder.itemViewType) {
            RECEIVED -> (holder as ReceivedMessageViewHolder).assignValuesToComponents(
                message
            )
            RECEIVED_WITH_DATE -> (holder as ReceivedMessageViewHolder).assignValuesToComponents(
                message, showDate = true
            )
            RECEIVED_SAME_SENDER -> (holder as ReceivedMessageViewHolder).assignValuesToComponents(
                message, false
            )
            SENT -> (holder as SentMessageViewHolder).assignValuesToComponents(message)
            SENT_WITH_DATE -> (holder as SentMessageViewHolder).assignValuesToComponents(message, true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(messageList: List<ChatMessage>) {
        this.messageList = messageList
        notifyDataSetChanged()
    }

    fun setCurrentUserId(currentUserId: String) {
        this.currentUserId = currentUserId
    }

    inner class ReceivedMessageViewHolder(private val binding: LineRecyclerViewMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun assignValuesToComponents(chatMessage: ChatMessage, showSender: Boolean = true, showDate: Boolean = false) {
            binding.message.text = chatMessage.message
            binding.sender.text = chatMessage.senderName
            if (!showSender) binding.sender.visibility = View.GONE
            binding.date.text = chatMessage.timestamp.toFormattedChatDateString()
            if (!showDate) binding.date.visibility = View.GONE
            binding.time.text = chatMessage.timestamp.toFormattedChatHourString()
        }
    }

    inner class SentMessageViewHolder(private val binding: LineRecyclerViewMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun assignValuesToComponents(chatMessage: ChatMessage, showDate: Boolean = false) {
            binding.message.text = chatMessage.message
            binding.date.text = chatMessage.timestamp.toFormattedChatDateString()
            if (!showDate) binding.date.visibility = View.GONE
            binding.time.text = chatMessage.timestamp.toFormattedChatHourString()
        }
    }
}