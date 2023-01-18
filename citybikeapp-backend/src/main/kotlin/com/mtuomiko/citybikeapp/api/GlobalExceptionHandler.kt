package com.mtuomiko.citybikeapp.api

import com.mtuomiko.citybikeapp.common.BadRequestError
import com.mtuomiko.citybikeapp.common.CustomError
import com.mtuomiko.citybikeapp.common.InnerError
import com.mtuomiko.citybikeapp.common.NotFoundError
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
@Requires(classes = [CustomError::class])
class GlobalExceptionHandler : ExceptionHandler<CustomError, HttpResponse<JsonError>> {

    override fun handle(request: HttpRequest<*>, exception: CustomError): HttpResponse<JsonError> {
        val statusCode = getHttpStatus(exception)
        val apiErrors = exception.innerErrors?.map { it.toJsonError() }

        return HttpResponse.status<JsonError>(statusCode).body(
            JsonError(exception.message)
                .embedded("errors", apiErrors)
                .link(Link.SELF, Link.of(request.uri))
        )
    }

    private fun getHttpStatus(error: CustomError): HttpStatus {
        return when (error) {
            is BadRequestError -> HttpStatus.BAD_REQUEST
            is NotFoundError -> HttpStatus.NOT_FOUND
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    private fun InnerError.toJsonError() = JsonError(message).path(target)
}
