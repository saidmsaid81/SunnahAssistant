package com.thesunnahrevival.sunnahassistant.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkManager private constructor(private val applicationContext: Context) {

    companion object {
        @Volatile
        private var instance: NetworkManager? = null

        fun getInstance(context: Context): NetworkManager {
            return instance ?: synchronized(this) {
                instance ?: NetworkManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val networkCapabilitiesFlow: Flow<NetworkCapabilities?> = callbackFlow {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback: ConnectivityManager.NetworkCallback

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    trySend(networkCapabilities)
                }

                override fun onLost(network: Network) {
                    trySend(null)
                }
            }
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder().build()
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork))
                }

                override fun onLost(network: Network) {
                    trySend(null)
                }
            }
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

}
