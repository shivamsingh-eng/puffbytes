package com.puffbytes.puffbytes.authentication.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.puffbytes.puffbytes.authentication.config.GoogleClientProperties;
import com.puffbytes.puffbytes.common.exception.InvalidOrExpiredTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private final GoogleClientProperties googleClient;

    public GoogleIdToken.Payload verify(String idTokenString) {

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClient.getId()))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                return idToken.getPayload();
            }

        } catch (Exception e) {
            throw new InvalidOrExpiredTokenException("Invalid Google token");
        }

        throw new InvalidOrExpiredTokenException("Invalid Google token");
    }
}
