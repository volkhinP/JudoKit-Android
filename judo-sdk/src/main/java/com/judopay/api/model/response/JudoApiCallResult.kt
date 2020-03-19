package com.judopay.api.model.response

import com.judopay.api.error.ApiError
import com.judopay.model.JudoPaymentResult

sealed class JudoApiCallResult<out T> {
    data class Success<T>(val data: T?) : JudoApiCallResult<T>()
    data class Failure(
        val statusCode: Int = -1,
        val error: ApiError? = null,
        val throwable: Throwable? = null
    ) : JudoApiCallResult<Nothing>()
}

fun JudoApiCallResult<Receipt>.toJudoPaymentResult(): JudoPaymentResult {
    val fallbackError = ApiError(-1, -1, "")

    return when (this) {
        is JudoApiCallResult.Success -> if (data != null) JudoPaymentResult.Success(data) else JudoPaymentResult.Error(
            fallbackError
        )
        is JudoApiCallResult.Failure -> JudoPaymentResult.Error(error ?: fallbackError)
    }
}