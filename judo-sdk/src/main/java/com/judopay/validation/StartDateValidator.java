package com.judopay.validation;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.judopay.R;
import com.judopay.model.CardDate;

import rx.Observable;
import rx.Subscriber;

public class StartDateValidator implements Validator {

    private final EditText editText;

    public StartDateValidator(EditText editText) {
        this.editText = editText;
    }

    @Override
    public Observable<Validation> onValidate() {
        return Observable.create(new Observable.OnSubscribe<Validation>() {
            @Override
            public void call(final Subscriber<? super Validation> subscriber) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable text) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(getValidation(text.toString()));
                        }
                    }
                });
            }
        });
    }

    private Validation getValidation(String text) {
        boolean startDateEntryComplete = text.length() == 5;
        boolean startDateValid = isStartDateValid(text);
        boolean showStartDateError = !startDateValid && startDateEntryComplete;

        int startDateError = 0;
        if (showStartDateError) {
            startDateError = R.string.check_start_date;
        }

        return new Validation(startDateValid, startDateError, !startDateValid && showStartDateError);
    }

    private boolean isStartDateValid(String startDate) {
        CardDate cardDate = new CardDate(startDate);
        return cardDate.isBeforeToday() && cardDate.isInsideAllowedDateRange();
    }

}
