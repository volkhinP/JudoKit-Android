package com.judopay;

import android.content.Intent;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.judopay.TestUtil.getJudo;
import static com.judopay.util.ViewMatchers.withTextInputHint;

public class SecurityCodeHintTest {

    @Rule
    public ActivityTestRule<PaymentActivity> rule = new ActivityTestRule<>(PaymentActivity.class, false, false);

    @Test
    public void shouldDisplayCvvSecurityCodeWhenUnknownCard() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.security_code_input_layout))
                .check(matches(withTextInputHint("CVV")));
    }

    @Test
    public void shouldDisplayVisaSecurityCodeWhenVisaDetected() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("4976"));

        onView(withId(R.id.security_code_input_layout))
                .check(matches(withTextInputHint("CVV2")));
    }

    @Test
    public void shouldDisplayAmexSecurityCodeWhenAmexDetected() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("3400"));

        onView(withId(R.id.security_code_input_layout))
                .check(matches(withTextInputHint("CID")));
    }

    @Test
    public void shouldDisplayMastercardSecurityCodeWhenMastercardDetected() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("5100"));

        onView(withId(R.id.security_code_input_layout))
                .check(matches(withTextInputHint("CVC2")));
    }

    @Test
    public void shouldDisplayCidvHintWhenAmexCardNumberEntered() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("3400"));

        onView(withId(R.id.security_code_edit_text))
                .perform(click())
                .check(matches(withHint("0000")));
    }

    @Test
    public void shouldDisplayCv2HintWhenVisaCardNumberEntered() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("4976"));

        onView(withId(R.id.security_code_edit_text))
                .perform(click())
                .check(matches(withHint("000")));
    }

    @Test
    public void shouldDisplayCvvImageOnLaunch() {
        Intent intent = new Intent();
        intent.putExtra(Judo.JUDO_OPTIONS, getJudo());

        rule.launchActivity(intent);

        onView(withId(R.id.security_code_image_view))
                .check(matches(isDisplayed()));
    }

}