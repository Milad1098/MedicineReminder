package ir.milad.medicinereminder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Form { PILL, CAPSULE, LIQUID, DROPS, INHALER, INJECTION }

enum class Instruction { NONE, BEFORE_FOOD, AFTER_FOOD, WITH_FOOD, EMPTY_STOMACH }

enum class Frequency { DAILY, WEEKDAYS, EVERY_N_DAYS, CYCLE, AS_NEEDED }

enum class LogStatus { TAKEN, SKIPPED }

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val form: Form = Form.PILL,
    val color: Int = 0xFFFFFFFF.toInt(),
    val instruction: Instruction = Instruction.NONE,
    val note: String = "",
    val frequency: Frequency = Frequency.DAILY,
    /** bit = DayOfWeek.ordinal (MONDAY = 0) */
    val weekdays: Int = 0,
    val intervalDays: Int = 2,
    val cycleOn: Int = 21,
    val cycleOff: Int = 7,
    /** epochDay */
    val startDate: Long,
    /** epochDay, inclusive; null = no end */
    val endDate: Long? = null,
    /** null = not tracking stock */
    val stock: Float? = null,
    val refillAt: Float = 5f,
    val archived: Boolean = false,
)

@Entity(
    tableName = "dose_times",
    foreignKeys = [ForeignKey(
        entity = Medicine::class, parentColumns = ["id"], childColumns = ["medicineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicineId")]
)
data class DoseTime(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long = 0,
    val minuteOfDay: Int,
    val amount: Float = 1f,
)

@Entity(
    tableName = "dose_logs",
    foreignKeys = [ForeignKey(
        entity = Medicine::class, parentColumns = ["id"], childColumns = ["medicineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicineId"), Index(value = ["doseTimeId", "scheduledAt"], unique = true)]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    /** null for as-needed doses */
    val doseTimeId: Long?,
    /** epoch millis of the planned dose (as-needed: the moment it was taken) */
    val scheduledAt: Long,
    val status: LogStatus,
    val actionAt: Long,
    val amount: Float,
)
