package com.eduardo.alrededor.ui.explore.map

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

// Texto de atribución que pide Geoapify (junto con el de OpenStreetMap)
private const val GEOAPIFY_COPYRIGHT =
    "Powered by Geoapify | © OpenMapTiles © OpenStreetMap contributors"

class GeoapifyTileSource(
    private val apiKey: String,
    style: String = "osm-bright"
) : OnlineTileSourceBase(
    "Geoapify-$style",
    0,                                              // zoom mínimo
    20,                                             // zoom máximo
    256,                                            // tamaño del mosaico en píxeles
    ".png",
    arrayOf("https://maps.geoapify.com/v1/tile/$style/"),
    GEOAPIFY_COPYRIGHT
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl +
            MapTileIndex.getZoom(pMapTileIndex) + "/" +
            MapTileIndex.getX(pMapTileIndex) + "/" +
            MapTileIndex.getY(pMapTileIndex) + ".png" +
            "?apiKey=" + apiKey
}
