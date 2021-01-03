package com.idirin.idceptor.models.network

data class AppInitRequest(
        val operatingSystem: String,
        val name: String?,
        val environment: String?,
        val version: String,
        val buildNumber: String,
        val device: DeviceModel
)

data class DeviceModel(
        val name: String,
        val userFriendlyName: String,
        val uuid: String,
        val operatingSystem: String,
        val osVersion: String
)