package com.example.finalproject.database

object DatabaseContract {
    object BusesEntry {
        const val TABLE_NAME = "buses"
        const val COLUMN_ENTITY_ID = "entity_id"
        const val COLUMN_TRIP_ID = "trip_id"
        const val COLUMN_ROUTE_ID = "route_id"
    }

    object StopsEntry {
        const val TABLE_NAME = "stops"
        const val COLUMN_STOP_ID = "stop_id"
        const val COLUMN_STOP_NAME = "stop_name"
        const val COLUMN_LATITUDE = "stop_lat"
        const val COLUMN_LONGITUDE = "stop_lon"
    }

    object StopTimeEntry {
        const val TABLE_NAME = "stop_times"
        const val COLUMN_TRIP_ID = "trip_id"
        const val COLUMN_STOP_ID = "stop_id"
        const val COLUMN_STOP_SEQUENCE = "stop_sequence"
    }

    object RoutesEntry {
        const val TABLE_NAME = "routes"
        const val COLUMN_ROUTE_ID = "route_id"
        const val COLUMN_ROUTE_LONG_NAME = "route_long_name"
    }

    object FareEntry {
        const val TABLE_NAME = "fares"
        const val COLUMN_FARE_ID = "fare_id"
        const val COLUMN_PRICE = "price"
        const val COLUMN_ROUTE_ID = "route"
        const val COLUMN_SOURCE_ID = "source"
        const val COLUMN_DESTINATION_ID = "destination"
        const val COLUMN_AGENCY_ID = "agency_id"
    }

    object CombinedEntry {
        const val TABLE_NAME = "combined_data"
        const val COLUMN_ENTITY_ID = "bus_reg_number"
        const val COLUMN_ROUTE_LONG_NAME = "route_long_name"
        const val COLUMN_STOP_NAME = "stop_name"
        const val COLUMN_STOP_SEQUENCE = "stop_sequence"
    }
}
