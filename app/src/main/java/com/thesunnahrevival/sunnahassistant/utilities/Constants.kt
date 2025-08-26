package com.thesunnahrevival.sunnahassistant.utilities

private const val SUNNAH_ASSISTANT_BASE_LINK =
    "https://play.google.com/store/apps/details?id=com.thesunnahrevival.sunnahassistant"

val SUPPORTED_LOCALES = arrayOf("en", "ar")
const val RETRY_AFTER_KEY = "Retry-After"
const val SUPPORT_EMAIL = "apps@thesunnahrevival.com"
const val EXPECTED_USER_AGENT = "SunnahAssistant-Android-App"
const val NOTIFICATION_PERMISSION_REQUESTS_COUNT_KEY = "notification-permission-requests-count"
const val DB_NAME = "SunnahAssistant.db"
const val DB_NAME_TEMP = "SunnahAssistant_temp.db"
const val SHARE = "SHARE"
const val LINK = "link"
const val MARK_AS_COMPLETE = "com.thesunnahrevival.sunnahassitant.MARK_AS_COMPLETE"
const val TO_DO_ID = "com.thesunnahrevival.sunnahassitant.TO_DO_ID"
const val NOTIFICATION_TITLE =
    "com.thesunnahrevival.sunnahassistant.utilities.notificationTitle"
const val NOTIFICATION_TEXT =
    "com.thesunnahrevival.sunnahassistant.utilities.notificationText"
const val NOTIFICATION_TONE_URI =
    "com.thesunnahrevival.sunnahassistant.utilities.notificationToneUri"
const val NOTIFICATION_VIBRATE =
    "com.thesunnahrevival.sunnahassistant.utilities.notificationVibrate"
const val NOTIFICATION_CATEGORY =
    "com.thesunnahrevival.sunnahassistant.utilities.notificationCategory"
const val NOTIFICATION_DND_MINUTES =
    "com.thesunnahrevival.sunnahassistant.utilities.dndMinutes"

const val TODO_REMINDER_SCHEDULER_WORK_TAG = "todo_reminder_scheduler_work"

const val TODO_NOTIFICATION_CHANNEL_PREFIX = "ToDo_"

const val DEVELOPER_MESSAGES_CHANNEL_ID = "Developer"

const val LOW_PRIORITY_MAINTENANCE_NOTIFICATION_CHANNEL_ID = "Low Priority Maintenance"

const val TEXT_COLOR = "text_color"

const val THE_SUNNAH_REVIVAL_RSS_FEED = "https://thesunnahrevival.com/category/daily-hadith/feed"

const val DOWNLOADS_NOTIFICATION_CHANNEL_ID = "Downloads"
const val DOWNLOAD_COMPLETE_NOTIFICATION_CHANNEL_ID = "DownloadComplete"

const val DOWNLOAD_WORK_TAG = "download_work"

const val PRAYER_TIMES_REMINDERS_ID = -1019700

// Tip IDs
const val TIP_PRAYER_TIME_ALERTS_ID = -900
const val TIP_UNRWA_GAZA_APPEAL_ID = -901
const val TIP_PRCS_PALESTINE_APPEAL_ID = -902
const val TIP_ISLAMIC_RELIEF_GAZA_APPEAL_ID = -903

val DONATION_APPEALS =  listOf(TIP_UNRWA_GAZA_APPEAL_ID, TIP_PRCS_PALESTINE_APPEAL_ID, TIP_ISLAMIC_RELIEF_GAZA_APPEAL_ID)

const val REQUEST_NOTIFICATION_PERMISSION_CODE = 100
const val REQUESTCODEFORUPDATE: Int = 1

const val FIREBASE_NOTIFICATION_ID = -2
const val REQUEST_ALARM_PERMISSION_CODE = -3
const val STICKY_NOTIFICATION_ID = -4
const val REFRESHING_NOTIFICATIONS_ID = -5
const val DOWNLOAD_NOTIFICATION_ID = -6
const val DOWNLOAD_COMPLETE_NOTIFICATION_ID = -7

fun getSunnahAssistantAppLink(
    utmSource: String = "Sunnah-Assistant-App",
    utmMedium: String = "share",
    utmCampaign: String? = null
): String {
    val baseUrl = SUNNAH_ASSISTANT_BASE_LINK
    val utmParams = mutableListOf<String>()

    utmSource.let { utmParams.add("utm_source=$it") }
    utmMedium.let { utmParams.add("utm_medium=$it") }
    utmCampaign?.let { utmParams.add("utm_campaign=$it") }

    return if (utmParams.isNotEmpty()) {
        "$baseUrl?${utmParams.joinToString("&")}"
    } else {
        baseUrl
    }
}