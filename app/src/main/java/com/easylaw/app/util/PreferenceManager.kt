package com.easylaw.app.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.easylaw.app.domain.model.UserInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/*
로그인 유저의 세션 관리

1. 앱 실행 중 유저 정보(UserInfo)를 메모리에 유지 (UserState)
2. 로그인 시 정보를 기기(DataStore)에 영구 저장하여 자동 로그인 지원
3. 앱 재시작 시 저장된 데이터를 복구하여 로그인 절차 생략

 */

@Singleton
class PreferenceManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dataStore: DataStore<Preferences>,
    ) {
        private val userDataKey = stringPreferencesKey("user_data")
        private val languageKey = stringPreferencesKey("app_language")
        private val _onboardingKey = booleanPreferencesKey("onboarding_key")

        // Singleton scope: PreferenceManager 생명주기와 동일하게 유지
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val userData =
            dataStore.data.map { prefs ->
                val json = prefs[userDataKey] ?: ""
//                val json = prefs[userDataKey] ?: return@map null
                try {

                    when {
                        json.isEmpty() -> {
                            UserInfo()
                        }
                        else -> {
                            Json.decodeFromString<UserInfo>(json)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("preference error", e.toString())
                    null
                }
            }

        // 로그인 시 세션 정보 저장
        suspend fun saveUser(userInfo: UserInfo) {
            val json = Json.encodeToString(userInfo)
            Log.d("PreferenceManager", "DataStore에 저장 시도: $json") // 👈 추가
            dataStore.edit { prefs ->
                prefs[userDataKey] = json
            }
            Log.d("PreferenceManager", "DataStore 저장 완료!") // 👈 추가
        }

        // 로그아웃
        suspend fun sessionClear() {
            dataStore.edit { it.remove(userDataKey) }
        }

        suspend fun saveLanguage(languageCode: String) {
            dataStore.edit { prefs ->
                prefs[languageKey] = languageCode
            }
        }

        suspend fun saveOnboarding(key: Boolean) {
            dataStore.edit { prefs ->
                prefs[_onboardingKey] = key
            }
        }

        val isOnboardingState =
            dataStore.data.map { prefs ->
                prefs[_onboardingKey] ?: false
            }

        val languageState: StateFlow<String> =
            dataStore.data.map { prefs -> prefs[languageKey] ?: "ko" }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = runBlocking { dataStore.data.first()[languageKey] ?: "ko" },
            )

        // 지문 등록
        val masterKey =
            MasterKey
                .Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        // 2. EncryptedSharedPreferences 인스턴스 생성
        val sharedPreferences =
            EncryptedSharedPreferences.create(
                context,
                "secure_prefs", // 파일 이름
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // 키 암호화 방식
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM, // 값 암호화 방식
            )

        fun saveBiometricData(
            id: String,
            pw: String,
        ) {
            sharedPreferences.edit().apply {
                putString("biometric_id", id)
                putString("biometric_pwd", pw)
                putBoolean("is_biometric_enabled", true)
                apply()
            }
        }

        fun getBiometricData(): Pair<String, String>? {
            val id = sharedPreferences.getString("biometric_id", null)
            val pw = sharedPreferences.getString("biometric_pwd", null)

            return if (id != null && pw != null) {
                Pair(id, pw)
            } else {
                null
            }
        }

        fun isBiometricEnabled(): Boolean = sharedPreferences.getBoolean("is_biometric_enabled", false)

        fun clearBiometricData() {
            sharedPreferences.edit().apply {
                remove("biometric_id")
                remove("biometric_pwd")
                putBoolean("is_biometric_enabled", false)
                apply()
            }
        }
    }
