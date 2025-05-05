package com.AzaAza.foodcare.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.AzaAza.foodcare.R
import com.AzaAza.foodcare.api.RetrofitClient
import com.AzaAza.foodcare.models.IngredientDto
import com.AzaAza.foodcare.ui.FoodManagementActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class ExpiryNotificationManager {

    companion object {
        private const val CHANNEL_ID = "expiry_notification_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "ExpiryNotification"

        private val notificationTexts = listOf(
            // 기본형 (직관적이고 명확한 표현)
            "⚠️ [식자재명]의 소비기한이 곧 도래합니다. 신속히 사용해주세요!",
            "⏰ [식자재명]의 소비기한이 [남은 일수]일 남았습니다. 빠른 소비를 권장합니다.",
            "📅 [식자재명]의 소비기한이 오늘입니다. 즉시 사용하시기 바랍니다.",

            // 친근하고 유쾌한 표현
            "🍽️ [식자재명], 이제 곧 작별 인사를 할 시간이에요. 오늘의 요리에 활용해보세요!",
            "👀 [식자재명]이(가) 냉장고에서 주목을 기다리고 있어요. 소비기한이 임박했답니다!",
            "🥦 [식자재명]이(가) 신선함을 유지하고 있어요. 소비기한 전에 맛있게 즐겨보세요!",

            // 요리 제안 포함
            "👩‍🍳 [식자재명]의 소비기한이 임박했습니다. 오늘은 맛있는 요리를 만들어보는 건 어떨까요?",
            "🍳 [식자재명]을(를) 활용한 맛있는 요리로 오늘의 식사를 준비해보세요. 소비기한이 가까워지고 있어요!"
        )

        // 알림 채널 생성 함수
        fun createNotificationChannel(context: Context) {
            // Android 8.0 (Oreo) 이상에서는 채널 생성이 필요합니다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "식자재 소비기한 알림"
                val descriptionText = "식자재의 소비기한이 임박했을 때 알림을 표시합니다"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                }

                // 시스템에 채널 등록
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)

                Log.d(TAG, "알림 채널이 생성되었습니다")
            }
        }

        // 배터리 최적화 예외 확인 및 요청 함수
        fun checkBatteryOptimization(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = context.packageName
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    // 배터리 최적화 예외가 되어 있지 않은 경우 사용자에게 안내
                    val batteryOptimizationDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("배터리 최적화 예외 설정")
                        .setMessage("정확한 알림 수신을 위해 배터리 최적화 예외 설정이 필요합니다.")
                        .setPositiveButton("설정하기") { _, _ ->
                            // 배터리 최적화 예외 설정 화면으로 이동
                            requestBatteryOptimizationExemption(context)
                        }
                        .setNegativeButton("나중에") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()

                    batteryOptimizationDialog.show()

                    Log.d(TAG, "배터리 최적화 예외 설정 필요")
                } else {
                    Log.d(TAG, "배터리 최적화 예외 설정됨")
                }
            }
        }

        // 배터리 최적화 예외 설정 화면으로 이동
        fun requestBatteryOptimizationExemption(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "배터리 최적화 예외 설정 화면 열기 실패", e)
                    // 일반 배터리 사용량 화면으로 대체
                    try {
                        val settingsIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                        context.startActivity(settingsIntent)
                    } catch (e2: Exception) {
                        Log.e(TAG, "배터리 설정 화면 열기 실패", e2)
                        Toast.makeText(context, "설정 앱에서 배터리 > 배터리 최적화에서 FoodCare 앱을 '최적화 안함'으로 설정해주세요.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // 알림 일정 예약 함수
        fun scheduleNotifications(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // 알림 시간 설정 (오전 7시, 오전 11시, 오후 3시 20분, 오후 5시)
            val notificationTimes = listOf(
                Pair(7, 0),    // 오전 7시 00분
                Pair(8, 0),    // 오전 8시 00분
                Pair(11, 0),   // 오전 11시 00분
                Pair(17, 0),  // 오후 4시 00분
                Pair(18, 0)    // 오후 5시 00분
            )

            // 현재 시간
            val now = System.currentTimeMillis()

            notificationTimes.forEachIndexed { index, (hour, minute) ->
                val intent = Intent(context, ExpiryNotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 지정된 시간으로 Calendar 설정
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    // 이미 지난 시간이면 다음 날로 설정
                    if (timeInMillis < now) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                // Android M(마시멜로) 이상에서는 Doze 모드를 고려한 알림 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 정확한 알람 권한 확인 (Android S 이상)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                            // 다음 날 같은 시간에도 알림이 울리도록 별도 설정 필요
                            setNextDayAlarm(context, index, hour, minute)
                        } else {
                            Log.w(TAG, "정확한 알람 권한이 없습니다. 설정에서 권한을 허용해 주세요.")
                            // 사용자에게 권한 설정 화면으로 이동 안내
                            Toast.makeText(
                                context,
                                "정확한 알림을 위해 설정에서 정확한 알람 권한을 허용해 주세요.",
                                Toast.LENGTH_LONG
                            ).show()

                            // 정확한 알람 권한 설정 화면으로 이동
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                intent.data = Uri.parse("package:${context.packageName}")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e(TAG, "정확한 알람 권한 설정 화면 열기 실패", e)
                            }
                        }
                    } else {
                        // Android S 미만에서는 권한 확인 없이 설정 가능
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        // 다음 날 같은 시간에도 알림이 울리도록 별도 설정 필요
                        setNextDayAlarm(context, index, hour, minute)
                    }
                } else {
                    // 안드로이드 M 미만에서는 setRepeating 사용
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }

                Log.d(TAG, "알림이 예약되었습니다: ${hour}시 ${minute}분, 다음 알림 시간: ${calendar.time}")
            }
        }

        // 매일 알림이 울리도록 다음 날 알림도 예약
        private fun setNextDayAlarm(context: Context, requestCode: Int, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ExpiryNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode + 100, // 기존 requestCode와 겹치지 않도록 100 추가
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 다음 날 같은 시간으로 설정
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1) // 다음 날
            }

            // 정확한 알람 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }

        // 즉시 소비기한 알림 표시 함수 (테스트용)
        fun showExpiryNotificationNow(context: Context) {
            checkExpiringIngredients(context)
        }

        // 소비기한 임박 식자재 확인 및 알림 표시 (public으로 변경)
        fun checkExpiringIngredients(context: Context) {
            // 서버에서 식자재 데이터 가져오기
            RetrofitClient.ingredientApiService.getIngredients().enqueue(object : Callback<List<IngredientDto>> {
                override fun onResponse(call: Call<List<IngredientDto>>, response: Response<List<IngredientDto>>) {
                    if (response.isSuccessful) {
                        val ingredients = response.body()
                        if (ingredients != null && ingredients.isNotEmpty()) {
                            processIngredients(context, ingredients)
                        } else {
                            Log.d(TAG, "소비기한이 임박한 식자재가 없습니다")
                        }
                    } else {
                        Log.e(TAG, "서버 응답 오류: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<IngredientDto>>, t: Throwable) {
                    Log.e(TAG, "서버 연결 실패", t)
                }
            })
        }

        // 가져온 식자재 데이터 처리 함수
        private fun processIngredients(context: Context, ingredientDtos: List<IngredientDto>) {
            // 오늘 날짜 가져오기
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // 소비기한이 3일 이내인 식자재 필터링
            val nearExpiryIngredients = ingredientDtos.filter { ingredient ->
                try {
                    val expiryDate = apiDateFormat.parse(ingredient.expiryDate) ?: return@filter false
                    val diffDays = ((expiryDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                    diffDays in 0..3 // 오늘 포함 3일 이내
                } catch (e: Exception) {
                    Log.e(TAG, "날짜 파싱 오류", e)
                    false
                }
            }.sortedBy {
                // 소비기한이 가장 임박한 순으로 정렬
                try {
                    apiDateFormat.parse(it.expiryDate)?.time ?: Long.MAX_VALUE
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            }

            if (nearExpiryIngredients.isNotEmpty()) {
                // 가장 임박한 식자재 또는 여러 식자재에 대한 알림 표시
                showNotification(context, nearExpiryIngredients)
            } else {
                Log.d(TAG, "소비기한이 임박한 식자재가 없습니다")
            }
        }

        // 알림 표시 함수
        private fun showNotification(context: Context, nearExpiryIngredients: List<IngredientDto>) {
            // 알림을 클릭했을 때 실행될 Intent 설정 (FoodManagementActivity로 이동)
            val intent = Intent(context, FoodManagementActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 소비기한이 가장 임박한 식자재
            val firstIngredient = nearExpiryIngredients.first()

            // 오늘 날짜 가져오기
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // 남은 일수 계산
            val expiryDate = apiDateFormat.parse(firstIngredient.expiryDate) ?: today
            val diffDays = ((expiryDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()

            // 랜덤 알림 텍스트 선택 (0일인 경우 특정 문구 제외)
            val availableTexts = if (diffDays == 0) {
                // 남은 일수가 0일인 경우 "[남은 일수]일 남았습니다" 형태의 문구는 제외
                notificationTexts.filterNot { it.contains("[남은 일수]일 남았습니다") }
            } else {
                notificationTexts
            }

            val randomTextIndex = Random.nextInt(availableTexts.size)
            var notificationText = availableTexts[randomTextIndex]

            // 알림 텍스트 가공
            notificationText = notificationText.replace("[식자재명]", firstIngredient.name)
            if (notificationText.contains("[남은 일수]")) {
                notificationText = notificationText.replace("[남은 일수]", diffDays.toString())
            }

            // 여러 식자재가 임박한 경우 알림 제목 조정
            val notificationTitle = if (nearExpiryIngredients.size > 1) {
                "${nearExpiryIngredients.size}개 식자재의 소비기한이 임박했습니다"
            } else {
                "${firstIngredient.name}의 소비기한이 임박했습니다"
            }

            // 알림 빌더 설정
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bell)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setColor(ContextCompat.getColor(context, R.color.your_background_color))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // 알림 클릭 시 자동 제거

            // 여러 식자재가 있는 경우 확장 스타일 적용
            if (nearExpiryIngredients.size > 1) {
                val inboxStyle = NotificationCompat.InboxStyle()
                    .setBigContentTitle("${nearExpiryIngredients.size}개 식자재의 소비기한이 임박했습니다")

                // 최대 5개 식자재만 표시
                nearExpiryIngredients.take(5).forEach { ingredient ->
                    try {
                        val expDate = apiDateFormat.parse(ingredient.expiryDate) ?: today
                        val expDiffDays = ((expDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
                        inboxStyle.addLine("${ingredient.name}: ${if (expDiffDays == 0) "오늘까지" else "${expDiffDays}일 남음"}")
                    } catch (e: Exception) {
                        inboxStyle.addLine("${ingredient.name}: 소비기한 확인 필요")
                    }
                }

                builder.setStyle(inboxStyle)
            }

            // 알림 표시
            with(NotificationManagerCompat.from(context)) {
                try {
                    notify(NOTIFICATION_ID, builder.build())
                    Log.d(TAG, "알림이 표시되었습니다: $notificationTitle")
                } catch (e: SecurityException) {
                    Log.e(TAG, "알림 권한이 없습니다", e)
                }
            }
        }
    }
}

// BroadcastReceiver - 알림 시간에 자동으로 호출됨
class ExpiryNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 소비기한 알림 확인 및 표시
        ExpiryNotificationManager.checkExpiringIngredients(context)

        // 테스트 알림 로그
        if (intent.getBooleanExtra("ONE_TIME_TEST", false)) {
            Log.d("ExpiryNotification", "일회성 테스트 알림이 실행되었습니다")
        }
    }
}