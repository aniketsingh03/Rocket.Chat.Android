package chat.rocket.android.profile.presentation

import android.net.Uri
import android.util.Log
import chat.rocket.android.chatroom.domain.UriInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.setAvatar
import chat.rocket.core.internal.rest.updateProfile
import java.io.File
import javax.inject.Inject

class ProfilePresenter @Inject constructor(private val view: ProfileView,
                                           private val strategy: CancelStrategy,
                                           private  val uriInteractor: UriInteractor,
                                           serverInteractor: GetCurrentServerInteractor,
                                           factory: RocketChatClientFactory) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private lateinit var myselfId: String

    fun loadUserProfile() {
        launchUI(strategy) {
            view.showLoading()
            try {
                val myself = retryIO("me") { client.me() }
                myselfId = myself.id
                val avatarUrl = serverUrl.avatarUrl(myself.username!!)
                Log.d("AVATAR", avatarUrl)
                view.showProfile(
                        avatarUrl,
                        myself.name ?: "",
                        myself.username ?: "",
                        myself.emails?.get(0)?.address!!
                )
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun updateUserProfile(email: String, name: String, username: String, avatarUrl: String = "", avatarImage: File?, avatarImageUri: Uri?) {
        launchUI(strategy) {
            view.showLoading()
            try {
                if (avatarUrl.isNotEmpty()) {
                    retryIO { client.setAvatar(avatarUrl) }
                }
                if (avatarImage != null){
                    val mimeType = uriInteractor.getMimeType(avatarImageUri!!)
                    client.setAvatar(avatarImage,mimeType)
                }
                val user = retryIO { client.updateProfile(myselfId, email, name, username) }
                view.showProfileUpdateSuccessfullyMessage()
                loadUserProfile()
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }
}