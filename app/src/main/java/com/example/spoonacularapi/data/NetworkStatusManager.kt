package com.example.spoonacularapi.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateNetworkStatus()
        }
    }

    init {
        registerNetworkReceiver()
        updateNetworkStatus()
    }

    fun updateNetworkStatus() {
        _isOnline.value = checkInternetAvailability()
    }

    private fun checkInternetAvailability(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.isConnectedOrConnecting == true
            }
        } catch (e: Exception) {
            true
        }
    }

    private fun registerNetworkReceiver() {
        try {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(connectivityReceiver, filter)
        } catch (e: Exception) {
        }
    }

    fun unregisterReceiver() {
        try {
            context.unregisterReceiver(connectivityReceiver)
        } catch (e: Exception) {
        }
    }
}