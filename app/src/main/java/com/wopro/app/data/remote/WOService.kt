package com.wopro.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Minimal REST contract for a WO Pro backend.
 * Implement server-side; the demo build does not call these.
 */
interface WOService {

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): AuthResponse

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: Map<String, String>): AuthResponse

    @GET("workorders")
    suspend fun listWorkOrders(): List<Any>

    @GET("workorders/{id}")
    suspend fun getWorkOrder(@Path("id") id: Long): Any

    @POST("workorders")
    suspend fun createWorkOrder(@Body body: Any): Any
}

data class AuthResponse(
    val token: String? = null,
    val user: RemoteUser? = null,
    val message: String? = null
)

data class RemoteUser(
    val id: Long = 0,
    val name: String = "",
    val email: String = ""
)
