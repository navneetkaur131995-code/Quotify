package com.quotify.core.data.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import com.quotify.core.domain.connectivity.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

// @Singleton because the callback registration is non-trivial & we don't want multiple registrations per process.
@Singleton
class AndroidNetworkMonitor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : NetworkMonitor {
    /* callbackFlow is the canonical bridge from a callback-based API to a Flow.
    It binds the callback's lifetime to subscription: awaitClose { unregisterNetworkCallback(...) } runs when the last
    subscriber goes away (e.g. ViewModel is cleared + WhileSubscribed timeout expires).
    No manual lifecycle observer needed.
*/
        @SuppressLint("MissingPermission")
        override val isOnline: Flow<Boolean> =
            callbackFlow {
                val cm =
                    context.getSystemService(ConnectivityManager::class.java)
                        ?: run {
                            trySend(false)
                            awaitClose {}

                            return@callbackFlow
                        }

        /* Tracking a Set<Network> rather than a single boolean handles the multi-network case (user on Wi-Fi +
        cellular). The original code re-evaluated only the network that triggered the callback,
        so losing Wi-Fi while cellular was active would falsely report "offline".*/

                val validatedNetworks = mutableSetOf<Network>()
                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            // onAvailable fires before validation; capabilities callback below is authoritative.
                        }

                        override fun onLost(network: Network) {
                            validatedNetworks -= network
                            trySend(validatedNetworks.isNotEmpty())
                        }

                        // NET_CAPABILITY_VALIDATED is the difference between "has a network interface" and
                        // "can actually reach the internet". Captive portals fail validation,
                        // which is what you want — the user can't fetch quotes through a hotel login
                        // wall.
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities,
                        ) {
                            val usable =
                                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                            if (usable) {
                                validatedNetworks += network
                            } else {
                                validatedNetworks -= network
                            }

                            trySend(validatedNetworks.isNotEmpty())
                        }
                    }

                val request =
                    NetworkRequest
                        .Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()
                cm.registerNetworkCallback(request, callback)

                // Seed subscribers with the current state - don't wait for the first system event
                trySend(cm.isCurrentlyValidated())

                awaitClose { cm.unregisterNetworkCallback(callback) }
            }.conflate() // If subscriber is slow or if the system emits a burst of events,
                // only the latest value matters
                // distinctUntilChanged() - because onCapabilitiesChanged fires often (signal strength, etc.) and
                // we only care about the boolean transition.
                .distinctUntilChanged()

        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        private fun ConnectivityManager.isCurrentlyValidated(): Boolean {
            val active = activeNetwork ?: return false
            val caps = getNetworkCapabilities(active) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }
