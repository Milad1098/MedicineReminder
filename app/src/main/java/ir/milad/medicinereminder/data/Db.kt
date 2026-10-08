package ir.milad.medicinereminder.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MedicineWithTimes(
    @Embedded val medicine: Medicine,
    @Relation(parentColumn = "id", entityColumn = "medicineId") val times: List<DoseTime>,
)

@Dao
interface MedDao {
    @Transaction
    @Query("SELECT * FROM medicines ORDER BY archived, name")
    fun observeAll(): Flow<List<MedicineWithTimes>>

    @Transaction
    @Query("SELECT * FROM medicines WHERE archived = 0")
    suspend fun active(): List<MedicineWithTimes>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun get(id: Long): MedicineWithTimes?

    @Query("SELECT * FROM dose_times WHERE id = :id")
    suspend fun doseTime(id: Long): DoseTime?

    @Insert suspend fun insert(m: Medicine): Long
    @Update suspend fun update(m: Medicine)
    @Query("DELETE FROM medicines WHERE id = :id") suspend fun delete(id: Long)

    @Insert suspend fun insertTimes(t: List<DoseTime>)
    @Query("DELETE FROM dose_times WHERE medicineId = :medicineId") suspend fun deleteTimes(medicineId: Long)

    @Query("UPDATE medicines SET stock = stock + :delta WHERE id = :id AND stock IS NOT NULL")
    suspend fun adjustStock(id: Long, delta: Float)

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun log(l: DoseLog)
    @Query("DELETE FROM dose_logs WHERE id = :id") suspend fun deleteLog(id: Long)

    @Query("SELECT * FROM dose_logs WHERE doseTimeId = :doseTimeId AND scheduledAt = :at")
    suspend fun logFor(doseTimeId: Long, at: Long): DoseLog?

    @Query("SELECT * FROM dose_logs WHERE scheduledAt >= :from AND scheduledAt < :to ORDER BY scheduledAt")
    fun observeLogs(from: Long, to: Long): Flow<List<DoseLog>>
}

@Database(entities = [Medicine::class, DoseTime::class, DoseLog::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): MedDao

    companion object {
        @Volatile private var instance: AppDb? = null
        fun get(context: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDb::class.java, "medicine.db")
                .build().also { instance = it }
        }
    }
}
