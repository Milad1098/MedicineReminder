# راهنمای توسعه

## ساخت و تست
- پیش‌نیاز: JDK 17 و Android SDK 35 (platform 35، build-tools 35.0.0).
- ساخت: `./gradlew assembleDebug` — تست: `./gradlew testDebugUnitTest` (`JAVA_HOME` باید ست باشد).
- اگر `dl.google.com` یا Google Maven در دسترس نبود، پکیج‌های SDK را از یک میرور نصب کنید و گروه‌های گوگل را در یک init script محلی Gradle (`~/.gradle/init.d/`) به میرور بفرستید — نه در خود ریپو.

## تست آلارم روی شبیه‌ساز
- شبیه‌ساز را با `-gpu host` اجرا کنید؛ بدون آن رندر نرم‌افزاری است و blur خیلی کند دیده می‌شود.
- `am force-stop` همه‌ی آلارم‌های اپ را پاک می‌کند؛ قبل از تست آلارم یک بار اپ را باز کنید.
- برای جلو بردن ساعت: `adb root`، بعد `settings put global auto_time 0` و `adb shell "date -s '2026-10-10 08:59:50'"`.
- در Git Bash قبل از دستورهای adb با مسیر `/data/...` بگذارید `MSYS_NO_PATHCONV=1`.
- هر تغییر UI را قبل از merge روی شبیه‌ساز یا گوشی واقعی ببینید.

## قواعد کد
- منطق زمان‌بندی فقط در `domain/Schedule.kt` (Kotlin خالص، بدون Android) و حتماً با تست.
- هر تغییر schema دیتابیس = Migration واقعی؛ `fallbackToDestructiveMigration` ممنوع.
- requestCode آلارم = `doseTime.id` (یکتا). هر DoseTime فقط آلارم «بعدی‌اش» را دارد و بعد از زنگ، بعدی را زمان‌بندی می‌کند.
- متن‌های UI فارسی، اعداد با ارقام فارسی (`.fa()` در `ui/Format.kt`).
- بعد از هر کار، [ROADMAP.md](ROADMAP.md) را به‌روز کنید.

مستندات دیگر: [ARCHITECTURE](ARCHITECTURE.md) · [SCHEDULING](SCHEDULING.md) · [DESIGN](DESIGN.md) · [DECISIONS](DECISIONS.md)
