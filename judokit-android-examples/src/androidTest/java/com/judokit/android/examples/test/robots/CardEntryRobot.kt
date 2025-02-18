package com.judokit.android.examples.test.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.judokit.android.examples.R
import com.judokit.android.examples.test.espresso.DEFAULT_TIMEOUT
import com.judokit.android.examples.test.espresso.waitUntilVisible
import com.judokit.android.examples.test.exceptions.ViewNotDefinedException
import com.judokit.android.examples.test.robots.enum.View
import com.judokit.android.examples.test.robots.enum.ViewType
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not

class CardEntryRobot {

    fun isOnScreen(screen: String) {
        when (screen) {
            View.MAIN.value -> onView(withId(R.id.sampleAppConstraintLayout)).check(
                matches(
                    isDisplayed()
                )
            )
            else -> throw throw ViewNotDefinedException(
                CardEntryRobot::class.qualifiedName,
                screen
            )
        }
    }

    fun press(button: String) {
        when (button) {
            View.SUBMIT_BUTTON.value -> {
                Thread.sleep(DEFAULT_TIMEOUT)
                Web.onWebView()
                    .withElement(DriverAtoms.findElement(Locator.NAME, "UsernamePasswordEntry"))
                    .perform(DriverAtoms.webClick())
            }
            else -> onView(withText(button)).waitUntilVisible().perform(click())
        }
    }

    fun tapOn(text: String, type: String) {
        when (type) {
            ViewType.TEXT_FIELD.value ->
                when (text) {
                    View.SECURE_CODE.value -> onView(withId(R.id.securityNumberTextInputEditText)).perform(
                        click()
                    )
                    View.CARDHOLDER_NAME.value -> onView(withId(R.id.nameTextInputEditText)).perform(
                        click()
                    )
                    View.COUNTRY.value -> onView(withId(R.id.countryTextInputEditText)).perform(
                        click()
                    )
                    else -> throw throw ViewNotDefinedException(
                        CardEntryRobot::class.qualifiedName,
                        text
                    )
                }
            else -> onView(withText(text)).perform(click())
        }
    }

    fun enterTextIntoField(textToEnter: String?, fieldName: String) {
        val view = when (fieldName) {
            View.CARD_NUMBER.value -> onView(withId(R.id.numberTextInputEditText))
            View.CARDHOLDER_NAME.value -> onView(withId(R.id.nameTextInputEditText))
            View.EXPIRY_DATE.value -> onView(withId(R.id.expirationDateTextInputEditText))
            View.SECURE_CODE.value -> onView(withId(R.id.securityNumberTextInputEditText))
            View.POST_CODE.value -> onView(withId(R.id.postcodeTextInputEditText))
            else -> onView(withId(R.id.numberTextInputEditText))
        }
        view.perform(click(), replaceText(textToEnter))
    }

    fun isVisible(identifier: String?, view: String?, visibility: String) {
        val matchedView = when (identifier) {
            View.CARD_HEADER.value -> onView(withId(R.id.cardView))
            View.GOOGLE_PAY_HEADER.value -> onView(withId(R.id.googlePayCardView))
            View.IDEAL_HEADER.value -> onView(withId(R.id.idealPaymentCardView))
            View.PBBA_HEADER.value -> onView(withId(R.id.payByBankCardView))
            View.CARD_SELECTOR.value -> onView(ViewMatchers.withTagValue(CoreMatchers.`is`("CARD")))
            View.GOOGLE_PAY_SELECTOR.value -> onView(
                ViewMatchers.withTagValue(
                    CoreMatchers.`is`(
                        "GOOGLE_PAY"
                    )
                )
            )
            View.IDEAL_SELECTOR.value -> onView(ViewMatchers.withTagValue(CoreMatchers.`is`("IDEAL")))
            View.PAYMENT_METHODS.value -> onView(withId(R.id.coordinatorLayout))
            View.MAIN.value -> onView(withId(R.id.sampleAppConstraintLayout))
            View.RESULTS.value -> onView(withText("JudoResult")).waitUntilVisible()
            View.INVALID_POST_CODE.value -> onView(
                allOf(
                    withId(R.id.errorTextView),
                    isDescendantOfA(withId(R.id.postcodeTextInputLayout))
                )
            )
            View.CHECK_EXPIRY_DATE.value -> onView(
                allOf(
                    withId(R.id.errorTextView),
                    isDescendantOfA(withId(R.id.expirationDateTextInputLayout))
                )
            )
            else -> onView(withText(view))
        }
        if (visibility == "be") {
            matchedView.check(matches(isDisplayed()))
        } else {
            if (view == "screen") {
                matchedView.check(doesNotExist())
            } else {
                matchedView.check(matches(not(isDisplayed())))
            }
        }
    }

    fun isDisabled(button: String) {
        onView(withText(button)).check(matches(not(isEnabled())))
    }

    fun isValueOfFieldEqual(field: String, value: String) {
        onView(ViewMatchers.withTagValue(CoreMatchers.`is`(field))).check(
            matches(withText(containsString(value)))
        )
    }

    fun selectFromDropdown(country: String) {
        onView(withText(country)).inRoot(isPlatformPopup()).perform(click())
    }

    fun expectedValue(view: String, expectedValue: String) {
        val matchedView = when (view) {
            View.POST_CODE.value -> onView(withId(R.id.postcodeTextInputEditText))
            View.SUBMIT_BUTTON.value -> onView(withId(R.id.submitButton))
            else -> throw ViewNotDefinedException(CardEntryRobot::class.qualifiedName, view)
        }
        matchedView.check(matches(withText(expectedValue)))
    }
}
