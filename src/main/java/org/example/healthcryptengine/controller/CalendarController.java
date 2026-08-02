package org.example.healthcryptengine.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import jakarta.servlet.http.HttpSession;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String showCalendar(HttpSession session, Model model) throws Exception {
        Credential credential = (Credential) session.getAttribute("googleCredential");

        if (credential == null) {
            return "redirect:/google/auth";
        }

        Calendar service = new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("HealthCryptEngine").build();

        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate lastOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        DateTime timeMin = new DateTime(
            firstOfMonth.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli());
        DateTime timeMax = new DateTime(
            lastOfMonth.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli());

        // Fetch Pomodoro events
        Map<String, Integer> dailyFocus = new HashMap<>();
        try {
            Events events = service.events().list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setQ("Pomodoro")
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

            for (Event event : events.getItems()) {
                String date = event.getStart().getDateTime()
                    .toStringRfc3339().substring(0, 10);
                String summary = event.getSummary();
                int mins = 25;
                try {
                    String numStr = summary.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) mins = Integer.parseInt(numStr.substring(0, Math.min(2, numStr.length())));
                } catch (Exception ignored) {}
                dailyFocus.merge(date, mins, Integer::sum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Build calendar cells
        List<Map<String, Object>> cells = new ArrayList<>();

        // Empty cells for days before month starts (0=Sun, 1=Mon...)
        int startDow = firstOfMonth.getDayOfWeek().getValue() % 7; // Sun=0
        for (int i = 0; i < startDow; i++) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("empty", true);
            empty.put("day", 0);
            empty.put("focusMins", 0);
            empty.put("today", false);
            cells.add(empty);
        }

        // Actual days
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int d = 1; d <= today.lengthOfMonth(); d++) {
            LocalDate date = today.withDayOfMonth(d);
            String dateStr = date.format(fmt);
            Map<String, Object> cell = new HashMap<>();
            cell.put("empty", false);
            cell.put("day", d);
            cell.put("today", date.equals(today));
            cell.put("focusMins", dailyFocus.getOrDefault(dateStr, 0));
            cells.add(cell);
        }

        String monthLabel = today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        model.addAttribute("calendarCells", cells);
        model.addAttribute("monthLabel", monthLabel);
        model.addAttribute("today", today.toString());

        return "calendar";
    }
}