# قواعد زمان‌بندی

پیاده‌سازی: `domain/Schedule.kt` — تست: `app/src/test/.../ScheduleTest.kt`

## انواع تکرار (`Frequency`)

| نوع | پارامتر | روز مصرف است اگر… | مثال |
|---|---|---|---|
| `DAILY` | — | همیشه | ویتامین هر روز |
| `WEEKDAYS` | `weekdays` (bitmask، بیت = `DayOfWeek.ordinal`، دوشنبه=۰) | روز هفته در mask باشد | **۲ عدد، فقط شنبه‌ها** |
| `EVERY_N_DAYS` | `intervalDays` | `(date - start) % n == 0` | یک روز در میان |
| `CYCLE` | `cycleOn`, `cycleOff` | `(date - start) % (on+off) < on` | ۲۱ روز مصرف، ۷ روز استراحت |
| `AS_NEEDED` | — | هیچ‌وقت (آلارم ندارد، فقط ثبت دستی) | مسکن |

در همه‌ی انواع: روز قبل از `startDate` یا بعد از `endDate` → مصرف نیست.

## چند وعده در روز
هر `DoseTime` ساعت و مقدار خودش را دارد (صبح ۱، شب ۲). «هر ۸ ساعت» در ویرایشگر یک میان‌بر است که ۳ DoseTime می‌سازد.

## `nextTrigger(after)`
اولین `date + minuteOfDay` که روز مصرف باشد و **بعد از** `after` باشد؛ حداکثر ۴۰۰ روز جلو می‌رود (برای دوره‌های بلند). اگر نبود → `null` (دوره تمام شده).

## وضعیت یک وعده در صفحه امروز
- لاگ دارد → TAKEN / SKIPPED
- ندارد و زمانش نرسیده → UPCOMING
- ندارد و گذشته → MISSED (قرمز)
