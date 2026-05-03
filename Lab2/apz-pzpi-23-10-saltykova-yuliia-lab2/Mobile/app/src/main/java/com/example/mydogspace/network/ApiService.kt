package com.example.mydogspace.network

import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: UserForLoginDto): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: UserForRegistrationDto): GenericResponse

    @GET("api/events")
    suspend fun getEvents(): List<EventDto>

    @GET("api/events/{id}")
    suspend fun getEventById(@Path("id") id: Int): EventDto

    @POST("api/events")
    suspend fun createEvent(@Body request: CreateUpdateEventDto): EventDto

    @DELETE("api/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int)

    @POST("api/events/{id}/join")
    suspend fun joinEvent(@Path("id") id: Int)

    @POST("api/events/{id}/leave")
    suspend fun leaveEvent(@Path("id") id: Int)

    @GET("api/partners")
    suspend fun getPartners(): List<PartnerDto>

    @GET("api/partners/{id}")
    suspend fun getPartnerById(@Path("id") id: Int): PartnerDto

    @GET("api/dogs/my")
    suspend fun getDogs(): List<DogDto>

    @POST("api/dogs")
    suspend fun createDog(@Body request: CreateUpdateDogDto): DogDto

    @DELETE("api/dogs/{id}")
    suspend fun deleteDog(@Path("id") id: Int)

    @GET("api/users/profile")
    suspend fun getProfile(): UserProfileDto

    @GET("api/users/friends")
    suspend fun getFriends(): List<UserDto>

    @GET("api/conversations")
    suspend fun getConversations(): List<ConversationDto>

    @GET("api/conversations/{id}/messages")
    suspend fun getMessages(@Path("id") conversationId: Int): List<MessageDto>

    @POST("api/conversations/{id}/messages")
    suspend fun sendMessage(@Path("id") conversationId: Int, @Body content: String): MessageDto

    @POST("api/conversations/private/{targetUserId}")
    suspend fun createPrivateConversation(@Path("targetUserId") targetUserId: Int): ConversationDto

    @POST("api/SmartDevices/device/{deviceGuid}/assign")
    suspend fun assignDevice(@Path("deviceGuid") deviceGuid: String, @Body request: AssignDeviceDto): GenericResponse
}
