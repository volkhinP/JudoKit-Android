package com.judokit.android.ui.common

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.judokit.android.R
import com.zapp.library.merchant.util.PBBAAppUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Checks the input string to see whether or not it is a valid Luhn number.
 *
 * @param cardNumber a String that may or may not represent a valid Luhn number
 * @return `true` if and only if the input value is a valid Luhn number
 */
internal fun isValidLuhnNumber(cardNumber: String): Boolean {

    var isOdd = true
    var sum = 0

    for (index in cardNumber.length - 1 downTo 0) {
        val c = cardNumber[index]
        if (!Character.isDigit(c)) {
            return false
        }

        var digitInteger = Character.getNumericValue(c)
        isOdd = !isOdd

        if (isOdd) {
            digitInteger *= 2
        }

        if (digitInteger > 9) {
            digitInteger -= 9
        }

        sum += digitInteger
    }

    return sum % 10 == 0
}

fun toDate(
    timestamp: String,
    locale: Locale,
    pattern: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
): Date = try {
    val sdf = SimpleDateFormat(pattern, locale)
    sdf.parse(timestamp) ?: Date()
} catch (exception: ParseException) {
    Log.e("toDate", exception.toString())
    Date()
}

fun getLocale(resources: Resources): Locale =
    ConfigurationCompat.getLocales(resources.configuration)[0]

fun showAlert(context: Context, message: String) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.unable_to_process_request_error_title)
        .setMessage(message)
        .setNegativeButton(R.string.close, null)
        .show()
}

/**
 * Checks whether banking application is installed on user's phone or not.
 * @param context context of the application.
 * @return True if there is at least one PBBA enabled CFI App available, false otherwise.
 */
fun isBankingAppAvailable(context: Context) = PBBAAppUtils.isCFIAppAvailable(context)

/**
 * Helper function to check if a dependency is present using reflection.
 * @param className name of a class in a dependency package.
 * @return true if present, otherwise false
 */
internal fun isDependencyPresent(className: String) =
    try {
        Class.forName(className)
        true
    } catch (e: Throwable) {
        Log.i("isDependencyPresent", "Dependency $className not available", e)
        false
    }
