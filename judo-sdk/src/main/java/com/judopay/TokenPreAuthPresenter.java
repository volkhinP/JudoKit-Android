package com.judopay;

import com.google.gson.Gson;
import com.judopay.arch.Scheduler;
import com.judopay.model.Card;
import com.judopay.model.TokenRequest;

import java.math.BigDecimal;

class TokenPreAuthPresenter extends BasePresenter {

    public TokenPreAuthPresenter(PaymentFormView view, JudoApiService judoApiService, Scheduler scheduler, Gson gson) {
        super(view, judoApiService, scheduler, gson);
    }

    public void performTokenPreAuth(Card card, JudoOptions options) {
        this.loading = true;

        paymentFormView.showLoading();

        TokenRequest tokenTransaction = new TokenRequest.Builder()
                .setAmount(new BigDecimal(options.getAmount()))
                .setCardAddress(card.getCardAddress())
                .setCurrency(options.getCurrency())
                .setJudoId(options.getJudoId())
                .setYourConsumerReference(options.getConsumerRef())
                .setCv2(card.getCv2())
                .setEmailAddress(options.getEmailAddress())
                .setMobileNumber(options.getMobileNumber())
                .setMetaData(options.getMetaDataMap())
                .setToken(options.getCardToken())
                .build();

        apiService.tokenPreAuth(tokenTransaction)
                .subscribeOn(scheduler.backgroundThread())
                .observeOn(scheduler.mainThread())
                .subscribe(callback(), error());
    }

}