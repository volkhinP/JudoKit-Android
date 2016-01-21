package com.judopay;

import android.os.Bundle;

import com.judopay.model.Receipt;

import static com.judopay.Judo.JUDO_AMOUNT;
import static com.judopay.Judo.JUDO_CONSUMER;
import static com.judopay.Judo.JUDO_CURRENCY;
import static com.judopay.Judo.JUDO_ID;
import static com.judopay.Judo.JUDO_OPTIONS;

/**
 * Displays a payment form to the user, allowing for a pre-auth to be made.
 * <br>
 * The {@link Receipt} containing the result of the payment transaction is
 * returned in the Activity result and can be either {@link Judo#RESULT_SUCCESS},
 * {@link Judo#RESULT_DECLINED} or {@link Judo#RESULT_ERROR} if an error occurred.
 * <br>
 * Mandatory extras:
 * <ol>
 * <li>{@link Judo#JUDO_ID} Judo ID of your account</li>
 * <li>{@link Judo#JUDO_AMOUNT} the total amount for the transaction</li>
 * <li>{@link Judo#JUDO_CURRENCY} the currency for the transaction (GBP, USD, CAD)</li>
 * <li>{@link Judo#JUDO_CONSUMER} identifier for the consumer of the transaction</li>
 * </ol>
 * <br>
 * Optional extras:
 * {@link Judo#JUDO_META_DATA} an optional key-value map of data to be included when making the
 * pre-auth transaction.
 */
public final class PreAuthActivity extends JudoActivity {

    private PreAuthFragment preAuthFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra((JUDO_OPTIONS))) {
            JudoOptions options = getIntent().getParcelableExtra(JUDO_OPTIONS);

            if (options.getAmount() == null || options.getJudoId() == null || options.getCurrency() == null || options.getConsumerRef() == null) {
                throw new IllegalArgumentException("Intent must contain all required extras for PreAuthActivity");
            }
        } else {
            checkRequiredExtras(JUDO_AMOUNT, JUDO_ID, JUDO_CURRENCY, JUDO_CONSUMER);
        }

        setTitle(R.string.payment);

        if (savedInstanceState == null) {
            preAuthFragment = new PreAuthFragment();
            preAuthFragment.setArguments(getIntent().getExtras());

            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, preAuthFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (!preAuthFragment.isPaymentInProgress()) {
            super.onBackPressed();
        }
    }

}