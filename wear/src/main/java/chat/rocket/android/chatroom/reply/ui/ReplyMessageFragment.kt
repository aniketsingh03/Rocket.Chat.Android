package chat.rocket.android.chatroom.reply.ui

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.wear.activity.ConfirmationActivity
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.reply.presentation.ReplyMessagePresenter
import chat.rocket.android.chatroom.reply.presentation.ReplyMessageView
import chat.rocket.android.util.showToast
import chat.rocket.android.util.ui
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_reply_message.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

fun newInstance(chatRoomId: String, replyText: String): Fragment {
    return ReplyMessageFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_REPLY_TEXT, replyText)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_REPLY_TEXT = "reply_text"

class ReplyMessageFragment : Fragment(), ReplyMessageView {

    @Inject
    lateinit var presenter: ReplyMessagePresenter
    @Inject
    lateinit var navigator: ChatRoomNavigator
    private lateinit var chatRoomId: String
    private lateinit var replyText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        getRoomIdFromBundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater!!.inflate(R.layout.fragment_reply_message, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reply_message_edit_text.setText(replyText)

        with(action_message_send) {
            setOnClickListener {
                if (reply_message_edit_text.text.isNotBlank()) {
                    presenter.sendMessage(chatRoomId, reply_message_edit_text.text.toString())
                }
            }
        }
    }

    override fun messageSentSuccessfully() {
        hideLoading()
        showConfirmationAnimation(
            ConfirmationActivity.SUCCESS_ANIMATION, getString(R.string.msg_sent)
        )
        launch {
            delay(1000L)
            navigator.removeReplyMessageFragment()
        }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() {
        showConfirmationAnimation(
            ConfirmationActivity.FAILURE_ANIMATION,
            getString(R.string.msg_sending_failed)
        )
    }

    override fun showLoading() {
        ui {
            view_loading.isVisible = true
            changeViewVisibility(false, 0.5f)
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
            changeViewVisibility(true, 1.0f)
        }
    }

    private fun showConfirmationAnimation(animationType: Int, confirmationMessage: String) {
        val confirmationIntent = Intent(context, ConfirmationActivity::class.java)
        confirmationIntent.putExtra(
            ConfirmationActivity.EXTRA_ANIMATION_TYPE,
            animationType
        )
        confirmationIntent.putExtra(
            ConfirmationActivity.EXTRA_MESSAGE,
            confirmationMessage
        )
        startActivity(confirmationIntent)
    }

    private fun changeViewVisibility(visibility: Boolean, alpha: Float) {
        reply_message_edit_text.isEnabled = visibility
        action_message_send.isEnabled = visibility
        reply_message_edit_text.alpha = alpha
        action_message_send.alpha = alpha
    }

    private fun getRoomIdFromBundle() {
        val bundle = arguments
        if (arguments != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            replyText = bundle.getString(BUNDLE_REPLY_TEXT)
        }
    }
}