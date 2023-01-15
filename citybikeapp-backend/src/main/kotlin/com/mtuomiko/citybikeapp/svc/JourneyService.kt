package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.common.TIMEZONE
import com.mtuomiko.citybikeapp.common.model.CursorWith
import com.mtuomiko.citybikeapp.common.model.Journey
import com.mtuomiko.citybikeapp.common.model.PaginationKeyset
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.svc.model.QueryParameter
import io.micronaut.core.beans.BeanIntrospection
import io.micronaut.core.beans.BeanProperty
import jakarta.inject.Singleton
import java.security.InvalidParameterException
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

const val DEFAULT_ORDER = "departureAt"

@Singleton
class JourneyService(
    private val config: PaginationConfig,
    private val journeyRepository: JourneyRepository
) {
    private val introspection = BeanIntrospection.getIntrospection(Journey::class.java) // compile time access

    fun searchJourneys(queryParameters: Map<String, String>): CursorWith<List<Journey>> {
        val orderByString = queryParameters.get(QueryParameter.ORDER_BY) ?: DEFAULT_ORDER
        val orderByProperty = introspection.getProperty(orderByString)
            .orElseThrow { Exception("unknown orderBy property") }

        val keyset = queryParameters.get(QueryParameter.NEXT_CURSOR)?.let {
            extractKeyset(it, orderByProperty.type)
        }

        val descending = queryParameters.get(QueryParameter.DIRECTION) != "asc"
        val pageSize = config.getMaxLimitedPageSize(queryParameters.get(QueryParameter.PAGE_SIZE)?.toInt())

        val resultJourneys =
            journeyRepository.listJourneys(orderByString, descending, pageSize, keyset).map { it.toModel() }
        val nextCursor = if (resultJourneys.size == pageSize) {
            createCursor(orderByProperty, resultJourneys.last())
        } else {
            null
        }

        return CursorWith(content = resultJourneys, cursor = nextCursor)
    }

    private fun <T> extractKeyset(cursor: String, type: Class<T>): PaginationKeyset<T, Long> {
        val tokens = cursor.split('|')
        if (tokens.size != 2 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
            throw InvalidParameterException("invalid cursor")
        }

        val typedValueKey = convertToTypedValue(tokens[0], type)
        val typedIdKey = tokens[1].toLong()

        return PaginationKeyset(typedValueKey, typedIdKey)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> convertToTypedValue(value: String, type: Class<T>): T = when (type) {
        Instant::class.java -> try {
            ZonedDateTime.parse(value).toInstant()
        } catch (e: DateTimeParseException) {
            null
        }

        Long::class.java -> value.toLongOrNull()
        Int::class.java -> value.toIntOrNull()

        else -> null
    } as? T ?: throw InvalidParameterException("failed to parse `$value` to type ${type.name}")

    private fun createCursor(orderByProperty: BeanProperty<Journey, Any>, element: Journey): String {
        val value = orderByProperty.get(element)
        val valueString = convertToString(value, orderByProperty.type)
        val idString = element.id.toString()
        return "$valueString|$idString"
    }

    private fun <T> convertToString(value: T, type: Class<T>): String = when (type) {
        Instant::class.java -> ZonedDateTime.ofInstant((value as Instant), TIMEZONE).toString()
        Long::class.java, Int::class.java -> value.toString()

        else -> null
    } ?: throw InvalidParameterException("failed to stringify $value to type ${type.name}")

    private fun Map<String, String>.get(enum: QueryParameter) = this[enum.value]

    private fun JourneyEntity.toModel() =
        Journey(id, departureAt, arrivalAt, departureStationId, arrivalStationId, distance, duration)
}
