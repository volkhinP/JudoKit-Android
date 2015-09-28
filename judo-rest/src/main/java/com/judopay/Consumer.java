package com.judopay;

public class Consumer {

    private String consumerToken;
    private String yourConsumerReference;

    public String getConsumerToken() {
        return consumerToken;
    }

    public String getYourConsumerReference() {
        return yourConsumerReference;
    }

    @Override
    public String toString() {
        return "Consumer{" +
                "consumerToken='" + consumerToken + '\'' +
                ", yourConsumerReference='" + yourConsumerReference + '\'' +
                '}';
    }
}
