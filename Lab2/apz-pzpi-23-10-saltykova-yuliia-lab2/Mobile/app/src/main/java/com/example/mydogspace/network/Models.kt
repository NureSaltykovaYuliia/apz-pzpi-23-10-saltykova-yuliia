package com.example.mydogspace.network

import com.squareup.moshi.Json

data class UserForLoginDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

data class UserForRegistrationDto(
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "adminCode") val adminCode: String? = null
)

data class LoginResponse(
    @Json(name = "token") val token: String
)

data class GenericResponse(
    @Json(name = "message") val message: String
)

data class EventDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "type") val type: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "organizerId") val organizerId: Int,
    @Json(name = "organizerName") val organizerName: String,
    @Json(name = "participantCount") val participantCount: Int,
    @Json(name = "isJoined") val isJoined: Boolean
)

data class PartnerDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "address") val address: String,
    @Json(name = "phoneNumber") val phoneNumber: String,
    @Json(name = "website") val website: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "photoUrl") val photoUrl: String? = null
)

data class DogDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "breed") val breed: String,
    @Json(name = "dateOfBirth") val dateOfBirth: String,
    @Json(name = "description") val description: String,
    @Json(name = "photoUrl") val photoUrl: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

data class UserProfileDto(
    @Json(name = "id") val id: Int,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "photoUrl") val photoUrl: String? = null,
    @Json(name = "lastLatitude") val lastLatitude: Double? = null,
    @Json(name = "lastLongitude") val lastLongitude: Double? = null,
    @Json(name = "lastActivity") val lastActivity: String? = null,
    @Json(name = "isBlocked") val isBlocked: Boolean = false,
    @Json(name = "blockReason") val blockReason: String? = null,
    @Json(name = "isFriend") val isFriend: Boolean = false
)

data class CreateUpdateEventDto(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "startTime") val startTime: String,
    @Json(name = "endTime") val endTime: String,
    @Json(name = "type") val type: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

data class CreateUpdateDogDto(
    @Json(name = "name") val name: String,
    @Json(name = "breed") val breed: String,
    @Json(name = "dateOfBirth") val dateOfBirth: String,
    @Json(name = "description") val description: String,
    @Json(name = "photoUrl") val photoUrl: String? = null
)

data class ConversationDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "participantIds") val participantIds: List<Int>,
    @Json(name = "participantNames") val participantNames: List<String>
)

data class MessageDto(
    @Json(name = "id") val id: Int,
    @Json(name = "content") val content: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "senderId") val senderId: Int,
    @Json(name = "senderName") val senderName: String,
    @Json(name = "conversationId") val conversationId: Int
)

data class UserDto(
    @Json(name = "id") val id: Int,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String? = null
)

data class AssignDeviceDto(
    @Json(name = "dogId") val dogId: Int
)

data class SmartDeviceDto(
    @Json(name = "id") val id: Int,
    @Json(name = "deviceGuid") val deviceGuid: String,
    @Json(name = "lastLatitude") val lastLatitude: Double,
    @Json(name = "lastLongitude") val lastLongitude: Double,
    @Json(name = "batteryLevel") val batteryLevel: Double,
    @Json(name = "totalDistance") val totalDistance: Double,
    @Json(name = "dogId") val dogId: Int?,
    @Json(name = "dogName") val dogName: String?
)
