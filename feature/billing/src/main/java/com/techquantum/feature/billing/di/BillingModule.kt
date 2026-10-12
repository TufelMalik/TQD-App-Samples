package com.techquantum.feature.billing.di

import com.techquantum.feature.billing.data.LocalBillingRepository
import com.techquantum.feature.billing.domain.BillingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    @Singleton
    abstract fun bindBillingRepository(repository: LocalBillingRepository): BillingRepository
}
