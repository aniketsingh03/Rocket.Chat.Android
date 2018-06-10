package chat.rocket.android.util

import android.app.Activity
import android.app.Fragment
import android.os.Looper
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


inline fun Fragment.ui(crossinline block: (activity: Activity) -> Unit): Job? {
    // Checking first for activity and view saves us from some synchronyzed and thread local checks
    if (activity != null && view != null) {
        // If we already are running on the Main Thread (UI Thread), just go ahead and execute the block
        return if (Looper.getMainLooper() == Looper.myLooper()) {
            block(activity!!)
            null
        } else {
            // Launch a Job on the UI context and check again if the activity and view are still valid
            launch(UI) {
                if (activity != null && view != null) {
                    block(activity!!)
                }
            }
        }
    }

    return null
}