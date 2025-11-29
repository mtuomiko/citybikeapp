package com.mtuomiko.citybikeapp.api

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class DefaultExceptionHandler {
    @ExceptionHandler(value = [CustomError::class])
    fun onApiException(
        ex: CustomError,
        response: HttpServletResponse,
    ) {
        val statusCode = getHttpStatus(ex)

        response.sendError(statusCode.value(), ex.message)
    }

    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun onConstraintViolation(
        ex: ConstraintViolationException,
        response: HttpServletResponse,
    ) = response.sendError(HttpStatus.BAD_REQUEST.value(), ex.constraintViolations.joinToString(", ") { it.message })

    private fun getHttpStatus(error: CustomError): HttpStatus =
        when (error) {
            is BadRequestError -> HttpStatus.BAD_REQUEST
            is NotFoundError -> HttpStatus.NOT_FOUND
            // else -> HttpStatus.INTERNAL_SERVER_ERROR, add this back when needed so kotlin doesn't warn us about it
        }
}
