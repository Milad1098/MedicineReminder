package ir.milad.medicinereminder.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.milad.medicinereminder.data.Repo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Boot, clock/timezone change, app update, exact-alarm permission granted → reschedule everything. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { Repo.rescheduleAll(context) } finally { result.finish() }
        }
    }
}
