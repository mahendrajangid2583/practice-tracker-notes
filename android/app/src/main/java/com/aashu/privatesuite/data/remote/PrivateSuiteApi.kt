package com.aashu.privatesuite.data.remote

import com.aashu.privatesuite.data.remote.dto.CollectionDto
import com.aashu.privatesuite.data.remote.dto.CollectionsResponseDto
import com.aashu.privatesuite.data.remote.dto.SyncPullRequest
import com.aashu.privatesuite.data.remote.dto.SyncPullResponse
import com.aashu.privatesuite.data.remote.dto.SyncPushRequest
import com.aashu.privatesuite.data.remote.dto.SyncPushResponse
import com.aashu.privatesuite.data.remote.dto.LoginRequestDto
import com.aashu.privatesuite.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface PrivateSuiteApi {

    suspend fun getCollections(): CollectionsResponseDto

    @GET("collections/{id}")
    suspend fun getCollectionDetails(@retrofit2.http.Path("id") id: String): CollectionDto

    @GET("collections/activity")
    suspend fun getActivityLog(): List<String>

    @POST("sync/pull")
    suspend fun syncPull(@Body request: SyncPullRequest): SyncPullResponse

    @POST("sync/push")
    suspend fun syncPush(@Body request: SyncPushRequest): SyncPushResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("target-settings")
    suspend fun getTargetSettings(): com.aashu.privatesuite.data.remote.dto.TargetSettingsDto

    @PUT("target-settings")
    suspend fun updateTargetSettings(@Body request: com.aashu.privatesuite.data.remote.dto.TargetSettingsDto): com.aashu.privatesuite.data.remote.dto.TargetSettingsDto

    @GET("daily-targets")
    suspend fun getDailyTargets(@Query("date") date: String): List<com.aashu.privatesuite.data.remote.dto.TaskDto>
}

