package com.hrms.social.engagement;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts @mentions, #hashtags, and URLs from free-form social post content.
 * Used at write time (to send mention notifications) and at index time (search).
 */
@Component
public class MentionsExtractor {

    // @username supports letters/digits/dot/underscore/hyphen, 2-50 chars, preceded by start/space/punctuation
    private static final Pattern MENTION = Pattern.compile("(?<=^|[\\s.,;:!?(\\[{])@([A-Za-z][A-Za-z0-9._-]{1,49})");
    private static final Pattern HASHTAG = Pattern.compile("(?<=^|[\\s.,;:!?(\\[{])#([A-Za-z][A-Za-z0-9_]{1,49})");
    private static final Pattern URL = Pattern.compile("https?://[\\w./?%&=:#~+-]+");

    public Set<String> mentions(String text) { return collect(text, MENTION); }
    public Set<String> hashtags(String text) { return collect(text, HASHTAG); }
    public Set<String> urls(String text) {
        if (text == null) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        Matcher m = URL.matcher(text);
        while (m.find()) out.add(m.group());
        return out;
    }

    public ExtractedTokens extract(String text) {
        return new ExtractedTokens(mentions(text), hashtags(text), urls(text));
    }

    private Set<String> collect(String text, Pattern p) {
        if (text == null) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        Matcher m = p.matcher(text);
        while (m.find()) out.add(m.group(1).toLowerCase());
        return out;
    }

    /** Replaces @user / #tag with anchor tags pointing to your front-end routes. */
    public String linkify(String text, String mentionBase, String hashtagBase) {
        if (text == null || text.isEmpty()) return text;
        String out = MENTION.matcher(text).replaceAll(
                m -> "<a href='" + mentionBase + "/" + m.group(1) + "'>@" + m.group(1) + "</a>");
        out = HASHTAG.matcher(out).replaceAll(
                m -> "<a href='" + hashtagBase + "/" + m.group(1).toLowerCase() + "'>#" + m.group(1) + "</a>");
        return out;
    }

    public record ExtractedTokens(Set<String> mentions, Set<String> hashtags, Set<String> urls) {}
}
