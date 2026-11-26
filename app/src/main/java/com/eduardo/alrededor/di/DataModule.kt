package com.eduardo.alrededor.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.eduardo.alrededor.data.repository.AndroidSpeechController
import com.eduardo.alrededor.data.repository.FusedLocationRepository
import com.eduardo.alrededor.data.repository.LocationPermissionRepositoryImpl
import com.eduardo.alrededor.data.repository.PlaceDetailRepositoryImpl
import com.eduardo.alrededor.data.repository.PlacesRepositoryImpl
import com.eduardo.alrededor.data.repository.SavedDataRepositoryImpl
import com.eduardo.alrededor.data.repository.SensorOrientationRepository
import com.eduardo.alrededor.data.repository.SettingsRepositoryImpl
import com.eduardo.alrededor.domain.repository.LocationPermissionRepository
import com.eduardo.alrededor.domain.repository.LocationRepository
import com.eduardo.alrededor.domain.repository.OrientationRepository
import com.eduardo.alrededor.domain.repository.PlaceDetailRepository
import com.eduardo.alrededor.domain.repository.PlacesRepository
import com.eduardo.alrededor.domain.repository.SavedDataRepository
import com.eduardo.alrededor.domain.repository.SettingsRepository
import com.eduardo.alrededor.domain.repository.SpeechController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindLocationPermissionRepository(
        impl: LocationPermissionRepositoryImpl
    ): LocationPermissionRepository

    @Binds @Singleton
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl): PlacesRepository

    @Binds @Singleton
    abstract fun bindLocationRepository(impl: FusedLocationRepository): LocationRepository

    @Binds @Singleton
    abstract fun bindOrientationRepository(impl: SensorOrientationRepository): OrientationRepository

    @Binds @Singleton
    abstract fun bindPlaceDetailRepository(
        impl: PlaceDetailRepositoryImpl
    ): PlaceDetailRepository

    @Binds
    abstract fun bindSpeechController(
        impl: AndroidSpeechController
    ): SpeechController

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindSavedDataRepository(impl: SavedDataRepositoryImpl): SavedDataRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("ajustes") }
    )
}
