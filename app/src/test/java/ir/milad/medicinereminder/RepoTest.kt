package ir.milad.medicinereminder

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ir.milad.medicinereminder.data.AppDb
import ir.milad.medicinereminder.data.Backup
import ir.milad.medicinereminder.data.DoseTime
import ir.milad.medicinereminder.data.LogStatus
import ir.milad.medicinereminder.data.Medicine
import ir.milad.medicinereminder.data.Repo
import ir.milad.medicinereminder.domain.toMillis
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** The data paths that broke during v2 development: logging, stock, editing, alarm list, backup. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepoTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private lateinit var db: AppDb
    private val dao get() = db.dao()
    private val today = LocalDate.now()

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ctx, AppDb::class.java).allowMainThreadQueries().build()
        AppDb.replaceForTests(db)
    }

    @After fun tearDown() = db.close()

    /** Daily medicine at 08:00 (2 pills) and 20:00 (1 pill), 10 in stock. */
    private fun seed(): Long = runBlocking {
        Repo.save(ctx, Medicine(name = "متفورمین", startDate = today.minusDays(3).toEpochDay(), stock = 10f, refillAt = 5f),
            listOf(DoseTime(minuteOfDay = 8 * 60, amount = 2f), DoseTime(minuteOfDay = 20 * 60, amount = 1f)))
    }

    private fun at(day: LocalDate, minute: Int) = day.atTime(minute / 60, minute % 60).toMillis()

    @Test fun takeLogsAndDecrementsStock_onlyOnce() = runBlocking {
        val id = seed()
        val t = dao.get(id)!!.times.first { it.minuteOfDay == 8 * 60 }
        val when8 = at(today, 8 * 60)
        Repo.take(ctx, id, t.id, when8, t.amount)
        Repo.take(ctx, id, t.id, when8, t.amount) // double tap / notification + screen
        assertEquals(8f, dao.get(id)!!.medicine.stock)
        assertEquals(LogStatus.TAKEN, dao.logFor(t.id, when8)!!.status)
    }

    @Test fun undoAndSkipGiveStockBack() = runBlocking {
        val id = seed()
        val t = dao.get(id)!!.times.first { it.minuteOfDay == 8 * 60 }
        val when8 = at(today, 8 * 60)
        Repo.take(ctx, id, t.id, when8, t.amount)
        Repo.undo(ctx, dao.logFor(t.id, when8)!!)
        assertEquals(10f, dao.get(id)!!.medicine.stock)
        assertNull(dao.logFor(t.id, when8))

        Repo.take(ctx, id, t.id, when8, t.amount)
        Repo.skip(ctx, id, t.id, when8, t.amount) // changed mind: taken → skipped
        assertEquals(10f, dao.get(id)!!.medicine.stock)
        assertEquals(LogStatus.SKIPPED, dao.logFor(t.id, when8)!!.status)
    }

    @Test fun editingKeepsHistoryOfUnchangedTimes() = runBlocking {
        val id = seed()
        val (m, times) = dao.get(id)!!
        val t8 = times.first { it.minuteOfDay == 8 * 60 }
        Repo.take(ctx, id, t8.id, at(today, 8 * 60), 2f)

        // keep 08:00 (now 1 pill), drop 20:00, add 14:00
        Repo.save(ctx, m.copy(name = "متفورمین ۵۰۰"),
            listOf(t8.copy(amount = 1f), DoseTime(minuteOfDay = 14 * 60)))

        val after = dao.get(id)!!.times
        assertEquals(listOf(8 * 60, 14 * 60), after.map { it.minuteOfDay }.sorted())
        assertEquals(t8.id, after.first { it.minuteOfDay == 8 * 60 }.id)
        assertNotNull("history must stay linked", dao.logFor(t8.id, at(today, 8 * 60)))
    }

    @Test fun ringingListsDueUnansweredDosesOnly() = runBlocking {
        val id = seed()
        val times = dao.get(id)!!.times
        val now = at(today, 20 * 60 + 5) // 20:05
        val t8 = times.first { it.minuteOfDay == 8 * 60 }
        Repo.take(ctx, id, t8.id, at(today, 8 * 60), 2f)

        // 08:00 is answered (and outside the 3h window anyway); 20:00 is ringing
        val ringing = Repo.ringing(ctx, now)
        assertEquals(listOf(20 * 60), ringing.map { it.time.minuteOfDay })
    }

    @Test fun backupRoundTripKeepsLogsAttached() = runBlocking {
        val id = seed()
        val t8 = dao.get(id)!!.times.first { it.minuteOfDay == 8 * 60 }
        Repo.take(ctx, id, t8.id, at(today, 8 * 60), 2f)
        Repo.take(ctx, id, null, at(today, 10 * 60), 1f) // as-needed style log

        val json = Backup.toJson(Backup.Snapshot(dao.all(), dao.allLogs()))
        dao.deleteAll()
        // a different medicine on the "new phone" gets replaced by the backup
        Repo.save(ctx, Medicine(name = "قدیمی", startDate = today.toEpochDay()), listOf(DoseTime(minuteOfDay = 60)))

        assertEquals(1, Backup.restore(ctx, Backup.fromJson(json)))
        val (m, times) = dao.all().single()
        assertEquals("متفورمین", m.name)
        assertEquals(7f, m.stock)
        val newT8 = times.first { it.minuteOfDay == 8 * 60 }
        assertEquals(LogStatus.TAKEN, dao.logFor(newT8.id, at(today, 8 * 60))!!.status)
        assertEquals(2, dao.allLogs().size)
        assertTrue(dao.allLogs().all { it.medicineId == m.id })
    }

    @Test fun restoreRejectsForeignFiles() {
        assertThrows(Exception::class.java) { Backup.fromJson("""{"app":"other","format":1}""") }
        assertThrows(Exception::class.java) { Backup.fromJson("not json") }
    }
}
