package com.AzaAza.foodcare

import android.app.Application
import android.util.Log
import com.AzaAza.foodcare.notification.ExpiryNotificationManager

class FoodCareApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 알림 채널 생성
        ExpiryNotificationManager.createNotificationChannel(this)

        // 정기적인 알림 예약 (오전 7시, 오전 11시, 오후 5시)
        ExpiryNotificationManager.scheduleNotifications(this)

        Log.d("FoodCareApp", "앱이 초기화되었습니다")
    }
}