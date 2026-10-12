package com.techquantum.feature.fooddelivery.di

import com.techquantum.feature.fooddelivery.data.LocalFoodRepository
import com.techquantum.feature.fooddelivery.domain.FoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FoodModule {
    @Binds @Singleton abstract fun repository(implementation: LocalFoodRepository): FoodRepository
}
