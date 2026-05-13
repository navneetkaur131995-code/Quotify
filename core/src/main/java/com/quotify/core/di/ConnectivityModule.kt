package com.quotify.core.di

import com.quotify.core.data.network.AndroidNetworkMonitor
import com.quotify.core.domain.connectivity.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {
    @Binds
    @Singleton
    abstract fun bindsNetworkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor
}
