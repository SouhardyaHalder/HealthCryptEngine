package org.example.healthcryptengine.service;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

@Service
public class GoogleCalendarService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private static final List<String> SCOPES =
        Collections.singletonList(CalendarScopes.CALENDAR);

    private GoogleAuthorizationCodeFlow flow;

    private GoogleAuthorizationCodeFlow getFlow() throws Exception {
        if (flow == null) {
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(clientId);
            details.setClientSecret(clientSecret);

            GoogleClientSecrets secrets = new GoogleClientSecrets();
            secrets.setInstalled(details);

            flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                secrets,
                SCOPES
            ).setAccessType("offline").build();
        }
        return flow;
    }

    // Generate Google login URL
    public String getAuthUrl() throws Exception {
        return getFlow()
            .newAuthorizationUrl()
            .setRedirectUri(redirectUri)
            .build();
    }

    // Exchange code for token
    public Credential getCredential(String code) throws Exception {
        TokenResponse response = getFlow()
            .newTokenRequest(code)
            .setRedirectUri(redirectUri)
            .execute();
        return getFlow().createAndStoreCredential(response, "user");
    }

    // Create a Pomodoro event in Google Calendar
    public void createPomodoroEvent(Credential credential, int durationMins) throws Exception {
        Calendar service = new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("HealthCryptEngine").build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMinutes(durationMins);
        ZoneId zone = ZoneId.of("Asia/Kolkata");

        Event event = new Event()
            .setSummary("🍅 Pomodoro - " + durationMins + "min Focus")
            .setDescription("Focus session logged by HealthCrypt Engine");

        DateTime startDt = new DateTime(
            ZonedDateTime.of(now, zone).toInstant().toEpochMilli());
        DateTime endDt = new DateTime(
            ZonedDateTime.of(end, zone).toInstant().toEpochMilli());

        event.setStart(new EventDateTime().setDateTime(startDt));
        event.setEnd(new EventDateTime().setDateTime(endDt));

        service.events().insert("primary", event).execute();
    }

    public Credential buildCredentialFromToken(String accessToken,
                                            String refreshToken,
                                            Long expiry) throws Exception {
    Credential credential = new Credential.Builder(
        com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
        .setTransport(GoogleNetHttpTransport.newTrustedTransport())
        .setJsonFactory(JacksonFactory.getDefaultInstance())
        .setTokenServerUrl(new com.google.api.client.http.GenericUrl(
            "https://oauth2.googleapis.com/token"))
        .setClientAuthentication(
            new com.google.api.client.auth.oauth2.ClientParametersAuthentication(
                clientId, clientSecret))
        .build();

    credential.setAccessToken(accessToken);
    credential.setRefreshToken(refreshToken);
    credential.setExpirationTimeMilliseconds(expiry);
    return credential;
}
}