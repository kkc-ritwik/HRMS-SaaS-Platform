package com.hrms.recruitment.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts text + structured fields (name, email, phone, skills) from a candidate's resume.
 * Powered by Apache Tika so any common format works: PDF, DOC/DOCX, RTF, ODT, HTML, TXT.
 * Output is best-effort heuristic — UI should let the candidate confirm/correct before saving.
 */
@Slf4j
@Service
public class ResumeParser {

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile(
            "(?:(?:\\+|00)\\d{1,3}[\\s-]?)?(?:\\(?\\d{2,4}\\)?[\\s-]?)?\\d{3,4}[\\s-]?\\d{3,6}");
    private static final Pattern LINKEDIN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?linkedin\\.com/in/[\\w-]+", Pattern.CASE_INSENSITIVE);

    /** Common tech skills the parser will recognise. Extend per tenant via config. */
    private static final Set<String> SKILL_KEYWORDS = Set.of(
            "java","python","javascript","typescript","react","angular","vue",
            "spring","springboot","spring boot","node","node.js","express","django","flask",
            "kotlin","scala","go","golang","rust","c++","c#",".net","ruby","rails",
            "aws","gcp","azure","docker","kubernetes","k8s","terraform","jenkins",
            "postgres","postgresql","mysql","mongodb","redis","kafka","elasticsearch",
            "graphql","rest","grpc","sql","nosql",
            "html","css","sass","tailwind","bootstrap",
            "git","ci/cd","linux","bash","powershell",
            "machine learning","ml","ai","tensorflow","pytorch","pandas","numpy",
            "agile","scrum","jira","figma");

    private final Tika tika = new Tika();
    private final AutoDetectParser parser = new AutoDetectParser();

    public ParsedResume parse(InputStream in, String filename) {
        try {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata meta = new Metadata();
            if (filename != null) meta.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
            parser.parse(in, handler, meta);
            String text = handler.toString();
            return extract(text, meta);
        } catch (Exception e) {
            log.warn("Resume parse failed for {}: {}", filename, e.getMessage());
            return ParsedResume.empty();
        }
    }

    public String detectMimeType(InputStream in, String filename) {
        try { return tika.detect(in, filename == null ? "" : filename); }
        catch (Exception e) { return "application/octet-stream"; }
    }

    private ParsedResume extract(String text, Metadata meta) {
        String emailFound = firstMatch(EMAIL, text);
        String phoneFound = firstMatch(PHONE, text);
        String linkedIn = firstMatch(LINKEDIN, text);
        String fullName = guessName(text);
        Set<String> skills = extractSkills(text);
        Integer years = extractYearsOfExperience(text);
        String author = meta.get("Author");
        String title = meta.get("title");

        return ParsedResume.builder()
                .fullName(fullName != null ? fullName : author)
                .email(emailFound)
                .phone(phoneFound == null ? null : phoneFound.trim())
                .linkedInUrl(linkedIn)
                .skills(skills)
                .totalExperienceYears(years)
                .rawText(text.length() > 50000 ? text.substring(0, 50000) : text)
                .documentTitle(title)
                .build();
    }

    private String firstMatch(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : null;
    }

    /** Heuristic: the first non-empty line of the resume is usually the candidate's name. */
    private String guessName(String text) {
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (t.isEmpty() || t.length() > 60) continue;
            if (t.matches("[A-Z][a-zA-Z'-]+(?:\\s[A-Z][a-zA-Z'-]+){1,3}")) return t;
        }
        return null;
    }

    private Set<String> extractSkills(String text) {
        String lower = text.toLowerCase();
        Set<String> found = new LinkedHashSet<>();
        for (String kw : SKILL_KEYWORDS) {
            if (lower.contains(kw)) found.add(kw);
        }
        return found;
    }

    private Integer extractYearsOfExperience(String text) {
        Matcher m = Pattern.compile("(\\d{1,2})\\+?\\s*(?:years|yrs)\\s*(?:of)?\\s*experience",
                Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @lombok.Data @lombok.Builder
    public static class ParsedResume {
        private String fullName;
        private String email;
        private String phone;
        private String linkedInUrl;
        private Set<String> skills;
        private Integer totalExperienceYears;
        private String rawText;
        private String documentTitle;
        public static ParsedResume empty() {
            return ParsedResume.builder().skills(Set.of()).rawText("").build();
        }
    }
}
