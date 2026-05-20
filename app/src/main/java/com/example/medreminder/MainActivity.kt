package com.example.medreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var medicineNameInput: EditText
    private lateinit var timeButton: Button
    private lateinit var saveButton: Button
    private lateinit var remindersList: LinearLayout
    
    private var selectedHour = 9
    private var selectedMinute = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        medicineNameInput = findViewById(R.id.medicineName)
        timeButton = findViewById(R.id.timeButton)
        saveButton = findViewById(R.id.saveButton)
        remindersList = findViewById(R.id.remindersList)
        
        // درخواست مجوز اعلان برای اندروید 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        
        timeButton.setOnClickListener {
            showTimePicker()
        }
        
        saveButton.setOnClickListener {
            saveReminder()
        }
        
        loadReminders()
    }
    
    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(selectedHour)
            .setMinute(selectedMinute)
            .setTitleText("زنگ یادآوری دارو")
            .build()
        
        picker.addOnPositiveButtonClickListener {
            selectedHour = picker.hour
            selectedMinute = picker.minute
            timeButton.text = String.format("%02d:%02d", selectedHour, selectedMinute)
        }
        
        picker.show(supportFragmentManager, "time_picker")
    }
    
    private fun saveReminder() {
        val medicineName = medicineNameInput.text.toString().trim()
        if (medicineName.isEmpty()) {
            Toast.makeText(this, "لطفاً نام دارو را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        setAlarm(medicineName, calendar.timeInMillis)
        saveToPreferences(medicineName, selectedHour, selectedMinute)
        
        Toast.makeText(this, "یادآور برای $medicineName در ساعت $selectedHour:$selectedMinute تنظیم شد", Toast.LENGTH_LONG).show()
        medicineNameInput.text.clear()
        loadReminders()
    }
    
    private fun setAlarm(medicineName: String, timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("medicine_name", medicineName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, medicineName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }
    
    private fun saveToPreferences(name: String, hour: Int, minute: Int) {
        val prefs = getSharedPreferences("reminders", MODE_PRIVATE)
        val count = prefs.getInt("count", 0)
        prefs.edit().apply {
            putString("reminder_${count}_name", name)
            putInt("reminder_${count}_hour", hour)
            putInt("reminder_${count}_minute", minute)
            putInt("count", count + 1)
            apply()
        }
    }
    
    private fun loadReminders() {
        remindersList.removeAllViews()
        val prefs = getSharedPreferences("reminders", MODE_PRIVATE)
        val count = prefs.getInt("count", 0)
        
        for (i in 0 until count) {
            val name = prefs.getString("reminder_${i}_name", "") ?: continue
            val hour = prefs.getInt("reminder_${i}_hour", 0)
            val minute = prefs.getInt("reminder_${i}_minute", 0)
            
            val textView = TextView(this).apply {
                text = "$name - ساعت ${String.format("%02d:%02d", hour, minute)}"
                textSize = 18f
                setPadding(16, 16, 16, 16)
            }
            remindersList.addView(textView)
        }
    }
}
