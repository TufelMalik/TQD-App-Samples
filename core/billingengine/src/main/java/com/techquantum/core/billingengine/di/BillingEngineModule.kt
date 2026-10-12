package com.techquantum.core.billingengine.di

import com.techquantum.core.billingengine.BillRepository
import com.techquantum.core.billingengine.domain.repository.IBillRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingEngineModule {

    @Binds
    @Singleton
    abstract fun bindBillRepository(
        impl: BillRepository
    ): IBillRepository
}
