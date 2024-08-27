package com.mtuomiko.citybikeapp.svc

import com.mtuomiko.citybikeapp.api.BadRequestError
import com.mtuomiko.citybikeapp.common.model.CursorWith
import com.mtuomiko.citybikeapp.common.model.Journey
import com.mtuomiko.citybikeapp.common.model.PaginationKeyset
import com.mtuomiko.citybikeapp.dao.entity.JourneyEntity
import com.mtuomiko.citybikeapp.dao.repository.JourneyRepository
import com.mtuomiko.citybikeapp.svc.model.Direction
import com.mtuomiko.citybikeapp.svc.model.JourneyQueryParameters
import org.springframework.stereotype.Service
import java.security.InvalidParameterException
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

const val DEFAULT_ORDER = "departureAt"

@Service
class JourneyService(
    private val config: SvcConfig,
    private val journeyRepository: JourneyRepository,
) {
    private val journeyClassProperties = Journey::class.declaredMemberProperties

    fun searchJourneys(queryParameters: JourneyQueryParameters): CursorWith<List<Journey>> {
        val orderByString = queryParameters.orderBy ?: DEFAULT_ORDER
        val orderByProperty =
            journeyClassProperties.firstOrNull { p -> p.name == orderByString }
                ?: throw BadRequestError("unknown journey orderBy property `$orderByString`")

        // get the actual KClass from kotlin member property
        val kClass: KClass<*> = orderByProperty.returnType.jvmErasure

        val keyset =
            queryParameters.nextCursor?.let {
                extractKeyset(it, kClass)
            }

        val descending = queryParameters.direction != Direction.ASC // defaults to descending order
        val pageSize = config.getMaxLimitedPageSize(queryParameters.pageSize)

        val resultJourneys =
            journeyRepository.listJourneys(orderByString, descending, pageSize, keyset).map { it.toModel() }
        val nextCursor =
            if (resultJourneys.size == pageSize) {
                createCursor(orderByProperty, resultJourneys.last())
            } else {
                null
            }

        return CursorWith(content = resultJourneys, cursor = nextCursor)
    }

    /**
     * @param cursor Custom keyset pagination cursor as a String in format **value|id** where *value* is the primary
     * orderBy value to paginate by, and *id* is the id to paginate by (the implicit secondary orderBy).
     * @param kClass Which class the cursor (primary orderBy value) should be deserialized to
     */
    private fun <T : Any> extractKeyset(
        cursor: String,
        kClass: KClass<T>,
    ): PaginationKeyset<T, Long> {
        val tokens = cursor.split('|')
        if (tokens.size != 2 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
            throw InvalidParameterException("invalid cursor")
        }

        val typedValueKey = convertToTypedValue(tokens[0], kClass)
        val typedIdKey = tokens[1].toLong()

        return PaginationKeyset(typedValueKey, typedIdKey)
    }

    /**
     * Tries to cast value to the given type
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> convertToTypedValue(
        value: String,
        kClass: KClass<T>,
    ): T {
        val rawValue =
            when (kClass) {
                Instant::class ->
                    try {
                        value.toLongOrNull()?.let { Instant.ofEpochSecond(it) }
                    } catch (e: DateTimeParseException) {
                        null
                    }

                Long::class -> value.toLongOrNull()
                Int::class -> value.toIntOrNull()

                else -> null
            }

        val typedValue = rawValue as? T

        return typedValue
            ?: throw IllegalArgumentException("failed to parse cursor value `$value` to type $kClass")
    }

    /**
     * Custom keyset pagination cursor as a String in format **value|id** where *value* is the primary orderBy value to
     * paginate by, and *id* is the id to paginate by (the implicit secondary orderBy).
     *
     *
     */
    private fun createCursor(
        orderByProperty: KProperty1<Journey, *>,
        element: Journey,
    ): String {
        val value = orderByProperty.get(element)
        val clazz = orderByProperty.returnType.jvmErasure
        val valueString = convertToString(value, clazz)
        val idString = element.id.toString()
        return "$valueString|$idString" // custom keyset/cursor format, could encode if needed
    }

    private fun <T> convertToString(
        value: T,
        type: KClass<*>,
    ): String =
        when (type) {
            Instant::class -> (value as Instant).epochSecond.toString()
            Long::class, Int::class -> value.toString()

            else -> null
        } ?: throw IllegalArgumentException("failed to stringify $value to type $type")

    private fun JourneyEntity.toModel() =
        Journey(
            id,
            departureAt,
            arrivalAt,
            departureStationId,
            arrivalStationId,
            distance,
            duration,
        )
}
