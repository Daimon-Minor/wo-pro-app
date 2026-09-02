package com.wopro.app.ui.navigation

/** Navigation destinations. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
    const val OTP = "otp"
    const val MAIN = "main"
    const val WO_LIST = "wo_list"
    const val WO_FORM = "wo_form?woId={woId}"
    const val WO_DETAIL = "wo_detail/{woId}"
    const val PROJECT_LIST = "project_list"
    const val PROJECT_FORM = "project_form?projectId={projectId}"
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    const val AUDIT_LIST = "audit_list"
    const val AUDIT_FORM = "audit_form?reportId={reportId}"
    const val METER_LIST = "meter_list"
    const val METER_FORM = "meter_form?meterType={meterType}"
    const val AI_CHAT = "ai_chat"
    const val SETTINGS = "settings"
    const val TEAMS_LIST = "teams_list"
    const val TEAMS_FORM = "teams_form?teamId={teamId}"
    const val NOTIFICATIONS = "notifications"

    fun woForm(woId: Long? = null) = "wo_form?woId=${woId ?: 0}"
    fun woDetail(woId: Long) = "wo_detail/$woId"
    fun projectForm(projectId: Long? = null) = "project_form?projectId=${projectId ?: 0}"
    fun projectDetail(projectId: Long) = "project_detail/$projectId"
    fun auditForm(reportId: Long? = null) = "audit_form?reportId=${reportId ?: 0}"
    fun meterForm(meterType: String? = null) = "meter_form?meterType=${meterType ?: ""}"
    fun teamForm(teamId: Long? = null) = "teams_form?teamId=${teamId ?: 0}"
}

/** Bottom navigation tabs inside Main. */
object Tabs {
    const val HOME = "tab_home"
    const val WOS = "tab_wos"
    const val AUDIT = "tab_audit"
    const val METERS = "tab_meters"
    const val MORE = "tab_more"
}
