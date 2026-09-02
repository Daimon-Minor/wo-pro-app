package com.wopro.app.data.repository

import com.wopro.app.data.local.AppDatabase
import com.wopro.app.data.local.AuditItemEntity
import com.wopro.app.data.local.AuditReportEntity
import com.wopro.app.data.local.ChatMessageEntity
import com.wopro.app.data.local.MeterReadingEntity
import com.wopro.app.data.local.ProjectEntity
import com.wopro.app.data.local.UserEntity
import com.wopro.app.data.local.WorkOrderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the UI. In demo mode all data lives in the
 * encrypted local Room DB, so every screen is fully functional offline.
 */
class WOProRepository(private val db: AppDatabase) {

    // ---- Users / Auth (demo) ----
    suspend fun findUserByEmail(email: String): UserEntity? = db.userDao().getByEmail(email)
    suspend fun createUser(user: UserEntity): Long = db.userDao().insert(user)
    suspend fun getUser(id: Long): UserEntity? = db.userDao().getById(id)

    // ---- Work Orders ----
    fun observeWorkOrders(): Flow<List<WorkOrderEntity>> = db.workOrderDao().observeAll()
    fun observeWorkOrders(status: String): Flow<List<WorkOrderEntity>> = db.workOrderDao().observeByStatus(status)
    suspend fun getWorkOrder(id: Long): WorkOrderEntity? = db.workOrderDao().getById(id)
    suspend fun saveWorkOrder(wo: WorkOrderEntity) {
        if (wo.id == 0L) db.workOrderDao().insert(wo) else db.workOrderDao().update(wo)
    }
    suspend fun deleteWorkOrder(wo: WorkOrderEntity) = db.workOrderDao().delete(wo)
    suspend fun countWorkOrders(status: String): Int = db.workOrderDao().countByStatus(status)
    suspend fun countAllWorkOrders(): Int = db.workOrderDao().countAll()

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
}
