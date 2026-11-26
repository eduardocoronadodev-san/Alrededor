package com.eduardo.alrededor.data.repository

import com.eduardo.alrededor.data.local.PlacesLocalDataSource
import com.eduardo.alrededor.data.local.SummaryLocalDataSource
import com.eduardo.alrededor.data.remote.SummaryRemoteDataSource
import com.eduardo.alrededor.domain.model.DetailDefaults
import com.eduardo.alrededor.domain.model.Place
import com.eduardo.alrededor.domain.model.PlaceSummary
import com.eduardo.alrededor.domain.repository.PlaceDetailRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PlaceDetailRepositoryImpl @Inject constructor(
    private val placesLocal: PlacesLocalDataSource,
    private val summaryLocal: SummaryLocalDataSource,
    private val summaryRemote: SummaryRemoteDataSource
) : PlaceDetailRepository {

    override suspend fun getPlace(placeId: Long): Place? = placesLocal.findById(placeId)

    override suspend fun getSummary(place: Place): PlaceSummary {
        val cached = summaryLocal.find(place.id)
        val now = System.currentTimeMillis()

        // Guardado y reciente: se usa tal cual.
        if (cached != null && now - cached.updatedAt < DetailDefaults.SUMMARY_MAX_AGE_MS) {
            return cached
        }

        return try {
            val fresh = summaryRemote.fetch(place)
            summaryLocal.save(fresh)
            fresh
        } catch (e: HttpException) {
            // 404: Wikipedia no tiene resumen de este artículo. Otros códigos: se usa lo guardado.
            if (e.code() == 404) PlaceSummary(place.id, extract = null, updatedAt = now)
            else cached ?: throw e
        } catch (e: IOException) {
            // Sin red: se usa lo guardado aunque sea viejo.
            cached ?: throw e
        }
    }
}
