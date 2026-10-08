# یادآور دارو · MedicineReminder

اپ اندرویدی متن‌باز برای اینکه هیچ دارویی جا نیفتد — فارسی، راست‌به‌چپ، با طراحی تمیز به سبک iOS.

An open-source Android medication reminder (Persian / RTL), built with Kotlin and Jetpack Compose.

[![Android CI](https://github.com/Milad1098/MedicineReminder/actions/workflows/android.yml/badge.svg)](https://github.com/Milad1098/MedicineReminder/actions/workflows/android.yml)

## امکانات

- **هر نوع نسخه‌ای**: هر روز، روزهای خاص هفته (مثلاً «۲ عدد، فقط شنبه‌ها»)، هر چند روز، دوره‌ای (۲۱ روز مصرف / ۷ روز استراحت)، در صورت نیاز
- **مقدار در هر وعده**: ۲ عدد، نصف قرص، ۱۰ میلی‌لیتر، ۲ پاف… و چند ساعت در روز با مقدار متفاوت
- **مدت درمان**: مثلاً آنتی‌بیوتیک ۱۰ روزه؛ بعد از آن دارو به آرشیو می‌رود
- **آلارمی که جا نمی‌اندازد**: روی صفحه‌ی قفل، دکمه‌های «خوردم / ۱۰ دقیقه بعد / رد کردن» روی خود نوتیف، و اگر جواب ندهی دوباره یادآوری می‌کند
- **موجودی قرص**: با هر مصرف کم می‌شود و وقت تمدید نسخه خبرت می‌کند
- **صفحه‌ی امروز**: حلقه‌ی پیشرفت روز و وعده‌ها به ترتیب ساعت؛ با یک لمس ثبت کن
- **تاریخچه**: درصد پایبندی ۷ روز اخیر و سابقه‌ی هر وعده (خورده شد / رد شد / جا افتاد)
- **شناخت دارو از ظاهر**: شکل (قرص، کپسول، شربت، قطره، اسپری، آمپول) و رنگ دلخواه
- تم روشن و تیره، فونت وزیرمتن، تاریخ شمسی
- بدون اینترنت، بدون تبلیغ، بدون ارسال داده — همه‌چیز روی گوشی خودت می‌ماند

## دانلود

آخرین APK از بخش [Releases](https://github.com/Milad1098/MedicineReminder/releases). اندروید ۸ به بالا.

> بعد از نصب، کارت «دسترسی‌ها» در صفحه‌ی امروز را کامل کن (نوتیف، آلارم دقیق، نمایش تمام‌صفحه). روی شیائومی و سامسونگ بهینه‌سازی باتری را هم برای اپ خاموش کن.

## ساخت از سورس

نیازمندی‌ها: JDK 17 و Android SDK 35

```bash
./gradlew testDebugUnitTest   # تست‌های منطق زمان‌بندی
./gradlew assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
```

## ساختار

Kotlin · Jetpack Compose (Material 3) · Room · AlarmManager — بدون کتابخانه‌ی اضافه.
جزئیات معماری در [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)، قواعد زمان‌بندی در [docs/SCHEDULING.md](docs/SCHEDULING.md) و نقشه‌ی راه در [docs/ROADMAP.md](docs/ROADMAP.md).

## مشارکت

Issue و Pull Request خوش‌آمد است. قبل از تغییر منطق زمان‌بندی، تست مربوطش را در `ScheduleTest` اضافه کن.

## مجوز

[MIT](LICENSE)
