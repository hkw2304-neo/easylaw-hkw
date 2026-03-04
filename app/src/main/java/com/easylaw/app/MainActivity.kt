package com.easylaw.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.navigation.AppRoute
import com.easylaw.app.navigation.NavRoute
import com.easylaw.app.navigation.NavRoute.bottomItems
import com.easylaw.app.ui.components.EasylawSideBar
import com.easylaw.app.ui.theme.EasyLawTheme
import com.easylaw.app.ui.theme.TossBlue
import com.easylaw.app.ui.theme.TossGrey100
import com.easylaw.app.ui.theme.TossGrey400
import com.easylaw.app.ui.theme.TossGrey600
import com.easylaw.app.ui.theme.TossWhite
import com.easylaw.app.util.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

private val NAV_BAR_COLOR = Color(0xFFEAEFEF)
private val SELECTED_ICON_COLOR = Color(0xFFD95F1E)
private val UNSELECTED_ICON_COLOR = Color(0xFF797573)
private val DRAWER_BACKGROUND_COLOR = Color.White

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userSession: UserSession

    @Inject lateinit var supabase: SupabaseClient

    @Inject lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 앱 시작 시 로컬 저장소 및 세션 정보 불러오기
        lifecycleScope.launch {
            try {
                val savedUser = preferenceManager.userData.firstOrNull()
                val currentSupabaseSession = supabase.auth.currentSessionOrNull()

                if (currentSupabaseSession != null && savedUser != null) {
                    userSession.setLoginInfo(savedUser)
                } else {
                    userSession.sessionClear()
                    preferenceManager.sessionClear()
                    supabase.auth.signOut()
                }
            } catch (e: Exception) {
                Log.e("Init", "유저 정보 로드 실패: ${e.message}")
            } finally {
                // 성공/실패 여부와 상관없이 초기화 완료 신호 전달
                userSession.setInitialized(true)
            }
        }

        setContent {
            EasyLawTheme {
                // 상태 관찰
                val userInfo by userSession.userInfo.collectAsState()
                val isInitialized by userSession.isInitialized.collectAsState()

                // 1. 세션 정보를 가져오는 동안 Splash/로딩 화면 출력
                if (!isInitialized) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        // 로고나 인디케이터를 넣어도 좋습니다.
                    }
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // 로그인 상태에 따른 시작 경로 결정
                    val startRoute = if (userInfo.id.isNotEmpty()) NavRoute.COMMUNITY else NavRoute.ONBOARDING

                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val hideBarsRoutes = listOf(NavRoute.ONBOARDING, NavRoute.LOGIN, NavRoute.SIGN_UP, NavRoute.COMMUNITY_WRITE)

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentRoute !in hideBarsRoutes,
                        drawerContent = {
                            EasylawSideBar(
                                userInfo = userInfo,
                                onLanguageClick = {},
                                onLogoutClick = {
                                    scope.launch {
                                        userSession.sessionClear()
                                        preferenceManager.sessionClear()
                                        drawerState.close()

                                        if (navController.currentBackStackEntry?.destination?.route != NavRoute.ONBOARDING) {
                                            navController.navigate(NavRoute.ONBOARDING) {
                                                popUpTo(navController.graph.id) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                            )
                        },
                    ) {
                        Scaffold(
                            bottomBar = {
                                if (currentRoute !in hideBarsRoutes) {
                                    NavigationBar(
                                        containerColor = TossWhite,
                                        tonalElevation = 0.dp,
                                        windowInsets = WindowInsets.navigationBars,
                                        modifier =
                                            Modifier
                                                .height(120.dp)
                                                .drawBehind {
                                                    drawLine(
                                                        color = TossGrey100,
                                                        start = Offset(0f, 0f),
                                                        end = Offset(size.width, 0f),
                                                        strokeWidth = 1.dp.toPx(),
                                                    )
                                                },
                                    ) {
                                        bottomItems.forEach { item ->
                                            val isSelected = currentRoute == item.route

                                            NavigationBarItem(
                                                selected = isSelected,
                                                label = {
                                                    Text(
                                                        text = item.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        letterSpacing = (-0.3).sp,
                                                    )
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = item.title,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                },
                                                colors =
                                                    NavigationBarItemDefaults.colors(
                                                        indicatorColor = Color.Transparent,
                                                        selectedIconColor = TossBlue,
                                                        selectedTextColor = TossBlue,
                                                        unselectedIconColor = TossGrey400,
                                                        unselectedTextColor = TossGrey600,
                                                    ),
                                                onClick = {
                                                    if (currentRoute != item.route) {
                                                        navController.navigate(item.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
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
                            )
                        }
                    }
                }
            }
        }
    }
}

private val DRAWER_DIVIDER_COLOR = Color(0xFFEEEEEE)

// @Composable
// fun EasylawSideBar(
//    userInfo: UserInfo,
//    onLogoutClick: () -> Unit,
// ) {
//    ModalDrawerSheet(
//        drawerContainerColor = DRAWER_BACKGROUND_COLOR,
//        modifier = Modifier.width(280.dp),
//    ) {
//        Column(
//            modifier =
//                Modifier
//                    .fillMaxHeight()
//                    .padding(20.dp),
//        ) {
//            // 상단 유저 정보 영역
//            Text(
//                text = if (userInfo.id.isNotEmpty()) "${userInfo.name}님 환영합니다" else "로그인이 필요합니다",
//                style = MaterialTheme.typography.titleMedium,
//                color = Color.Black,
//            )
//            Text(
//                text = userInfo.email,
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.Gray,
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//            HorizontalDivider(thickness = 1.dp, color = DRAWER_DIVIDER_COLOR)
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 메뉴 리스트
//            NavigationDrawerItem(
//                label = { Text(text = "로그아웃", color = Color.Red) },
//                selected = false,
//                onClick = onLogoutClick,
//                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red) },
//                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
//            )
//        }
//    }
// }
