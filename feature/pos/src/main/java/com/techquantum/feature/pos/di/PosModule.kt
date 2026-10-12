package com.techquantum.feature.pos.di

import com.techquantum.feature.pos.data.LocalPosRepository
import com.techquantum.feature.pos.domain.PosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PosModule {
    @Binds
    abstract fun bindPosRepository(repository: LocalPosRepository): PosRepository
}
