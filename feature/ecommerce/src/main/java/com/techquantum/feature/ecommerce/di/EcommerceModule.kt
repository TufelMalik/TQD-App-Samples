package com.techquantum.feature.ecommerce.di

import com.techquantum.feature.ecommerce.data.InMemoryEcommerceRepository
import com.techquantum.feature.ecommerce.domain.EcommerceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EcommerceModule {
    @Binds
    @Singleton
    abstract fun bindEcommerceRepository(repository: InMemoryEcommerceRepository): EcommerceRepository
}
