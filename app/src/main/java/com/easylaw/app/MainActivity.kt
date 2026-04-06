package com.easylaw.app

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.navigation.AppRoute
import com.easylaw.app.navigation.NavRoute
import com.easylaw.app.navigation.NavRoute.bottomItems
import com.easylaw.app.ui.components.EasylawSideBar
import com.easylaw.app.ui.components.LanguageBottombar
import com.easylaw.app.ui.theme.EasyLawTheme
import com.easylaw.app.util.PreferenceManager
import com.easylaw.app.viewModel.root.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private val NAV_BAR_COLOR = Color(0xFFEAEFEF)
private val SELECTED_ICON_COLOR = Color(0xFFD95F1E)
private val UNSELECTED_ICON_COLOR = Color(0xFF797573)
private val LANGUAGE_DISPLAY_MAP =
    mapOf(
        "ko" to "한국어",
        "en" to "English",
        "ja" to "日本語",
    )

fun applyLocale(
    context: Context,
    languageCode: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context
            .getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags(languageCode)
    } else {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode),
        )
    }
}

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val _viewModel: MainViewModel by viewModels()

    @Inject lateinit var userSession: UserSession

    @Inject lateinit var supabase: SupabaseClient

    @Inject lateinit var preferenceManager: PreferenceManager

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callPostId(intent)
        // enableEdgeToEdge : 상태바와 하단 바까지 화면을 꽉 채운다.
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            val savedLanguage = preferenceManager.languageState.value
            applyLocale(this, savedLanguage)
        }

        /*
             앱 시작 시 로컬 저장소 및 세션 정보 불러오기
             I/O작업은 비동기 처리, 백그라운드에서 처리해준다.
             viewModel에서 하냐 Activity에서 실행하냐에 따라 viewModelScope, lifecycle 사용
         */
        lifecycleScope.launch {
            try {
                val savedUser = preferenceManager.userData.first()
                val currentSupabaseSession = supabase.auth.currentSessionOrNull()

                Log.d("session 유지 확인", "savedUser: $savedUser, currentSupabaseSession: $currentSupabaseSession")

                if (savedUser != null && savedUser.id.isNotEmpty()) {
                    userSession.setLoginInfo(savedUser)
                } else {
                    userSession.sessionClear()
                    preferenceManager.sessionClear()
                    if (currentSupabaseSession != null) supabase.auth.signOut()
//                    supabase.auth.signOut()
                    userSession.setInitialized(true)
                }
            } catch (e: Exception) {
                Log.e("session error", "유저 정보 로드 실패: ${e.message}")
                userSession.setInitialized(true)
            }
        }

        setContent {
            val viewState by _viewModel.mainViewState.collectAsStateWithLifecycle()

            EasyLawTheme {
                // 상태 관찰
                val userInfo by userSession.userInfo.collectAsState()
                Log.d("userinfo", userSession.getUserState().toString())
                val isInitialized by userSession.isInitialized.collectAsState()

                if (!isInitialized) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                    }
                } else {
                    // Nav
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Language
                    val languageState = remember { mutableStateOf(false) }
                    val currentLanguageCode by preferenceManager.languageState.collectAsState()
                    val currentLanguageDisplay = LANGUAGE_DISPLAY_MAP[currentLanguageCode] ?: "한국어"

                    // Drawer
                    val drawerState = rememberDrawerState(DrawerValue.Closed)

                    // scope
                    val scope = rememberCoroutineScope()

                    // sheet
                    val sheetState = rememberModalBottomSheetState()

                    // 로그인 상태에 따른 시작 경로 결정
//                    val startRoute = if (userInfo.id.isNotEmpty()) {
//                        NavRoute.COMMUNITY
//                    }
// //                    else if(){
// //
// //                    }
//                    else{
//                        NavRoute.ONBOARDING
//                    }
                    val isOnboardingComplete by preferenceManager.isOnboardingState
                        .collectAsState(initial = false)
                    val startRoute =
                        when {
                            userInfo.id.isNotEmpty() -> NavRoute.COMMUNITY
                            isOnboardingComplete -> NavRoute.LOGIN
                            else -> NavRoute.ONBOARDING
                        }

//                    Log.d("시작 위치", startRoute)

                    LaunchedEffect(viewState.deepLinkPostId) {
                        if (!viewState.deepLinkPostId.isNullOrBlank()) {
                            try {
                                navController.navigate("communityDetail/${viewState.deepLinkPostId}") {
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Log.e("NavigationError", "이동 실패: ${e.message}")
                            } finally {
                                _viewModel.updateDeepPostId(null)
                            }
                        }
                    }

                    val hideBarsRoutes =
                        listOf(
                            NavRoute.ONBOARDING,
                            NavRoute.LOGIN,
                            NavRoute.SIGN_UP,
                            NavRoute.COMMUNITY_WRITE,
                            NavRoute.COMMUNITY_DETAIL,
                            NavRoute.COMMUNITY_UPDATE,
                            NavRoute.MAP,
                            NavRoute.LAYWERS_RESERVE,
                            NavRoute.LAYWERS_RESERVE_DETAIL,
                        )

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentRoute !in hideBarsRoutes,
                        drawerContent = {
                            EasylawSideBar(
                                userInfo = userInfo,
                                selectedLanguage = currentLanguageDisplay,
                                currentRoute = currentRoute,
                                onLanguageClick = {
                                    scope.launch {
                                        drawerState.close()
                                        languageState.value = true
                                    }
                                },
                                onMenuClick = { route ->
                                    scope.launch {
                                        drawerState.close()
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo(
                                                    navController.graph
                                                        .findStartDestination()
                                                        .id,
                                                ) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                onLogoutClick = {
                                    scope.launch {
                                        userSession.sessionClear()
                                        preferenceManager.sessionClear()
                                        drawerState.close()
                                        if (navController.currentBackStackEntry
                                                ?.destination
                                                ?.route != NavRoute.LOGIN
                                        ) {
                                            navController.navigate(NavRoute.LOGIN) {
                                                popUpTo(navController.graph.id) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                            )
                        },
                    ) {
                        Scaffold(
                            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
                            bottomBar = {
                                if (currentRoute !in hideBarsRoutes) {
                                    NavigationBar(
                                        containerColor = NAV_BAR_COLOR,
                                        tonalElevation = 8.dp,
                                    ) {
                                        bottomItems.forEach { item ->
                                            val isSelected = currentRoute == item.route

                                            NavigationBarItem(
                                                selected = isSelected,
                                                label = {
                                                    Text(
                                                        text = stringResource(item.titleResId),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        fontSize = 10.sp,
                                                    )
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = stringResource(item.titleResId),
                                                    )
                                                },
                                                colors =
                                                    NavigationBarItemDefaults.colors(
                                                        selectedIconColor = SELECTED_ICON_COLOR,
                                                        selectedTextColor = SELECTED_ICON_COLOR,
                                                        unselectedIconColor = UNSELECTED_ICON_COLOR,
                                                        unselectedTextColor = UNSELECTED_ICON_COLOR,
                                                    ),
                                                onClick = {
                                                    if (currentRoute != item.route) {
                                                        navController.navigate(item.route) {
                                                            popUpTo(
                                                                navController.graph
                                                                    .findStartDestination()
                                                                    .id,
                                                            ) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            },
                        ) { innerPadding ->
                            // AppRoute에 패딩과 navController 전달
                            AppRoute(
                                modifier = if (currentRoute in hideBarsRoutes) Modifier else Modifier.padding(innerPadding),
                                navController = navController,
                                startDestination = startRoute,
                                preferenceManager = preferenceManager,
                            )
                        }
                    }
                    if (languageState.value) {
                        ModalBottomSheet(
                            onDismissRequest = { languageState.value = false },
                            sheetState = sheetState,
                        ) {
                            LanguageBottombar(
                                currentLanguageCode = currentLanguageCode,
                                onLanguageSelected = { selectedCode ->
                                    scope.launch {
                                        preferenceManager.saveLanguage(selectedCode)
                                        applyLocale(this@MainActivity, selectedCode)
                                        sheetState.hide()
                                        languageState.value = false
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // onNewIntent는 앱이 켜져있을 때만 실행
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
//        viewModel.updateDeepPostId(intent.data?.getQueryParameter("postId"))
        callPostId(intent)
    }

    fun callPostId(intent: Intent) {
        try {
            // 카톡 공유하기
            intent?.data?.let { uri ->
                val postId = uri.getQueryParameter("postId")
                // 유효성 검사 추가 (숫자인지, 혹은 비어있지 않은지)
                if (!postId.isNullOrBlank() && postId != "null") {
                    _viewModel.updateDeepPostId(postId)
                }
            }

            // 알림
            val fcmPostId = intent?.extras?.getString("postId")
            if (!fcmPostId.isNullOrBlank() && fcmPostId != "null") {
                _viewModel.updateDeepPostId(fcmPostId)
            }
        } catch (e: Exception) {
            Log.e("DeepLink", "잘못된 접근 차단: ${e.message}")
        }

        // 카톡 공유하기
        intent?.data?.let { uri ->
            val postId = uri.getQueryParameter("postId")
            if (!postId.isNullOrBlank()) {
                _viewModel.updateDeepPostId(postId)
            }
        }
        // 알림
        intent?.extras?.getString("postId")?.let { fcmPostId ->
            _viewModel.updateDeepPostId(fcmPostId)
        }
    }
}
