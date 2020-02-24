package com.judopay;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.judopay.api.model.Credentials;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CredentialsEncodingTest {

    @Test
    public void shouldEncodeTokenAndSecret() {
        String token = "xvTWcOyTn1ib5vvR";
        String secret = "73a577a8dff2459c57461598046bdce22fc1aa89dec75292899a6d33f3af273c";

        Credentials credentials = new Credentials(token, secret);
        assertThat(credentials.getBasicAuthorizationHeader(), equalTo("Basic eHZUV2NPeVRuMWliNXZ2Ujo3M2E1NzdhOGRmZjI0NTljNTc0NjE1OTgwNDZiZGNlMjJmYzFhYTg5ZGVjNzUyOTI4OTlhNmQzM2YzYWYyNzNj"));
//
    }

}