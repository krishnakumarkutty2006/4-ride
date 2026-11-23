package com.example.data.repository

import com.example.data.model.DriverEntity
import com.example.data.model.MapPlace
import com.example.data.model.PoiCategory
import com.example.data.model.VehicleType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object LocationCatalog {
    const val DEFAULT_LAT = 12.9716
    const val DEFAULT_LNG = 77.5946

    private val basePlaces = listOf(
        MapPlace("p_city", "MG Road Central Plaza", "MG Road, City Center", 12.9756, 77.6066, null),
        MapPlace("p_tech", "Horizon Tech Park", "Outer Ring Ave, Sector 4", 12.9591, 77.6410, null),
        MapPlace("p_mall", "Orion Galleria Mall", "West Gate Boulevard", 12.9890, 77.5612, null),
        MapPlace("p_airport", "Metro International Terminal", "Airport Express Highway", 13.0120, 77.6380, null),
        MapPlace("p_hotel1", "Grand Meridian Hotel", "14 Residency Parkway", 12.9718, 77.6012, PoiCategory.HOTEL),
        MapPlace("p_hotel2", "Saffron Suites & Inn", "22 Parkview Lane", 12.9642, 77.5845, PoiCategory.HOTEL),
        MapPlace("p_cafe1", "Roast & Ember Cafe", "88 Lavelle Terrace", 12.9735, 77.5968, PoiCategory.CAFE),
        MapPlace("p_cafe2", "Velvet Espresso Bar", "5 Church Cross Street", 12.9768, 77.6032, PoiCategory.CAFE),
        MapPlace("p_bakery1", "Artisan Crumb Bakery", "19 Indiranagar Main", 12.9784, 77.6208, PoiCategory.BAKERY),
        MapPlace("p_bakery2", "Golden Croissant House", "41 Maple Arcade", 12.9665, 77.5912, PoiCategory.BAKERY),
        MapPlace("p_hosp1", "St. Jude Multispecialty Hospital", "300 Health Corridor", 12.9682, 77.6115, PoiCategory.HOSPITAL),
        MapPlace("p_hosp2", "City Care Medical Center", "74 Richmond Circle", 12.9615, 77.5985, PoiCategory.HOSPITAL),
        MapPlace("p_bus1", "Majestic Central Bus Stand", "Platform 1-18, Transit Hub", 12.9772, 77.5724, PoiCategory.BUS_STAND),
        MapPlace("p_bus2", "East Corridor Bus Terminal", "Ring Road Junction", 12.9695, 77.6312, PoiCategory.BUS_STAND),
        MapPlace("p_rail1", "City Junction Railway Station", "Station Square East", 12.9781, 77.5698, PoiCategory.RAILWAY_STATION),
        MapPlace("p_rail2", "Cantonment Rail Terminal", "Queen's Road North", 12.9935, 77.5988, PoiCategory.RAILWAY_STATION)
    )

    fun placesAnchoredNear(anchorLat: Double, anchorLng: Double): List<MapPlace> {
        val distFromDefault = calculateDistanceKm(anchorLat, anchorLng, DEFAULT_LAT, DEFAULT_LNG)
        if (distFromDefault <= 25.0) {
            return basePlaces
        }
        val dLat = anchorLat - DEFAULT_LAT
        val dLng = anchorLng - DEFAULT_LNG
        return basePlaces.map { place ->
            place.copy(lat = place.lat + dLat, lng = place.lng + dLng)
        }
    }

    fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val km = earthRadiusKm * c
        return ((km * 10.0).roundToInt() / 10.0).coerceAtLeast(0.4)
    }

    fun estimateMinutes(distanceKm: Double, vehicleType: VehicleType): Int {
        val avgSpeedKmh = when (vehicleType) {
            VehicleType.CAR -> 28.0
            VehicleType.VAN -> 25.0
            VehicleType.TRAVEL_BUS -> 22.0
        }
        val mins = ((distanceKm / avgSpeedKmh) * 60.0).roundToInt() + 2
        return max(3, mins)
    }

    fun calculateTripFare(driver: DriverEntity, distanceKm: Double): Pair<Double, Double> {
        val isLongDistance = distanceKm >= 15.0
        val appliedPerKm = if (isLongDistance) driver.longDistancePricePerKm else driver.perKmPrice
        val rawFare = driver.minFare + (distanceKm * appliedPerKm)
        val rounded = (rawFare / 5.0).roundToInt() * 5.0
        return max(driver.minFare, rounded) to appliedPerKm
    }

    fun maskPhone(phone: String): String {
        val clean = phone.trim()
        if (clean.length <= 4) return "••••"
        val last4 = clean.takeLast(4)
        val prefix = if (clean.startsWith("+")) clean.take(3) + " " else ""
        return "${prefix}••••• $last4"
    }
}
