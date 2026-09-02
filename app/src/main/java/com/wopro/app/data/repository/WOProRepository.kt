package com.wopro.app.data.repository

import com.wopro.app.data.local.AppDatabase
import com.wopro.app.data.local.AuditItemEntity
import com.wopro.app.data.local.AuditReportEntity
import com.wopro.app.data.local.ChatMessageEntity
import com.wopro.app.data.local.LocationEntity
import com.wopro.app.data.local.LogbookEntity
import com.wopro.app.data.local.MeterReadingEntity
import com.wopro.app.data.local.NotificationEntity
import com.wopro.app.data.local.ProjectEntity
import com.wopro.app.data.local.TeamEntity
import com.wopro.app.data.local.UserEntity
import com.wopro.app.data.local.WorkOrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Single source of truth for the UI. In demo mode all data lives in the
 * encrypted local Room DB, so every screen is fully functional offline.
 */
class WOProRepository(private val db: AppDatabase) {

    // ---- Users / Auth (demo) ----
    suspend fun findUserByEmail(email: String): UserEntity? = db.userDao().getByEmail(email)
    suspend fun findUserByName(name: String): UserEntity? = db.userDao().getByName(name)
    fun observeUsers(): Flow<List<UserEntity>> = db.userDao().observeAll()
    suspend fun countUsers(): Int = db.userDao().countAll()
    suspend fun createUser(user: UserEntity): Long = db.userDao().insert(user)
    suspend fun getUser(id: Long): UserEntity? = db.userDao().getById(id)

    /**
     * Seed super user (admin / admin) on first launch. Idempotent.
     */
    suspend fun seedAdminUser() {
        if (db.userDao().getByEmail(ADMIN_EMAIL) == null) {
            db.userDao().insert(
                UserEntity(name = "Admin", email = ADMIN_EMAIL, role = "Admin")
            )
        }
    }

    /**
     * Seed default locations on first launch. Idempotent.
     */
    suspend fun seedLocations() {
        if (db.locationDao().countAll() > 0) return
        DEFAULT_LOCATIONS.forEach { name ->
            db.locationDao().insert(LocationEntity(name = name))
        }
    }

    // ---- Work Orders ----
    fun observeWorkOrders(): Flow<List<WorkOrderEntity>> = db.workOrderDao().observeAll()
    fun observeWorkOrders(status: String): Flow<List<WorkOrderEntity>> = db.workOrderDao().observeByStatus(status)
    suspend fun getWorkOrder(id: Long): WorkOrderEntity? = db.workOrderDao().getById(id)

    suspend fun saveWorkOrder(wo: WorkOrderEntity) {
        if (wo.id == 0L) {
            val assignedTeamId = findTeamIdFor(wo.block, wo.roomNumber)
            val now = System.currentTimeMillis()
            val creator = wo.createdBy.ifBlank { "User" }
            val initialLog = "${fmtLog(now)} -> Created ($creator)"
            val id = db.workOrderDao().insert(
                wo.copy(
                    assignedTeamId = assignedTeamId,
                    activityLog = wo.activityLog.ifBlank { initialLog },
                    createdAt = now,
                    updatedAt = now
                )
            )
            notifyForWorkOrder(wo.copy(id = id, assignedTeamId = assignedTeamId))
        } else {
            db.workOrderDao().update(wo)
        }
    }

    suspend fun deleteWorkOrder(wo: WorkOrderEntity) = db.workOrderDao().delete(wo)
    suspend fun countWorkOrders(status: String): Int = db.workOrderDao().countByStatus(status)
    suspend fun countAllWorkOrders(): Int = db.workOrderDao().countAll()

    // ---- Alur kerja status ----
    suspend fun acceptWorkOrder(wo: WorkOrderEntity, byName: String) {
        val now = System.currentTimeMillis()
        val log = wo.activityLog + "\n${fmtLog(now)} -> Accepted ($byName)"
        db.workOrderDao().update(
            wo.copy(status = "Accepted", acceptedBy = byName, acceptedAt = now, updatedAt = now, activityLog = log)
        )
    }

    suspend fun setPending(wo: WorkOrderEntity, reason: String) {
        val now = System.currentTimeMillis()
        val log = wo.activityLog + "\n${fmtLog(now)} -> Pending: $reason"
        db.workOrderDao().update(
            wo.copy(status = "Pending", pendingReason = reason, updatedAt = now, activityLog = log)
        )
    }

    suspend fun setDone(wo: WorkOrderEntity, photoUri: String?) {
        val now = System.currentTimeMillis()
        val log = wo.activityLog + "\n${fmtLog(now)} -> Done: photo attached"
        db.workOrderDao().update(
            wo.copy(status = "Done", donePhotoUri = photoUri, doneAt = now, updatedAt = now, activityLog = log)
        )
    }

    suspend fun resumeWorkOrder(wo: WorkOrderEntity) {
        val now = System.currentTimeMillis()
        val log = wo.activityLog + "\n${fmtLog(now)} -> Resumed to Accepted"
        db.workOrderDao().update(wo.copy(status = "Accepted", updatedAt = now, activityLog = log))
    }

    // ---- Export laporan ----
    suspend fun exportWorkOrders(status: String? = null, room: Int = 0): List<WorkOrderEntity> {
        val all = if (status == null || status == "All") {
            db.workOrderDao().listAll()
        } else {
            db.workOrderDao().listByStatus(status)
        }
        return if (room > 0) all.filter { it.roomNumber == room } else all
    }

    /** Riwayat work order untuk kamar tertentu. */
    suspend fun getRoomHistory(block: String, room: Int): List<WorkOrderEntity> {
        if (room <= 0 || block.isBlank()) return emptyList()
        return db.workOrderDao().listByRoom(block.trim().uppercase(), room)
    }

    /**
     * Assign team & push notification to every member whose team covers the
     * block + room range. Works offline (local DB).
     */
    private suspend fun notifyForWorkOrder(wo: WorkOrderEntity) {
        if (wo.block.isBlank()) return
        val block = wo.block.trim().uppercase()
        val teams = db.teamDao().observeAll().first()
        val team = teams.firstOrNull { t ->
            t.block.equals(block, ignoreCase = true) &&
                wo.roomNumber >= t.roomStart && wo.roomNumber <= t.roomEnd
        } ?: return

        val members = team.memberEmails.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val title = "Tugas Baru: ${wo.title}"
        val body = "Tim ${team.name} (Blok $block) menerima work order ruang $block-${wo.roomNumber}."
        if (members.isEmpty()) {
            addNotification(
                NotificationEntity(title = title, body = body, teamName = team.name, woId = wo.id)
            )
        } else {
            members.forEach { email ->
                addNotification(
                    NotificationEntity(
                        title = title,
                        body = body,
                        teamName = team.name,
                        woId = wo.id,
                        targetEmail = email
                    )
                )
            }
        }
    }

    private suspend fun findTeamIdFor(block: String, room: Int): Long {
        if (block.isBlank() || room <= 0) return 0
        val b = block.trim().uppercase()
        return db.teamDao().observeAll().first()
            .firstOrNull { t -> t.block.equals(b, ignoreCase = true) && room >= t.roomStart && room <= t.roomEnd }
            ?.id ?: 0
    }

    // ---- Projects ----
    fun observeProjects(): Flow<List<ProjectEntity>> = db.projectDao().observeAll()
    suspend fun getProject(id: Long): ProjectEntity? = db.projectDao().getById(id)
    suspend fun saveProject(p: ProjectEntity) {
        if (p.id == 0L) db.projectDao().insert(p) else db.projectDao().update(p)
    }
    suspend fun deleteProject(p: ProjectEntity) = db.projectDao().delete(p)

    // ---- Audit ----
    fun observeAuditReports(): Flow<List<AuditReportEntity>> = db.auditDao().observeReports()
    suspend fun getAuditReport(id: Long): AuditReportEntity? = db.auditDao().getReport(id)
    fun observeAuditItems(reportId: Long): Flow<List<AuditItemEntity>> = db.auditDao().observeItems(reportId)
    suspend fun saveAuditReport(
        report: AuditReportEntity,
        items: List<AuditItemEntity>
    ): Long {
        val reportId = if (report.id == 0L) {
            db.auditDao().insertReport(report)
        } else {
            db.auditDao().updateReport(report)
            report.id
        }
        db.auditDao().deleteItems(reportId)
        db.auditDao().insertItems(items.map { it.copy(reportId = reportId) })
        return reportId
    }

    // ---- Meters ----
    fun observeMeters(): Flow<List<MeterReadingEntity>> = db.meterDao().observeAll()
    fun observeMeters(type: String): Flow<List<MeterReadingEntity>> = db.meterDao().observeByType(type)
    suspend fun addMeterReading(m: MeterReadingEntity): Long = db.meterDao().insert(m)
    suspend fun deleteMeterReading(m: MeterReadingEntity) = db.meterDao().delete(m)
    suspend fun distinctMeterTypes(): List<String> = db.meterDao().distinctTypes()

    // ---- AI Chat ----
    fun observeChat(): Flow<List<ChatMessageEntity>> = db.chatDao().observeAll()
    suspend fun addChatMessage(m: ChatMessageEntity): Long = db.chatDao().insert(m)
    suspend fun clearChat() = db.chatDao().clear()

    // ---- Teams (Kelompok) ----
    fun observeTeams(): Flow<List<TeamEntity>> = db.teamDao().observeAll()
    suspend fun getTeam(id: Long): TeamEntity? = db.teamDao().getById(id)
    suspend fun saveTeam(t: TeamEntity) {
        if (t.id == 0L) db.teamDao().insert(t) else db.teamDao().update(t)
    }
    suspend fun deleteTeam(t: TeamEntity) = db.teamDao().delete(t)

    // ---- Locations (Lokasi) ----
    fun observeLocations(): Flow<List<LocationEntity>> = db.locationDao().observeAll()
    suspend fun getLocation(id: Long): LocationEntity? = db.locationDao().getById(id)
    suspend fun addLocation(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return 0
        val existing = db.locationDao().getByName(trimmed)
        if (existing != null) return existing.id
        return db.locationDao().insert(LocationEntity(name = trimmed))
    }
    suspend fun deleteLocation(l: LocationEntity) = db.locationDao().delete(l)

    // ---- Logbook ----
    fun observeLogbook(): Flow<List<LogbookEntity>> = db.logbookDao().observeAll()
    suspend fun getLogbookEntry(id: Long): LogbookEntity? = db.logbookDao().getById(id)
    suspend fun saveLogbookEntry(e: LogbookEntity) {
        if (e.id == 0L) db.logbookDao().insert(e) else db.logbookDao().update(e)
    }
    suspend fun deleteLogbookEntry(e: LogbookEntity) = db.logbookDao().delete(e)

    /** Format timestamp untuk activity log: "02 Sep 2026, 18:31". */
    private fun fmtLog(epoch: Long): String {
        return try {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.ENGLISH)
            sdf.format(java.util.Date(epoch))
        } catch (t: Throwable) {
            epoch.toString()
        }
    }

    // ---- Notifications ----
    fun observeNotifications(): Flow<List<NotificationEntity>> = db.notificationDao().observeAll()
    fun observeUnread(): Flow<List<NotificationEntity>> = db.notificationDao().observeUnread()
    suspend fun addNotification(n: NotificationEntity): Long = db.notificationDao().insert(n)
    suspend fun markNotificationRead(id: Long) = db.notificationDao().markRead(id)
    suspend fun markAllNotificationsRead() = db.notificationDao().markAllRead()
    suspend fun clearNotifications() = db.notificationDao().clear()

    companion object {
        const val ADMIN_EMAIL = "admin"

        /** Default lokasi yang di-seed saat pertama install (admin bisa tambah/hapus). */
        val DEFAULT_LOCATIONS = listOf(
            "Kamar", "Lobby", "Kolam Renang", "Restoran", "Spa",
            "Gym", "Parkir", "Ruang Rapat", "Kantor", "Dapur",
            "Koridor", "Atap", "Ruang Mesin", "Luar Gedung"
        )
    }
}
