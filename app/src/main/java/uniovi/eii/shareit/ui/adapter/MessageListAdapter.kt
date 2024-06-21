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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MessageListAdapter(
    private var messageList: List<ChatMessage> = emptyList(),
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SENT = 0
        const val SENT_WITH_DATE = 1
        const val RECEIVED = 2
        const val RECEIVED_WITH_DATE = 3
        const val RECEIVED_SAME_SENDER = 4
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].senderId == "0") {
            return if (position == 0 ||
                !messageList[position].timestamp.truncatedTo(ChronoUnit.DAYS).equals(messageList[position-1].timestamp.truncatedTo(ChronoUnit.DAYS)))
                SENT_WITH_DATE
            else SENT
        }
        if (position == 0 ||
            !messageList[position].timestamp.truncatedTo(ChronoUnit.DAYS).equals(messageList[position-1].timestamp.truncatedTo(ChronoUnit.DAYS))) return RECEIVED_WITH_DATE
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

    inner class ReceivedMessageViewHolder(private val binding: LineRecyclerViewMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun assignValuesToComponents(chatMessage: ChatMessage, showSender: Boolean = true, showDate: Boolean = false) {
            binding.message.text = chatMessage.message
            binding.sender.text = chatMessage.senderName
            if (!showSender) binding.sender.visibility = View.GONE
            binding.date.text = chatMessage.timestamp.format(DateTimeFormatter.ofPattern("d MMM uuuu"))
            if (!showDate) binding.date.visibility = View.GONE
            binding.time.text = chatMessage.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    inner class SentMessageViewHolder(private val binding: LineRecyclerViewMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun assignValuesToComponents(chatMessage: ChatMessage, showDate: Boolean = false) {
            binding.message.text = chatMessage.message
            binding.date.text = chatMessage.timestamp.format(DateTimeFormatter.ofPattern("d MMM uuuu"))
            if (!showDate) binding.date.visibility = View.GONE
            binding.time.text = chatMessage.timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }
}