package ir.milad.medicinereminder.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import ir.milad.medicinereminder.alarm.AlarmScheduler
import org.json.JSONArray
import org.json.JSONObject

/**
 * Whole-app backup as one JSON file (user picks where: Downloads, Drive, Telegram…).
 * Restore replaces everything; ids are remapped so logs stay attached to their medicine/time.
 */
object Backup {
    private const val FORMAT = 1

    data class Snapshot(val medicines: List<MedicineWithTimes>, val logs: List<DoseLog>)

    fun toJson(s: Snapshot): String = JSONObject()
        .put("app", "ir.milad.medicinereminder").put("format", FORMAT).put("exportedAt", System.currentTimeMillis())
        .put("medicines", JSONArray(s.medicines.map { (m, times) ->
            JSONObject()
                .put("id", m.id).put("name", m.name).put("form", m.form.name).put("color", m.color)
                .put("instruction", m.instruction.name).put("note", m.note).put("frequency", m.frequency.name)
                .put("weekdays", m.weekdays).put("intervalDays", m.intervalDays)
                .put("cycleOn", m.cycleOn).put("cycleOff", m.cycleOff)
                .put("startDate", m.startDate).put("endDate", m.endDate ?: JSONObject.NULL)
                .put("stock", m.stock?.toDouble() ?: JSONObject.NULL).put("refillAt", m.refillAt.toDouble())
                .put("archived", m.archived)
                .put("times", JSONArray(times.map {
                    JSONObject().put("id", it.id).put("minuteOfDay", it.minuteOfDay).put("amount", it.amount.toDouble())
                }))
        }))
        .put("logs", JSONArray(s.logs.map {
            JSONObject().put("medicineId", it.medicineId).put("doseTimeId", it.doseTimeId ?: JSONObject.NULL)
                .put("scheduledAt", it.scheduledAt).put("status", it.status.name)
                .put("actionAt", it.actionAt).put("amount", it.amount.toDouble())
        }))
        .toString(2)

    /** Throws IllegalArgumentException / JSONException on a file that isn't our backup. */
    fun fromJson(text: String): Snapshot {
        val root = JSONObject(text)
        require(root.optString("app") == "ir.milad.medicinereminder") { "not a MedicineReminder backup" }
        require(root.getInt("format") <= FORMAT) { "backup is from a newer app version" }
        val meds = root.getJSONArray("medicines").objects().map { o ->
            MedicineWithTimes(
                Medicine(
                    id = o.getLong("id"), name = o.getString("name"), form = Form.valueOf(o.getString("form")),
                    color = o.getInt("color"), instruction = Instruction.valueOf(o.getString("instruction")),
                    note = o.optString("note"), frequency = Frequency.valueOf(o.getString("frequency")),
                    weekdays = o.getInt("weekdays"), intervalDays = o.getInt("intervalDays"),
                    cycleOn = o.getInt("cycleOn"), cycleOff = o.getInt("cycleOff"),
                    startDate = o.getLong("startDate"), endDate = if (o.isNull("endDate")) null else o.getLong("endDate"),
                    stock = if (o.isNull("stock")) null else o.getDouble("stock").toFloat(),
                    refillAt = o.getDouble("refillAt").toFloat(), archived = o.getBoolean("archived"),
                ),
                o.getJSONArray("times").objects().map {
                    DoseTime(it.getLong("id"), o.getLong("id"), it.getInt("minuteOfDay"), it.getDouble("amount").toFloat())
                },
            )
        }
        val logs = root.getJSONArray("logs").objects().map {
            DoseLog(
                0, it.getLong("medicineId"), if (it.isNull("doseTimeId")) null else it.getLong("doseTimeId"),
                it.getLong("scheduledAt"), LogStatus.valueOf(it.getString("status")),
                it.getLong("actionAt"), it.getDouble("amount").toFloat(),
            )
        }
        return Snapshot(meds, logs)
    }

    suspend fun export(context: Context, uri: Uri) {
        val dao = Repo.dao(context)
        val json = toJson(Snapshot(dao.all(), dao.allLogs()))
        context.contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter().use { it.write(json) }
    }

    /** Returns how many medicines were restored. */
    suspend fun import(context: Context, uri: Uri): Int {
        val text = context.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
        return restore(context, fromJson(text))
    }

    suspend fun restore(context: Context, s: Snapshot): Int {
        val db = AppDb.get(context)
        val dao = db.dao()
        dao.all().forEach { mwt -> mwt.times.forEach { AlarmScheduler.cancel(context, it.id) } }
        db.withTransaction {
            dao.deleteAll()
            val medIds = HashMap<Long, Long>()
            val timeIds = HashMap<Long, Long>()
            s.medicines.forEach { (m, times) ->
                val newId = dao.insert(m.copy(id = 0))
                medIds[m.id] = newId
                times.forEach { timeIds[it.id] = dao.insertTime(it.copy(id = 0, medicineId = newId)) }
            }
            dao.insertLogs(s.logs.mapNotNull { l ->
                val mid = medIds[l.medicineId] ?: return@mapNotNull null
                l.copy(id = 0, medicineId = mid, doseTimeId = l.doseTimeId?.let { timeIds[it] ?: return@mapNotNull null })
            })
        }
        Repo.rescheduleAll(context)
        return s.medicines.size
    }

    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
}
