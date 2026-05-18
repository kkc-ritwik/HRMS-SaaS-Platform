package com.hrms.recruitment.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pushes interview events into the interviewer's Google Calendar or Outlook/Microsoft 365
 * calendar with Google Meet / Teams meeting link auto-generated.
 *
 * Tokens are stored externally (user-level OAuth flow handled by service-auth or front-end);
 * this service receives a freshly-refreshed access token in each call.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarSyncService {

    @Value("${hrms.recruitment.calendar.google-enabled:true}") private boolean googleEnabled;
    @Value("${hrms.recruitment.calendar.outlook-enabled:true}") private boolean outlookEnabled;

    private final RestTemplate rest = new RestTemplate();

    public CalendarEventResult createGoogleEvent(String accessToken, CalendarEventRequest req) {
        if (!googleEnabled || accessToken == null || accessToken.isBlank()) {
            log.warn("Google Calendar skipped (disabled or no token)");
            return new CalendarEventResult(null, null, "SKIPPED");
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(accessToken);
            h.setContentType(MediaType.APPLICATION_JSON);

            List<Map<String, String>> attendees = req.attendeeEmails().stream()
                    .map(e -> Map.of("email", e)).toList();
            Map<String, Object> body = Map.of(
                    "summary", req.title(),
                    "description", req.description() == null ? "" : req.description(),
                    "start", Map.of("dateTime", req.startsAt().toString(), "timeZone", req.timeZone()),
                    "end",   Map.of("dateTime", req.endsAt().toString(),   "timeZone", req.timeZone()),
                    "attendees", attendees,
                    "conferenceData", Map.of(
                            "createRequest", Map.of(
                                    "requestId", UUID.randomUUID().toString(),
                                    "conferenceSolutionKey", Map.of("type", "hangoutsMeet"))),
                    "reminders", Map.of("useDefault", true));

            ResponseEntity<Map> resp = rest.exchange(
                    "https://www.googleapis.com/calendar/v3/calendars/primary/events?conferenceDataVersion=1",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Map<String, Object> b = resp.getBody() == null ? Map.of() : resp.getBody();
            String meetUrl = extractGoogleMeetUrl(b);
            return new CalendarEventResult(str(b.get("id")), meetUrl, "CREATED");
        } catch (Exception e) {
            log.error("Google Calendar create failed: {}", e.getMessage());
            return new CalendarEventResult(null, null, "FAILED: " + e.getMessage());
        }
    }

    public CalendarEventResult createOutlookEvent(String accessToken, CalendarEventRequest req) {
        if (!outlookEnabled || accessToken == null || accessToken.isBlank()) {
            log.warn("Outlook Calendar skipped (disabled or no token)");
            return new CalendarEventResult(null, null, "SKIPPED");
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(accessToken);
            h.setContentType(MediaType.APPLICATION_JSON);

            List<Map<String, Object>> attendees = req.attendeeEmails().stream()
                    .map(e -> (Map<String, Object>) Map.of(
                            "emailAddress", Map.of("address", e),
                            "type", "required")).toList();
            Map<String, Object> body = Map.of(
                    "subject", req.title(),
                    "body", Map.of("contentType", "HTML",
                            "content", req.description() == null ? "" : req.description()),
                    "start", Map.of("dateTime", req.startsAt().toString(), "timeZone", req.timeZone()),
                    "end",   Map.of("dateTime", req.endsAt().toString(),   "timeZone", req.timeZone()),
                    "attendees", attendees,
                    "isOnlineMeeting", true,
                    "onlineMeetingProvider", "teamsForBusiness");

            ResponseEntity<Map> resp = rest.exchange(
                    "https://graph.microsoft.com/v1.0/me/events",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Map<String, Object> b = resp.getBody() == null ? Map.of() : resp.getBody();
            String joinUrl = null;
            Object om = b.get("onlineMeeting");
            if (om instanceof Map<?, ?> m) joinUrl = String.valueOf(m.get("joinUrl"));
            return new CalendarEventResult(str(b.get("id")), joinUrl, "CREATED");
        } catch (Exception e) {
            log.error("Outlook Calendar create failed: {}", e.getMessage());
            return new CalendarEventResult(null, null, "FAILED: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractGoogleMeetUrl(Map<String, Object> body) {
        Object cd = body.get("conferenceData");
        if (!(cd instanceof Map<?, ?> m)) return null;
        Object entryPoints = ((Map<String, Object>) m).get("entryPoints");
        if (!(entryPoints instanceof List<?> list)) return null;
        for (Object ep : list) {
            if (ep instanceof Map<?, ?> em && "video".equals(em.get("entryPointType"))) {
                return String.valueOf(em.get("uri"));
            }
        }
        return null;
    }

    private String str(Object o) { return o == null ? null : o.toString(); }

    public record CalendarEventRequest(String title, String description,
                                        OffsetDateTime startsAt, OffsetDateTime endsAt,
                                        String timeZone, List<String> attendeeEmails) {}
    public record CalendarEventResult(String externalEventId, String meetingUrl, String status) {}
}
