package com.judopay.card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.judopay.Judo;
import com.judopay.JudoOptions;
import com.judopay.R;
import com.judopay.model.Address;
import com.judopay.model.Card;
import com.judopay.model.CardToken;
import com.judopay.model.CardNetwork;
import com.judopay.model.Country;
import com.judopay.validation.CardNumberValidator;
import com.judopay.validation.CountryAndPostcodeValidation;
import com.judopay.validation.CountryAndPostcodeValidator;
import com.judopay.validation.ExpiryDateValidator;
import com.judopay.validation.IssueNumberValidator;
import com.judopay.validation.PaymentFormValidation;
import com.judopay.validation.StartDateAndIssueNumberValidation;
import com.judopay.validation.StartDateValidator;
import com.judopay.validation.Validation;
import com.judopay.validation.ValidationManager;
import com.judopay.validation.Validator;
import com.judopay.view.CardNumberEntryView;
import com.judopay.view.ExpiryDateEntryView;
import com.judopay.view.IssueNumberEntryView;
import com.judopay.view.PostcodeEntryView;
import com.judopay.view.SecurityCodeEntryView;
import com.judopay.view.SimpleTextWatcher;
import com.judopay.view.SingleClickOnClickListener;
import com.judopay.view.StartDateEntryView;

import java.util.ArrayList;

import rx.functions.Action1;

import static com.judopay.Judo.isAvsEnabled;

/**
 * A Fragment that allows for card details to be entered by the user, with validation checks
 * on input data.
 * Configuration options can be provided by passing a {@link JudoOptions} instance in the fragment
 * arguments, identified using the {@link Judo#JUDO_OPTIONS} as a key, e.g.
 * <code>
 * CardEntryFragment fragment = new CardEntryFragment();
 * Bundle args = new Bundle();
 * args.putParcelable(Judo.JUDO_OPTIONS, new JudoOptions.Builder()
 * .setJudoId("123456")
 * .setAmount("1.99")
 * .setCurrency(Currency.USD)
 * .setButtonLabel("Perform payment")
 * .setSecureServerMessageShown(true)
 * .build())
 * fragment.setArguments(args);
 * </code>
 */
public final class CardEntryFragment extends AbstractCardEntryFragment {

    private Button paymentButton;
    private Spinner countrySpinner;
    private SecurityCodeEntryView securityCodeEntryView;
    private CardNumberEntryView cardNumberEntryView;

    private View startDateAndIssueNumberContainer;
    private View countryAndPostcodeContainer;
    private ScrollView scrollView;
    private IssueNumberEntryView issueNumberEntryView;
    private PostcodeEntryView postcodeEntryView;
    private StartDateEntryView startDateEntryView;
    private ExpiryDateEntryView expiryDateEntryView;

    private View secureServerText;
    private ValidationManager validationManager;
    private StartDateValidator startDateValidator;
    private IssueNumberValidator issueNumberValidator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_entry, container, false);

        paymentButton = (Button) view.findViewById(R.id.payment_button);

        securityCodeEntryView = (SecurityCodeEntryView) view.findViewById(R.id.security_code_entry_view);
        cardNumberEntryView = (CardNumberEntryView) view.findViewById(R.id.card_number_entry_view);
        expiryDateEntryView = (ExpiryDateEntryView) view.findViewById(R.id.expiry_date_entry_view);

        postcodeEntryView = (PostcodeEntryView) view.findViewById(R.id.postcode_entry_view);

        countrySpinner = (Spinner) view.findViewById(R.id.country_spinner);
        startDateEntryView = (StartDateEntryView) view.findViewById(R.id.start_date_entry_view);

        issueNumberEntryView = (IssueNumberEntryView) view.findViewById(R.id.issue_number_entry_view);

        startDateAndIssueNumberContainer = view.findViewById(R.id.start_date_issue_number_container);
        countryAndPostcodeContainer = view.findViewById(R.id.country_postcode_container);
        scrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        secureServerText = view.findViewById(R.id.secure_server_text);

        return view;
    }

    @Override
    protected void onInitialize(JudoOptions options) {
        if (judoOptions.getButtonLabel() != null) {
            paymentButton.setText(judoOptions.getButtonLabel());
        }

        CardToken cardToken = judoOptions.getCardToken();

        if (cardToken != null) {
            cardNumberEntryView.setCardType(cardToken.getType(), false);
            securityCodeEntryView.setCardType(cardToken.getType(), false);
            securityCodeEntryView.requestFocus();
        } else {
            if (judoOptions.getCardNumber() != null) {
                int cardType = CardNetwork.fromCardNumber(judoOptions.getCardNumber());
                cardNumberEntryView.setCardType(cardType, false);
                cardNumberEntryView.setText(judoOptions.getCardNumber());
                expiryDateEntryView.requestFocus();
            }

            if (judoOptions.getExpiryYear() != null && judoOptions.getExpiryMonth() != null) {
                expiryDateEntryView.setText(getString(R.string.expiry_date_format, judoOptions.getExpiryMonth(), judoOptions.getExpiryYear()));
                securityCodeEntryView.requestFocus();
            }
        }

        final ArrayList<Validator> validators = new ArrayList<>();

        cardNumberEntryView.getEditText().addTextChangedListener(new SimpleTextWatcher() {
            @Override
            protected void onTextChanged(CharSequence text) {
                int cardType = CardNetwork.fromCardNumber(text.toString());
                if (cardType == CardNetwork.MAESTRO) {
                    addMaestroValidators();
                } else {
                    validationManager.removeValidator(startDateValidator);
                    validationManager.removeValidator(issueNumberValidator);
                }
            }
        });

        CardNumberValidator cardNumberValidator = new CardNumberValidator(cardNumberEntryView.getEditText(), Judo.isMaestroEnabled(), Judo.isAmexEnabled());
        cardNumberValidator.onValidate()
                .subscribe(new Action1<Validation>() {
                    @Override
                    public void call(Validation validation) {
                        cardNumberEntryView.setValidation(validation);
                    }
                });

        ExpiryDateValidator expiryDateValidator = new ExpiryDateValidator(expiryDateEntryView.getEditText());
        expiryDateValidator.onValidate()
                .subscribe(new Action1<Validation>() {
                    @Override
                    public void call(Validation validation) {
                        expiryDateEntryView.setValidation(validation);
                    }
                });


        if (Judo.isAvsEnabled()) {
            CountryAndPostcodeValidator countryAndPostcodeValidator = new CountryAndPostcodeValidator(countrySpinner, postcodeEntryView.getEditText());
            validators.add(countryAndPostcodeValidator);
        }

        validationManager = new ValidationManager(validators, this);

        if (judoOptions.isSecureServerMessageShown()) {
            secureServerText.setVisibility(View.VISIBLE);
        } else {
            secureServerText.setVisibility(View.GONE);
        }

        initializeCountry();
        initializePayButton();
    }

    private void addMaestroValidators() {
        startDateValidator = new StartDateValidator(startDateEntryView.getEditText());
        startDateValidator.onValidate()
                .subscribe(new Action1<Validation>() {
                    @Override
                    public void call(Validation validation) {
                        startDateEntryView.setValidation(validation);
                    }
                });

        issueNumberValidator = new IssueNumberValidator(issueNumberEntryView.getEditText());
        validationManager.addValidator(issueNumberValidator);
    }

    private void initializePayButton() {
        paymentButton.setOnClickListener(new SingleClickOnClickListener() {
            @Override
            public void doClick() {
                hideKeyboard();
                submitForm();
            }
        });
    }

    private void initializeCountry() {
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String country = (String) countrySpinner.getSelectedItem();
                postcodeEntryView.setHint(getPostcodeLabel(country));
                boolean postcodeNumeric = Country.UNITED_STATES.equals(country);
                postcodeEntryView.setNumericInput(postcodeNumeric);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getPostcodeLabel(String country) {
        switch (country) {
            case Country.UNITED_STATES:
                return R.string.billing_zip_code;

            case Country.CANADA:
                return R.string.billing_postal_code;

            default:
                return R.string.billing_postcode;
        }
    }

//    private void initializePostcode(SimpleTextWatcher formValidator) {
//        postcodeEntryView.addTextChangedListener(formValidator);
//        postcodeEntryView.addOnFocusChangeListener(new ScrollOnFocusChangeListener(scrollView));
//    }
//
//    private void initializeExpiryDate(SimpleTextWatcher formValidator) {
//        if (judoOptions.getCardToken() == null) {
//            expiryDateEntryView.addTextChangedListener(formValidator);
//        } else {
//            expiryDateEntryView.setExpiryDate(judoOptions.getCardToken().getFormattedEndDate());
//            expiryDateEntryView.setEnabled(false);
//        }
//    }
//
//    private void initializeCardNumber(SimpleTextWatcher formValidator) {
//        if (judoOptions.getCardToken() == null) {
//            cardNumberEntryView.addTextChangedListener(formValidator);
//        } else {
//            cardNumberEntryView.setTokenCard(judoOptions.getCardToken());
//        }
//    }

//    private void updateFormView() {
//        PaymentForm.Builder builder = new PaymentForm.Builder()
//                .setCardNumber(cardNumberEntryView.getText())
//                .setSecurityCode(securityCodeEntryView.getText())
//                .setCountry(null)
//                .setPostcode(postcodeEntryView.getText())
//                .setIssueNumber(issueNumberEntryView.getText())
//                .setExpiryDate(expiryDateEntryView.getText())
//                .setStartDate(startDateEntryView.getText())
//                .setAddressRequired(Judo.isAvsEnabled())
//                .setAmexSupported(Judo.isAmexEnabled())
//                .setMaestroSupported(Judo.isMaestroEnabled());
//
//        CardToken cardToken = judoOptions.getCardToken();
//        if (cardToken != null) {
//            builder.setTokenCard(true)
//                    .setCardType(cardToken.getType());
//        }
//
//        PaymentFormValidation formView = new PaymentFormValidation.Builder()
//                .build(builder.build());
//
//        if (cardToken == null) {
//            cardNumberEntryView.setCardType(formView.getCardType(), true);
//        }
//
//        updateFormErrors(formView);
//        moveFieldFocus(formView);
//    }

    private void updateFormErrors(PaymentFormValidation formView) {
        showStartDateAndIssueNumberErrors(formView.getStartDateAndIssueNumberState());

        updateCvvErrors(formView);

        updateCountryAndPostcode(formView.getCountryAndPostcodeValidation());

        paymentButton.setVisibility(formView.isPaymentButtonEnabled() ? View.VISIBLE : View.GONE);
    }

    private void updateCvvErrors(PaymentFormValidation formView) {
//        securityCodeEntryView.setAlternateHint(formView.getSecurityCodeHint());

        securityCodeEntryView.setMaxLength(formView.getSecurityCodeLength());
        securityCodeEntryView.setCardType(formView.getCardType(), true);
    }

    private void showStartDateAndIssueNumberErrors(StartDateAndIssueNumberValidation startDateAndIssueNumberValidation) {
//        startDateEntryView.setError(startDateAndIssueNumberValidation.getStartDateError(),
//                startDateAndIssueNumberValidation.isShowStartDateError());
        startDateAndIssueNumberContainer.setVisibility(startDateAndIssueNumberValidation.isShowIssueNumberAndStartDate()
                ? View.VISIBLE : View.GONE);
    }

    private void updateCountryAndPostcode(CountryAndPostcodeValidation validation) {
        countryAndPostcodeContainer.setVisibility(validation.isShowCountryAndPostcode() ? View.VISIBLE : View.GONE);

        postcodeEntryView.setHint(validation.getPostcodeLabel());
        postcodeEntryView.setError(validation.getPostcodeError(), validation.isShowPostcodeError());

        postcodeEntryView.setEnabled(validation.isPostcodeEnabled());
        postcodeEntryView.setNumericInput(validation.isPostcodeNumeric());
    }

    private void moveFieldFocus(PaymentFormValidation formView) {
        if (cardNumberEntryView.hasFocus() && formView.getCardNumberValidation().isEntryComplete() && !formView.getCardNumberValidation().isShowError()) {
            if (startDateAndIssueNumberContainer.getVisibility() == View.VISIBLE) {
                startDateEntryView.requestFocus();
            } else {
                expiryDateEntryView.requestFocus();
            }
        } else if (expiryDateEntryView.hasFocus() && formView.isExpiryDateEntryComplete() && !formView.isShowExpiryDateError()) {
            securityCodeEntryView.requestFocus();
        } else if (securityCodeEntryView.hasFocus() && formView.isSecurityCodeValid()) {
            if (countryAndPostcodeContainer.getVisibility() == View.VISIBLE) {
                postcodeEntryView.requestFocus();
            }
        } else if (startDateEntryView.hasFocus()
                && formView.getStartDateAndIssueNumberState().isStartDateEntryComplete()
                && !formView.getStartDateAndIssueNumberState().isShowStartDateError()) {
            issueNumberEntryView.requestFocus();
        }
    }

    private void submitForm() {
        Card.Builder cardBuilder = new Card.Builder()
                .setCardNumber(cardNumberEntryView.getText())
                .setExpiryDate(expiryDateEntryView.getText())
                .setSecurityCode(securityCodeEntryView.getText());

        Address.Builder addressBuilder = new Address.Builder()
                .setPostCode(postcodeEntryView.getText());

        if (isAvsEnabled()) {
            addressBuilder.setCountryCode(Country.codeFromCountry((String) countrySpinner.getSelectedItem()));
        }

        cardBuilder.setCardAddress(addressBuilder.build());

        if (cardNumberEntryView.getCardType() == CardNetwork.MAESTRO) {
            cardBuilder.setIssueNumber(issueNumberEntryView.getText())
                    .setStartDate(startDateEntryView.getText());
        }

        if (cardEntryListener != null) {
            cardEntryListener.onSubmit(cardBuilder.build());
        }
    }

    public static CardEntryFragment newInstance(JudoOptions judoOptions, CardEntryListener listener) {
        CardEntryFragment cardEntryFragment = new CardEntryFragment();
        cardEntryFragment.setCardEntryListener(listener);

        Bundle arguments = new Bundle();
        arguments.putParcelable(Judo.JUDO_OPTIONS, judoOptions);
        cardEntryFragment.setArguments(arguments);

        return cardEntryFragment;
    }

    @Override
    public void onValidate(boolean valid) {
        paymentButton.setVisibility(valid ? View.VISIBLE : View.GONE);
    }
}