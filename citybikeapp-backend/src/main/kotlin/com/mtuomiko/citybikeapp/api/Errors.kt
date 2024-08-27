package com.mtuomiko.citybikeapp.api

sealed class CustomError(
    message: String = "Unknown error",
    cause: Exception? = null,
    val innerErrors: List<InnerError>? = null,
) : Exception(message, cause)

class InnerError(
    val message: String,
    val target: String? = null,
)

class NotFoundError(
    message: String = "Not found",
    cause: Exception? = null,
    innerErrors: List<InnerError>? = null,
) : CustomError(message, cause, innerErrors)

class BadRequestError(
    message: String = "Bad request",
    cause: Exception? = null,
    innerErrors: List<InnerError>? = null,
) : CustomError(message, cause, innerErrors)

object ErrorMessages {
    const val INVALID_QUERY_PARAMETER = "invalid query parameter"
}
