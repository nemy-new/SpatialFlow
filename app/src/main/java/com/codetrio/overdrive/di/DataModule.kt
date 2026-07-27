package com.codetrio.overdrive.di

import com.codetrio.overdrive.data.cache.FileCacheDataSource
import com.codetrio.overdrive.domain.repository.LocalCacheDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindLocalCacheDataSource(
        fileCacheDataSource: FileCacheDataSource
    ): LocalCacheDataSource
}
