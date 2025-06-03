package com.AzaAza.foodcare.data

import android.content.Context


/* 안 쓰는 함수 많은 것 같은데, 주석 이상해*/
object UserSession {
    private const val PREF_NAME = "user_prefs"
    private const val USER_ID_KEY = "USER_ID"
    private const val USER_LOGIN_ID_KEY = "USER_LOGIN_ID"
    private const val IS_LOGGED_IN_KEY = "IS_LOGGED_IN"

    /**
     * 현재 로그인한 사용자 ID 가져오기
     */
    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(USER_ID_KEY, -1)
    }

    /**
     * 현재 로그인한 사용자 로그인 ID 가져오기
     */
    fun getUserLoginId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_LOGIN_ID_KEY, null)
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_LOGGED_IN_KEY, false)
    }

    /**
     * 사용자 정보 저장
     */
    fun saveUserInfo(context: Context, userId: Int, loginId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(USER_ID_KEY, userId)
            .putString(USER_LOGIN_ID_KEY, loginId)
            .putBoolean(IS_LOGGED_IN_KEY, true)
            .apply()
    }

    /**
     * 로그아웃 (사용자 정보 삭제)
     */
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}