package com.example.spoonacularapi.data


import android.Manifest
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkStatusManager: NetworkStatusManager
) {
    fun isInternetAvailable(): Boolean {
        return networkStatusManager.isOnline.value
    }

    fun hasNetworkPermission(): Boolean {
        return try {
            context.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
}