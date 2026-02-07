package com.mangrove.bakajuan

data class BakajuanSpeciesData(
    val speciesID: String? = null,
    val localName: String? = null,
    val scientificName: String? = null,
    val zone: String? = null,
    val characteristics: String? = null,
    val height: String? = null,
    val circumference: String? = null,
    val estimatedCarbonSequestered: String? = null,
    val estimatedAge: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val mangroveImage: String? = null,
    val mangroveImageUrl: String? = null,
    var dateCatalogued: Long? = null
)