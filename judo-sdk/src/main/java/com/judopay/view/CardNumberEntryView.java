package com.judopay.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.judopay.R;
import com.judopay.model.CardNetwork;
import com.judopay.model.CardToken;
import com.judopay.validation.Validation;

import static com.judopay.model.CardNetwork.AMEX;

/**
 * A view that allows for card number data to be input by the user and the detected card type
 * to be displayed alongside the card number.
 * Does not perform validation itself, this is done by the {@link com.judopay.validation.CardNumberValidator}
 * class.
 */
public class CardNumberEntryView extends RelativeLayout {

    private static final String KEY_SUPER_STATE = "superState";
    private static final String KEY_CARD_TYPE = "cardType";

    private JudoEditText editText;
    private TextInputLayout inputLayout;
    private View scanCardButton;
    private CardTypeImageView cardTypeImageView;
    private NumberFormatTextWatcher numberFormatTextWatcher;
    private int cardType;

    public interface ScanCardButtonListener {
        void onClick();
    }

    public CardNumberEntryView(final Context context) {
        super(context);
        initialize(context);
    }

    public CardNumberEntryView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public CardNumberEntryView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(final Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_card_number_entry, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final TextView cardNumberHelperText = (TextView) findViewById(R.id.card_number_helper_text);

        this.cardTypeImageView = (CardTypeImageView) findViewById(R.id.card_type_view);
        this.editText = (JudoEditText) findViewById(R.id.card_number_edit_text);
        this.inputLayout = (TextInputLayout) findViewById(R.id.card_number_input_layout);
        this.scanCardButton = findViewById(R.id.scan_card_button);

        editText.setOnFocusChangeListener(new EmptyTextHintOnFocusChangeListener(cardNumberHelperText));
        editText.setOnFocusChangeListener(new ViewAlphaChangingTextWatcher(editText, cardTypeImageView));
        editText.setOnFocusChangeListener(new HintFocusListener(editText, getResources().getString(R.string.card_number_format)));

        numberFormatTextWatcher = new NumberFormatTextWatcher(editText, getResources().getString(R.string.card_number_format));
        editText.addTextChangedListener(numberFormatTextWatcher);
        editText.addTextChangedListener(new HidingViewTextWatcher(cardNumberHelperText));
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState());
        bundle.putInt(KEY_CARD_TYPE, cardType);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            final Parcelable superState = bundle.getParcelable(KEY_SUPER_STATE);
            setCardType(bundle.getInt(KEY_CARD_TYPE), false);
            super.onRestoreInstanceState(superState);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public void setScanCardListener(final ScanCardButtonListener listener) {
        if (listener != null) {
            this.scanCardButton.setVisibility(VISIBLE);
            this.cardTypeImageView.setVisibility(GONE);

            this.scanCardButton.setOnClickListener(new SingleClickOnClickListener() {
                @Override
                public void doClick() {
                    listener.onClick();
                }
            });
        } else {
            this.scanCardButton.setVisibility(GONE);
            this.cardTypeImageView.setVisibility(VISIBLE);
            this.scanCardButton.setOnClickListener(null);
        }
    }

    public void setCardType(final int type, final boolean animate) {
        this.cardType = type;

        cardTypeImageView.setImageType(type, animate);
        if (type != 0) {
            setScanCardListener(null);
        }

        if (type == AMEX) {
            setMaxLength(17);
            numberFormatTextWatcher.setFormat(getResources().getString(R.string.amex_card_number_format));
        } else {
            setMaxLength(19);
            numberFormatTextWatcher.setFormat(getResources().getString(R.string.card_number_format));
        }
    }

    public void setText(final String text) {
        editText.setText(text);
    }

    private void setMaxLength(final int maxLength) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    public int getCardType() {
        return CardNetwork.fromCardNumber(editText.getText().toString());
    }

    public String getText() {
        return editText.getText().toString().replaceAll(" ", "");
    }

    public void addTextChangedListener(final SimpleTextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void setTokenCard(final CardToken cardToken) {
        editText.setEnabled(false);
        boolean amex = cardToken.getType() == AMEX;

        editText.removeTextChangedListener(numberFormatTextWatcher);
        editText.setText(getContext().getString(amex ? R.string.amex_token_card_number : R.string.token_card_number, cardToken.getLastFour()));
        editText.addTextChangedListener(numberFormatTextWatcher);

        cardTypeImageView.setAlpha(1.0f);
    }

    public void setValidation(final Validation validation) {
        inputLayout.setErrorEnabled(validation.isShowError());

        if (validation.isShowError()) {
            inputLayout.setError(getResources().getString(validation.getError()));
        } else {
            inputLayout.setError("");
        }
    }

    public JudoEditText getEditText() {
        return editText;
    }

}