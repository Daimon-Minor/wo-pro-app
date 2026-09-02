package com.wopro.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE lower(name) = lower(:name) LIMIT 1")
    suspend fun getByName(name: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WorkOrderEntity?

    @Query("SELECT * FROM work_orders WHERE status = :status ORDER BY updatedAt DESC")
    fun observeByStatus(status: String): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE status = :status")
    suspend fun listByStatus(status: String): List<WorkOrderEntity>

    @Query("SELECT * FROM work_orders ORDER BY updatedAt DESC")
    suspend fun listAll(): List<WorkOrderEntity>

    @Query("SELECT * FROM work_orders WHERE block = :block AND roomNumber = :room ORDER BY createdAt DESC")
    suspend fun listByRoom(block: String, room: Int): List<WorkOrderEntity>

    @Query("SELECT COUNT(*) FROM work_orders WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM work_orders")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wo: WorkOrderEntity): Long

    @Update
    suspend fun update(wo: WorkOrderEntity)

    @Delete
    suspend fun delete(wo: WorkOrderEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: ProjectEntity): Long

    @Update
    suspend fun update(p: ProjectEntity)

    @Delete
    suspend fun delete(p: ProjectEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_reports ORDER BY createdAt DESC")
    fun observeReports(): Flow<List<AuditReportEntity>>

    @Query("SELECT * FROM audit_reports WHERE id = :id LIMIT 1")
    suspend fun getReport(id: Long): AuditReportEntity?

    @Query("SELECT * FROM audit_items WHERE reportId = :reportId ORDER BY id ASC")
    fun observeItems(reportId: Long): Flow<List<AuditItemEntity>>

    @Query("SELECT * FROM audit_items WHERE reportId = :reportId ORDER BY id ASC")
    suspend fun getItems(reportId: Long): List<AuditItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(r: AuditReportEntity): Long

    @Update
    suspend fun updateReport(r: AuditReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<AuditItemEntity>)

    @Query("DELETE FROM audit_items WHERE reportId = :reportId")
    suspend fun deleteItems(reportId: Long)
}

@Dao
interface MeterDao {
    @Query("SELECT * FROM meter_readings ORDER BY readingDate DESC")
    fun observeAll(): Flow<List<MeterReadingEntity>>

    @Query("SELECT * FROM meter_readings WHERE meterType = :type ORDER BY readingDate DESC")
    fun observeByType(type: String): Flow<List<MeterReadingEntity>>

    @Query("SELECT DISTINCT meterType FROM meter_readings ORDER BY meterType ASC")
    suspend fun distinctTypes(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(m: MeterReadingEntity): Long

    @Delete
    suspend fun delete(m: MeterReadingEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(m: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clear()
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY block ASC, name ASC")
    fun observeAll(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: TeamEntity): Long

    @Update
    suspend fun update(t: TeamEntity)

    @Delete
    suspend fun delete(t: TeamEntity)
}

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks ORDER BY name ASC")
    fun observeAll(): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blocks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BlockEntity?

    @Query("SELECT * FROM blocks WHERE lower(name) = lower(:name) LIMIT 1")
    suspend fun getByName(name: String): BlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(b: BlockEntity): Long

    @Update
    suspend fun update(b: BlockEntity)

    @Delete
    suspend fun delete(b: BlockEntity)

    @Query("SELECT COUNT(*) FROM blocks")
    suspend fun countAll(): Int
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE read = 0 ORDER BY createdAt DESC")
    fun observeUnread(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(n: NotificationEntity): Long

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications")
    suspend fun clear()
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun observeAll(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LocationEntity?

    @Query("SELECT * FROM locations WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(l: LocationEntity): Long

    @Update
    suspend fun update(l: LocationEntity)

    @Delete
    suspend fun delete(l: LocationEntity)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun countAll(): Int
}

@Dao
interface LogbookDao {
    @Query("SELECT * FROM logbook_entries ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<LogbookEntity>>

    @Query("SELECT * FROM logbook_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LogbookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: LogbookEntity): Long

    @Update
    suspend fun update(e: LogbookEntity)

    @Delete
    suspend fun delete(e: LogbookEntity)
}
