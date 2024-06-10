package com.example.finalproject.database

data class Buses(
    val entityId: String,
    val tripId: String,
    val routeId: String
)

data class Stops(
    val stopId: String,
    val stopName: String,
    val latitude: Double,
    val longitude: Double
)

data class StopTime(
    val tripId: String,
    val stopId: String,
    val stopSequence: Int
)

data class Route(
    val routeId: String,
    val routeLongName: String
)

data class Fare(
    val fareId: String,
    val price: String,
    val routeId: String,
    val sourceId: String,
    val destinationId: String,
    val agencyId: String
)

data class Combined(
    val entityId: String,
    val routeName: String,
    val stopName: String,
    val stopSequence: Int
)
