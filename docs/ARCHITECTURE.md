# معماری

## لایه‌ها (عمداً ساده)

```
ui/        Compose screens  ──┐
                              ├──> data/Repo.kt ──> Room (data/Db.kt)
alarm/     Receivers, Activity┘          │
                                         └──> alarm/AlarmScheduler.kt ──> AlarmManager
domain/    Schedule.kt  (Kotlin خالص — بدون وابستگی اندروید، با تست)
```

- بدون DI، بدون ViewModel جدا: `Repo` یک object است که UI و Receiverها هر دو صدا می‌زنند. وقتی صفحات پیچیده شدند ViewModel اضافه می‌شود.
- بدون navigation library: سه تب با یک `rememberSaveable` index.

## فایل‌ها

| مسیر | مسئولیت |
|---|---|
| `domain/Schedule.kt` | `isDueOn(date)`، `nextTrigger(after)`، خلاصه‌ی متنی برنامه |
| `data/Entities.kt` | `Medicine`، `DoseTime`، `DoseLog` + enumها |
| `data/Db.kt` | Room database + DAO |
| `data/Repo.kt` | عملیات سطح بالا: ذخیره دارو (با زمان‌بندی آلارم)، ثبت مصرف (با کم‌کردن موجودی) |
| `alarm/AlarmScheduler.kt` | زمان‌بندی/لغو آلارم‌ها |
| `alarm/AlarmReceiver.kt` | زنگ زدن، اکشن‌های نوتیف (خوردم/بعداً/رد) |
| `alarm/BootReceiver.kt` | زمان‌بندی دوباره بعد از ریبوت/تغییر ساعت/آپدیت اپ |
| `alarm/AlarmActivity.kt` | صفحه‌ی تمام‌صفحه‌ی آلارم |
| `alarm/Notifications.kt` | کانال‌ها و ساخت نوتیف‌ها |
| `ui/...` | تم، کامپوننت‌ها، صفحات امروز / داروها / تاریخچه / ویرایشگر |

## مدل داده (Room، نسخه ۱ از v2.0)

```
Medicine 1───* DoseTime        (ساعت + مقدار هر وعده)
Medicine 1───* DoseLog         (سابقه‌ی مصرف: TAKEN / SKIPPED)
```

- **Medicine**: نام، شکل (قرص/کپسول/شربت/…)، رنگ، دستور مصرف (قبل/بعد غذا…)، نوع تکرار + پارامترها، تاریخ شروع/پایان، موجودی و حد هشدار، آرشیو.
- **DoseTime**: `minuteOfDay` + `amount` (مثلاً ۲ عدد). برای «در صورت نیاز» خالی است.
- **DoseLog**: `(doseTimeId, scheduledAt)` یکتاست. «فراموش‌شده» ذخیره نمی‌شود؛ مشتق می‌شود (گذشته + بدون لاگ).

## جریان آلارم

```
schedule(doseTime) → AlarmManager.setAlarmClock(nextTrigger)  [requestCode = doseTime.id]
          │
          ▼ زمان رسید
AlarmReceiver.FIRE
  ├─ اگر وعده‌ی معمول بود → وعده‌ی بعدی همین DoseTime را زمان‌بندی کن
  ├─ اگر قبلاً لاگ شده (مثلاً زودتر خورده) → هیچ
  ├─ نوتیف آلارم (صدای ممتد، full-screen، دکمه‌های خوردم/۱۰ دقیقه بعد/رد) — بعد از ۶۰ ثانیه خودش می‌رود
  └─ پیگیری: اگر attempt < 2، یک FIRE دیگر ۱۵ دقیقه بعد  [requestCode = id + 1_000_000]
اکشن TAKE → DoseLog + کم کردن موجودی (+ نوتیف تمدید) ؛ SNOOZE → FIRE پیگیری ۱۰ دقیقه بعد
```
