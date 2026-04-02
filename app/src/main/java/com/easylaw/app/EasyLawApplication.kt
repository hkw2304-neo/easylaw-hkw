package com.easylaw.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

/**
 * EasyLaw 애플리케이션 클래스
 *
 * 주로 외부 라이브러리 등록 시 사용
 */
@HiltAndroidApp
class EasyLawApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}
