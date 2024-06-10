package com.example.finalproject.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val mContext: Context = context
    override fun onCreate(db: SQLiteDatabase) {
        createBusesTable(db)
        createStopsTable(db)
        createStopTimesTable(db)
        createRoutesTable(db)
        createFaresTable(db)
        createCombinedDataTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    private fun createBusesTable(db: SQLiteDatabase) {
        val createBusesTable = """
        CREATE TABLE ${DatabaseContract.BusesEntry.TABLE_NAME} (
            ${DatabaseContract.BusesEntry.COLUMN_ENTITY_ID} TEXT PRIMARY KEY,
            ${DatabaseContract.BusesEntry.COLUMN_TRIP_ID} TEXT,
            ${DatabaseContract.BusesEntry.COLUMN_ROUTE_ID} TEXT
        )
    """.trimIndent()

        db.execSQL(createBusesTable)
        insertBusesData(db)
    }

    private fun insertBusesData(db: SQLiteDatabase) {
        val busesInputStream = mContext.assets.open("output.txt")
        val reader = BufferedReader(InputStreamReader(busesInputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line!!.split(",")
            if (parts.size >= 3) {
                val entityId = parts[0]
                val tripId = parts[2]
                val routeId = tripId.split("_")[0]

                val values = ContentValues().apply {
                    put(DatabaseContract.BusesEntry.COLUMN_ENTITY_ID, entityId)
                    put(DatabaseContract.BusesEntry.COLUMN_TRIP_ID, tripId)
                    put(DatabaseContract.BusesEntry.COLUMN_ROUTE_ID, routeId)
                }
                db.insert(DatabaseContract.BusesEntry.TABLE_NAME, null, values)
            } else {
                Log.e("InsertBusesData", "Invalid data format: $line")
            }
        }
        reader.close()
    }


    private fun createStopsTable(db: SQLiteDatabase) {
        val createStopsTable = """
        CREATE TABLE ${DatabaseContract.StopsEntry.TABLE_NAME} (
            ${DatabaseContract.StopsEntry.COLUMN_STOP_ID} TEXT PRIMARY KEY,
            ${DatabaseContract.StopsEntry.COLUMN_STOP_NAME} TEXT,
            ${DatabaseContract.StopsEntry.COLUMN_LATITUDE} REAL,
            ${DatabaseContract.StopsEntry.COLUMN_LONGITUDE} REAL
        )
    """.trimIndent()

        db.execSQL(createStopsTable)
        insertStopsData(db)
    }

    private fun insertStopsData(db: SQLiteDatabase) {
        val stopsInputStream = mContext.assets.open("stops.txt")
        val reader = BufferedReader(InputStreamReader(stopsInputStream))
        reader.readLine()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line!!.split(",")
            val values = ContentValues().apply {
                put(DatabaseContract.StopsEntry.COLUMN_STOP_ID, parts[1])
                put(DatabaseContract.StopsEntry.COLUMN_STOP_NAME, parts[4])
                put(DatabaseContract.StopsEntry.COLUMN_LATITUDE, parts[2].toDouble()) // Assuming latitude is in index 2
                put(DatabaseContract.StopsEntry.COLUMN_LONGITUDE, parts[3].toDouble()) // Assuming longitude is in index 3
            }
            db.insert(DatabaseContract.StopsEntry.TABLE_NAME, null, values)
        }
        reader.close()
    }

//    fun getAllStopsFromDatabase(): List<Stops> {
//        val stopsList = mutableListOf<Stops>()
//        val db = this.readableDatabase
//
//        val query = "SELECT * FROM ${DatabaseContract.StopsEntry.TABLE_NAME}"
//        val cursor = db.rawQuery(query, null)
//
//        cursor.use {
//            val stopIdIndex = it.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_STOP_ID)
//            val stopNameIndex = it.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_STOP_NAME)
//            val latitudeIndex = it.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_LATITUDE)
//            val longitudeIndex = it.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_LONGITUDE)
//
//            while (it.moveToNext()) {
//                val stopId = if (stopIdIndex != -1) it.getString(stopIdIndex) else ""
//                val stopName = if (stopNameIndex != -1) it.getString(stopNameIndex) else ""
//                val latitude = if (latitudeIndex != -1) it.getDouble(latitudeIndex) else 0.0
//                val longitude = if (longitudeIndex != -1) it.getDouble(longitudeIndex) else 0.0
//
//                val stop = Stops(stopId, stopName, latitude, longitude)
//                stopsList.add(stop)
//            }
//        }
//
//        return stopsList
//    }


    private fun createStopTimesTable(db: SQLiteDatabase) {
        val createStopTimesTable = """
            CREATE TABLE ${DatabaseContract.StopTimeEntry.TABLE_NAME} (
                ${DatabaseContract.StopTimeEntry.COLUMN_TRIP_ID} TEXT,
                ${DatabaseContract.StopTimeEntry.COLUMN_STOP_ID} TEXT,
                ${DatabaseContract.StopTimeEntry.COLUMN_STOP_SEQUENCE} INTEGER,
                PRIMARY KEY (${DatabaseContract.StopTimeEntry.COLUMN_TRIP_ID}, ${DatabaseContract.StopTimeEntry.COLUMN_STOP_SEQUENCE})
            )
        """.trimIndent()

        db.execSQL(createStopTimesTable)
        insertStopTimesData(db)
    }

    private fun insertStopTimesData(db: SQLiteDatabase) {
        val stopTimesInputStream = mContext.assets.open("stop_times.txt")
        val reader = BufferedReader(InputStreamReader(stopTimesInputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line!!.split(",")
            parts[4].toIntOrNull() ?: continue
            val values = ContentValues().apply {
                put(DatabaseContract.StopTimeEntry.COLUMN_TRIP_ID, parts[0])
                put(DatabaseContract.StopTimeEntry.COLUMN_STOP_ID, parts[3])
                put(DatabaseContract.StopTimeEntry.COLUMN_STOP_SEQUENCE, parts[4].toInt())
            }
            db.insert(DatabaseContract.StopTimeEntry.TABLE_NAME, null, values)
        }
        reader.close()
    }

    private fun createRoutesTable(db: SQLiteDatabase) {
        val createRoutesTable = """
            CREATE TABLE ${DatabaseContract.RoutesEntry.TABLE_NAME} (
                ${DatabaseContract.RoutesEntry.COLUMN_ROUTE_ID} TEXT PRIMARY KEY,
                ${DatabaseContract.RoutesEntry.COLUMN_ROUTE_LONG_NAME} TEXT
            )
        """.trimIndent()

        db.execSQL(createRoutesTable)
        insertRoutesData(db)
    }

    private fun insertRoutesData(db: SQLiteDatabase) {
        val routesInputStream = mContext.assets.open("routes.txt")
        val reader = BufferedReader(InputStreamReader(routesInputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line!!.split(",")
            val values = ContentValues().apply {
                put(DatabaseContract.RoutesEntry.COLUMN_ROUTE_ID, parts[1])
                put(DatabaseContract.RoutesEntry.COLUMN_ROUTE_LONG_NAME, parts[2])
            }
            db.insert(DatabaseContract.RoutesEntry.TABLE_NAME, null, values)
        }
        reader.close()
    }

    private fun createFaresTable(db: SQLiteDatabase) {
        val createFaresTable = """
        CREATE TABLE ${DatabaseContract.FareEntry.TABLE_NAME} (
            ${DatabaseContract.FareEntry.COLUMN_FARE_ID} TEXT PRIMARY KEY,
            ${DatabaseContract.FareEntry.COLUMN_PRICE} TEXT,
            ${DatabaseContract.FareEntry.COLUMN_ROUTE_ID} TEXT,
            ${DatabaseContract.FareEntry.COLUMN_SOURCE_ID} TEXT,
            ${DatabaseContract.FareEntry.COLUMN_DESTINATION_ID} TEXT,
            ${DatabaseContract.FareEntry.COLUMN_AGENCY_ID} TEXT
        )
    """.trimIndent()

        db.execSQL(createFaresTable)
        insertFaresData(db)
    }

    private fun insertFaresData(db: SQLiteDatabase) {
        val faresInputStream = mContext.assets.open("fare_attributes.txt")
        val reader = BufferedReader(InputStreamReader(faresInputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line!!.split(",")
            if (parts.size >= 2) {
                val fareId = parts[0]
                val price = parts[1]

                val fareIdParts = fareId.split("_")
                if (fareIdParts.size >= 4) {
                    val routeId = fareIdParts[1]
                    val sourceId = fareIdParts[2]
                    val destinationId = fareIdParts[3]
                    val agencyId = fareIdParts[0]

                    val values = ContentValues().apply {
                        put(DatabaseContract.FareEntry.COLUMN_FARE_ID, fareId)
                        put(DatabaseContract.FareEntry.COLUMN_PRICE, price)
                        put(DatabaseContract.FareEntry.COLUMN_ROUTE_ID, routeId)
                        put(DatabaseContract.FareEntry.COLUMN_SOURCE_ID, sourceId)
                        put(DatabaseContract.FareEntry.COLUMN_DESTINATION_ID, destinationId)
                        put(DatabaseContract.FareEntry.COLUMN_AGENCY_ID, agencyId)
                    }
                    db.insert(DatabaseContract.FareEntry.TABLE_NAME, null, values)
                } else {
                    Log.e("InsertFaresData", "Invalid fare_id format: $fareId")
                }
            } else {
                Log.e("InsertFaresData", "Invalid data format: $line")
            }
        }
        reader.close()
    }


    private fun createCombinedDataTable(db: SQLiteDatabase) {
        val createCombinedDataTable = """
        CREATE TABLE ${DatabaseContract.CombinedEntry.TABLE_NAME} (
            ${DatabaseContract.CombinedEntry.COLUMN_ENTITY_ID} TEXT,
            ${DatabaseContract.CombinedEntry.COLUMN_ROUTE_LONG_NAME} TEXT,
            ${DatabaseContract.CombinedEntry.COLUMN_STOP_NAME} TEXT,
            ${DatabaseContract.CombinedEntry.COLUMN_STOP_SEQUENCE} INTEGER
        )
    """.trimIndent()

        db.execSQL(createCombinedDataTable)
        insertCombinedData(db)
    }

    private fun insertCombinedData(db: SQLiteDatabase) {
        val insertCombinedDataQuery = """
        INSERT INTO ${DatabaseContract.CombinedEntry.TABLE_NAME} 
        SELECT 
            ${DatabaseContract.BusesEntry.COLUMN_ENTITY_ID}, 
            ${DatabaseContract.RoutesEntry.COLUMN_ROUTE_LONG_NAME}, 
            ${DatabaseContract.StopsEntry.COLUMN_STOP_NAME}, 
            ${DatabaseContract.StopTimeEntry.COLUMN_STOP_SEQUENCE} 
        FROM 
            ${DatabaseContract.BusesEntry.TABLE_NAME} 
        JOIN 
            ${DatabaseContract.RoutesEntry.TABLE_NAME} 
        ON 
            ${DatabaseContract.BusesEntry.TABLE_NAME}.${DatabaseContract.BusesEntry.COLUMN_ROUTE_ID} = 
            ${DatabaseContract.RoutesEntry.TABLE_NAME}.${DatabaseContract.RoutesEntry.COLUMN_ROUTE_ID} 
        JOIN 
            ${DatabaseContract.StopTimeEntry.TABLE_NAME} 
        ON 
            ${DatabaseContract.BusesEntry.TABLE_NAME}.${DatabaseContract.BusesEntry.COLUMN_TRIP_ID} = 
            ${DatabaseContract.StopTimeEntry.TABLE_NAME}.${DatabaseContract.StopTimeEntry.COLUMN_TRIP_ID} 
        JOIN 
            ${DatabaseContract.StopsEntry.TABLE_NAME} 
        ON 
            ${DatabaseContract.StopTimeEntry.TABLE_NAME}.${DatabaseContract.StopTimeEntry.COLUMN_STOP_ID} = 
            ${DatabaseContract.StopsEntry.TABLE_NAME}.${DatabaseContract.StopsEntry.COLUMN_STOP_ID}
    """.trimIndent()

        db.execSQL(insertCombinedDataQuery)
    }

    fun getBusStops(): HashMap<String, HashMap<String, ArrayList<String>>> {
        val db = readableDatabase
        val cursor = db.query(
            DatabaseContract.CombinedEntry.TABLE_NAME,
            arrayOf(
                DatabaseContract.CombinedEntry.COLUMN_ENTITY_ID,
                DatabaseContract.CombinedEntry.COLUMN_ROUTE_LONG_NAME,
                DatabaseContract.CombinedEntry.COLUMN_STOP_NAME
            ),
            null,
            null,
            null,
            null,
            DatabaseContract.CombinedEntry.COLUMN_STOP_SEQUENCE
        )
        val buses = HashMap<String, HashMap<String, ArrayList<String>>>() // Modified HashMap structure

        while (cursor.moveToNext()) {
            val entityIdColumnIndex = cursor.getColumnIndex(DatabaseContract.CombinedEntry.COLUMN_ENTITY_ID)
            val routeNameColumnIndex = cursor.getColumnIndex(DatabaseContract.CombinedEntry.COLUMN_ROUTE_LONG_NAME)
            val stopNameColumnIndex = cursor.getColumnIndex(DatabaseContract.CombinedEntry.COLUMN_STOP_NAME)

            if (entityIdColumnIndex != -1 && routeNameColumnIndex != -1 && stopNameColumnIndex != -1) {
                val entityId = cursor.getString(entityIdColumnIndex)
                val routeName = cursor.getString(routeNameColumnIndex)
                val stopName = cursor.getString(stopNameColumnIndex)

                val innerStops = buses.get(entityId) // Check for existing inner HashMap
                // Create inner HashMap for bus stops
                if (innerStops == null) buses[entityId] = HashMap()
                val existingStops = innerStops?.get(routeName) ?: ArrayList() // Check for existing stops for this route
                existingStops.add(stopName)
                innerStops?.put(routeName, existingStops) // Update inner HashMap with stops
            } else {
                Log.e("getBusStops", "Error: Could not find columns in CombinedEntry table")
            }
        }
        cursor.close()
        db.close()
        return buses
    }

    fun getStopCoordinates(): HashMap<String, Pair<Double, Double>> {
        val db = readableDatabase
        val cursor = db.query(
            DatabaseContract.StopsEntry.TABLE_NAME,
            arrayOf(
                DatabaseContract.StopsEntry.COLUMN_STOP_NAME,
                DatabaseContract.StopsEntry.COLUMN_LATITUDE,
                DatabaseContract.StopsEntry.COLUMN_LONGITUDE
            ),
            null,
            null,
            null,
            null,
            null
        )

        val stopCoordinates = HashMap<String, Pair<Double, Double>>()

        while (cursor.moveToNext()) {
            val stopNameIndex = cursor.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_STOP_NAME)
            val latitudeIndex = cursor.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_LATITUDE)
            val longitudeIndex = cursor.getColumnIndex(DatabaseContract.StopsEntry.COLUMN_LONGITUDE)

            if (stopNameIndex != -1 && latitudeIndex != -1 && longitudeIndex != -1) {
                val stopName = cursor.getString(stopNameIndex)
                val latitude = cursor.getDouble(latitudeIndex)
                val longitude = cursor.getDouble(longitudeIndex)

                stopCoordinates[stopName] = Pair(latitude, longitude)
            } else {
                Log.e("getStopCoordinates", "Error: Could not find columns in StopsEntry table")
            }
        }
        cursor.close()
        db.close()
        return stopCoordinates
    }


    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BusAppDatabase.db"
    }
}
