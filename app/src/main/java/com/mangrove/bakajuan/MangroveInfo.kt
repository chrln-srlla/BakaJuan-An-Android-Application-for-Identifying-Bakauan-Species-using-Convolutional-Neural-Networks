package com.mangrove.bakajuan

data class MangroveInfo(
    val localName: String,
    val scientificName: String,
    val zone: String,
    val characteristics: String
)

val mangroveData = mapOf(
    "Bakhaw Babae" to MangroveInfo(
        localName = "Bakhaw Babae",
        scientificName = "Rhizophora mucronata",
        zone = "Mid-zone",
        characteristics = "This tree grows in the midzone, where it adapts well to muddy tidal flats. Its average height is about 8–10 m (medium-sized). Its strong prop roots, pencil-shaped seedlings, and broad pointed elliptical leaves make it easy to recognize."
    ),
    "Buta-buta" to MangroveInfo(
        localName = "Buta-buta",
        scientificName = "Excoecaria agallocha",
        zone = "Landward-zone",
        characteristics = "This species is found in the landward zone, usually along the inner edge of mangrove forests. Its average height is about 8–12 m (medium-sized). It is known for its toxic milky sap, oval leaves that turn red before falling, and smooth bark."
    ),
    "Miyapi" to MangroveInfo(
        localName = "Miyapi",
        scientificName = "Avicennia rumphiana",
        zone = "Seaward-zone",
        characteristics = "This mangrove is typically found in the seaward zone, where tidal flooding is frequent. Its average height is about 8–15 m (large-sized). It is recognized by its pencil-like roots, clusters of yellow-orange flowers, and oval salt-excreting leaves."
    ),
    "Pagatpat" to MangroveInfo(
        localName = "Pagatpat",
        scientificName = "Sonneratia alba",
        zone = "Seaward-zone",
        characteristics = "This species is commonly found along the seaward edge, near sandy or rocky shores. Its average height is about 10–15 m (large-sized). It has cone-shaped breathing roots, round leathery leaves, and large white flowers."
    ),
    "Pototan" to MangroveInfo(
        localName = "Pototan",
        scientificName = "Bruguiera cylindrica",
        zone = "Mid-zone",
        characteristics = "This species grows in the midzone of mangrove forests. Its average height is about 7–10 m (medium-sized). It is recognized by its knee-like roots, long dangling seed pods, and smooth, shiny elliptic leaves."
    )
)