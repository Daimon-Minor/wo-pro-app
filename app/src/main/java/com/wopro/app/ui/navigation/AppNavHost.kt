package com.wopro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wopro.app.WOProApp
import com.wopro.app.ui.VMFactory
import com.wopro.app.ui.auth.AuthVMFactory
import com.wopro.app.ui.auth.ForgotPasswordScreen
import com.wopro.app.ui.auth.LoginScreen
import com.wopro.app.ui.auth.RegisterScreen
import com.wopro.app.ui.auth.AuthViewModel
import com.wopro.app.ui.home.HomeScreen
import com.wopro.app.ui.workorder.WorkOrderListScreen
import com.wopro.app.ui.workorder.WorkOrderFormScreen
import com.wopro.app.ui.workorder.WorkOrderDetailScreen
import com.wopro.app.ui.project.ProjectListScreen
import com.wopro.app.ui.project.ProjectFormScreen
import com.wopro.app.ui.project.ProjectDetailScreen
import com.wopro.app.ui.audit.AuditListScreen
import com.wopro.app.ui.audit.AuditFormScreen
import com.wopro.app.ui.utility.MeterListScreen
import com.wopro.app.ui.utility.MeterFormScreen
import com.wopro.app.ui.aichat.AiChatScreen
import com.wopro.app.ui.settings.SettingsScreen
import com.wopro.app.ui.main.MainScreen
import com.wopro.app.ui.team.TeamsListScreen
import com.wopro.app.ui.team.TeamFormScreen
import com.wopro.app.ui.notification.NotificationsScreen
import com.wopro.app.ui.report.ReportsScreen
import com.wopro.app.ui.location.ManageLocationsScreen
import com.wopro.app.ui.logbook.LogbookListScreen
import com.wopro.app.ui.logbook.LogbookFormScreen
import com.wopro.app.ui.block.ManageBlocksScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as WOProApp
    val repo = app.container.repository
    val encryption = app.container.encryptionManager
    val authVm = AuthViewModel(repo, encryption)
    val authFactory = AuthVMFactory(repo, encryption)
    val vmFactory = VMFactory(repo)

    val start = if (encryption.isLoggedIn()) Routes.MAIN else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {

        // ---- Auth flow ----
        composable(Routes.LOGIN) {
            LoginScreen(
                vm = authVm,
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onGoForgot = { navController.navigate(Routes.FORGOT) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                vm = authVm,
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.FORGOT) {
            ForgotPasswordScreen(
                vm = authVm,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Main (tabs) ----
        composable(Routes.MAIN) {
            MainScreen(
                onNavTo = { route -> navController.navigate(route) },
                onLogout = {
                    authVm.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        // ---- Notifications ----
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                factory = vmFactory,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Reports ----
        composable(Routes.REPORTS) {
            ReportsScreen(
                factory = vmFactory,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Logbook ----
        composable(Routes.LOGBOOK_LIST) {
            LogbookListScreen(
                factory = vmFactory,
                onNew = { navController.navigate(Routes.logbookForm()) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "logbook_form?entryId={entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.LongType; defaultValue = 0L })
        ) { entry ->
            LogbookFormScreen(
                entryId = entry.arguments?.getLong("entryId") ?: 0L,
                factory = vmFactory,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Teams ----
        composable(Routes.TEAMS_LIST) {
            TeamsListScreen(
                factory = vmFactory,
                onNew = { navController.navigate(Routes.teamForm()) },
                onEdit = { id -> navController.navigate(Routes.teamForm(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "teams_form?teamId={teamId}",
            arguments = listOf(navArgument("teamId") { type = NavType.LongType; defaultValue = 0L })
        ) { entry ->
            TeamFormScreen(
                teamId = entry.arguments?.getLong("teamId") ?: 0L,
                factory = vmFactory,
                onSaved = { navController.popBackStack() }
            )
        }

        // ---- Work Orders ----
        composable(Routes.WO_LIST) {
            WorkOrderListScreen(
                factory = vmFactory,
                onNew = { navController.navigate(Routes.woForm()) },
                onDetail = { id -> navController.navigate(Routes.woDetail(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "wo_form?woId={woId}",
            arguments = listOf(navArgument("woId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val woId = backStackEntry.arguments?.getLong("woId") ?: 0L
            WorkOrderFormScreen(
                woId = woId,
                factory = vmFactory,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "wo_detail/{woId}",
            arguments = listOf(navArgument("woId") { type = NavType.LongType })
        ) { backStackEntry ->
            val woId = backStackEntry.arguments?.getLong("woId") ?: 0L
            WorkOrderDetailScreen(
                woId = woId,
                factory = vmFactory,
                onEdit = { navController.navigate(Routes.woForm(woId)) },
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Projects ----
        composable(Routes.PROJECT_LIST) {
            ProjectListScreen(
                factory = vmFactory,
                onNew = { navController.navigate(Routes.projectForm()) },
                onDetail = { id -> navController.navigate(Routes.projectDetail(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "project_form?projectId={projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.LongType; defaultValue = 0L })
        ) { entry ->
            ProjectFormScreen(
                projectId = entry.arguments?.getLong("projectId") ?: 0L,
                factory = vmFactory,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "project_detail/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { entry ->
            ProjectDetailScreen(
                projectId = entry.arguments?.getLong("projectId") ?: 0L,
                factory = vmFactory,
                onEdit = { navController.navigate(Routes.projectForm(projectId = entry.arguments?.getLong("projectId"))) },
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Audit ----
        composable(Routes.AUDIT_LIST) {
            AuditListScreen(
                factory = vmFactory,
                onNew = { navController.navigate(Routes.auditForm()) },
                onDetail = { id -> navController.navigate(Routes.auditForm(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "audit_form?reportId={reportId}",
            arguments = listOf(navArgument("reportId") { type = NavType.LongType; defaultValue = 0L })
        ) { entry ->
            AuditFormScreen(
                reportId = entry.arguments?.getLong("reportId") ?: 0L,
                factory = vmFactory,
                onSaved = { navController.popBackStack() }
            )
        }

        // ---- Meters ----
        composable(Routes.METER_LIST) {
            MeterListScreen(
                factory = vmFactory,
                onNew = { type -> navController.navigate(Routes.meterForm(type)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "meter_form?meterType={meterType}",
            arguments = listOf(navArgument("meterType") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            MeterFormScreen(
                meterType = entry.arguments?.getString("meterType") ?: "",
                factory = vmFactory,
                onSaved = { navController.popBackStack() }
            )
        }

        // ---- AI Chat ----
        composable(Routes.AI_CHAT) {
            AiChatScreen(
                factory = vmFactory,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Locations (admin) ----
        composable(Routes.LOCATIONS) {
            ManageLocationsScreen(
                factory = vmFactory,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Blocks (admin) ----
        composable(Routes.BLOCKS) {
            ManageBlocksScreen(
                factory = vmFactory,
                onBack = { navController.popBackStack() }
            )
        }

        // ---- Settings ----
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onLogout = {
                    authVm.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onManageLocations = { navController.navigate(Routes.LOCATIONS) },
                onManageBlocks = { navController.navigate(Routes.BLOCKS) }
            )
        }
    }
}