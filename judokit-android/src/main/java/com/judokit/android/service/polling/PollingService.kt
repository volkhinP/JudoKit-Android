package com.judokit.android.service.polling

import com.judokit.android.api.JudoApiService
import com.judokit.android.api.model.response.BankSaleStatusResponse
import com.judokit.android.api.model.response.JudoApiCallResult
import com.judokit.android.api.model.response.OrderStatus
import kotlinx.coroutines.delay
import retrofit2.await

private const val DELAY_IN_SECONDS = 60L
private const val MILLISECONDS = 1000L
private const val REQUEST_DELAY = 5000L
private const val TIMEOUT = DELAY_IN_SECONDS * MILLISECONDS

class PollingService(private val service: JudoApiService) {

    lateinit var orderId: String
    lateinit var result: (PollingResult<BankSaleStatusResponse>) -> Unit

    private var timeout = TIMEOUT

    suspend fun start() {
        while (timeout > 0L) {
            if (timeout != DELAY_IN_SECONDS * MILLISECONDS) {
                delay(REQUEST_DELAY)
            }
            when (val saleStatusResponse = service.status(orderId).await()) {
                is JudoApiCallResult.Success -> {
                    if (saleStatusResponse.data != null) {
                        handleOrderStatus(saleStatusResponse.data)
                    } else {
                        timeout = 0L
                        result.invoke(PollingResult.CallFailure())
                    }
                }
                is JudoApiCallResult.Failure -> {
                    timeout = 0L
                    result.invoke(PollingResult.CallFailure(error = saleStatusResponse.error))
                }
            }
        }
    }

    private fun handleOrderStatus(data: BankSaleStatusResponse) {
        when (data.orderDetails.orderStatus) {
            OrderStatus.SUCCEEDED -> {
                timeout = 0L
                result.invoke(PollingResult.Success(data))
            }
            OrderStatus.FAILED -> {
                timeout = 0L
                result.invoke(PollingResult.Failure(data))
            }
            OrderStatus.PENDING -> {
                timeout -= REQUEST_DELAY
                when {
                    timeout <= 0L -> result.invoke(PollingResult.Retry)
                    timeout <= TIMEOUT / 2 -> result.invoke(PollingResult.Delay)
                    else -> result.invoke(PollingResult.Processing)
                }
            }
        }
    }

    // restarts the polling flow
    suspend fun retry() {
        timeout = TIMEOUT
        start()
    }

    // resets the timer while polling is in progress
    fun reset() {
        timeout = TIMEOUT
    }

    // Sets the timer to 0 to exit polling
    fun cancel() {
        timeout = 0L
    }
}
