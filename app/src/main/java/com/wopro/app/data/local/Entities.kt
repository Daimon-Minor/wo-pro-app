package com.wopro.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** User profile persisted locally (never stores plaintext password). */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: String = "Engineer",
    val avatarUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String,
    val priority: String = "Medium", // Low / Medium / High / Critical
    val status: String = "Open",     // Open / In Progress / Completed / Overdue
    val location: String = "",
    val assignedTo: String = "",
    val dueDate: Long? = null,
    val photoUri: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val location: String = "",
    val status: String = "Active",   // Active / On Hold / Completed
    val budget: Double = 0.0,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_reports")
data class AuditReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val property: String,
    val auditor: String,
    val status: String = "Draft",    // Draft / Submitted / Approved / Overdue
    val findingCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_items")
data class AuditItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val checkItem: String,
    val result: String = "N/A",      // Pass / Fail / N/A
    val notes: String = "",
    val photoUri: String? = null
)

@Entity(tableName = "meter_readings")
data class MeterReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val meterType: String,           // Chiller / Freezer / Heat Pump / Water Tank / Fuel / Gas / KWH
    val meterName: String,
    val reading: Double,
    val unit: String = "",
    val tariff: Double = 0.0,
    val readingDate: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,                // "user" / "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
