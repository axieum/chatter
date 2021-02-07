package me.axieum.mcmod.chatter.impl.util;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormat
{
    private static final Logger LOGGER = LogManager.getLogger("Chatter");

    private final LinkedHashMap<Pattern, TokenReplacer> REPLACEMENTS = new LinkedHashMap<>();
    private static final String START_FE = "\\$\\{", END_FE = "}";

    /**
     * Constructs a new Message Format instance.
     */
    public MessageFormat() {}

    /**
     * Adds a string substitution.
     *
     * @param target      string to be substituted
     * @param replacement string to replace with
     * @return a reference to this object
     */
    public MessageFormat substitute(String target, String replacement)
    {
        return regex(Pattern.compile(Pattern.quote(target)), match -> replacement);
    }

    /**
     * Adds a new token replacement.
     *
     * @param token       token name to match
     * @param replacement plaintext replacement
     * @return a reference to this object
     * @see MessageFormat#tokenize(String, TokenReplacer)
     */
    public MessageFormat tokenize(String token, String replacement)
    {
        return tokenize(token, match -> replacement);
    }

    /**
     * Adds a new lazy token replacement.
     *
     * @param token    token name to match
     * @param supplier plaintext replacement supplier
     * @return a reference to this object
     */
    public MessageFormat tokenize(String token, Supplier<String> supplier)
    {
        return tokenize(token, match -> supplier.get());
    }

    /**
     * Adds a new functional token replacement.
     *
     * @param token    token name to match
     * @param replacer match replacer with groups: all, token, arguments
     * @return a reference to this object
     */
    public MessageFormat tokenize(String token, TokenReplacer replacer)
    {
        return regex(Pattern.compile(START_FE + "(" + Pattern.quote(token) + ")(?::(.*?))?" + END_FE), replacer);
    }

    /**
     * Adds a new date time token replacement using the current date and time.
     *
     * @param token token name to match
     * @return a reference to this object
     * @see #datetime(String, LocalDateTime)
     */
    public MessageFormat datetime(String token)
    {
        return datetime(token, LocalDateTime.now());
    }

    /**
     * Adds a new date time token replacement.
     *
     * @param token    token name to match
     * @param dateTime date time to use for replacements
     * @return a reference to this object
     */
    public MessageFormat datetime(String token, LocalDateTime dateTime)
    {
        return tokenize(token, match -> {
            try {
                return dateTime.format(DateTimeFormatter.ofPattern(match.get(2)));
            } catch (Exception e) {
                LOGGER.warn("Invalid message template encountered for token '{}': {}", token, e.getMessage());
                return "";
            }
        });
    }

    /**
     * Adds a new lazy date time token replacement.
     *
     * @param token    token name to match
     * @param supplier date time to use for replacements
     * @return a reference to this object
     */
    public MessageFormat datetime(String token, Supplier<LocalDateTime> supplier)
    {
        return tokenize(token, match -> {
            try {
                return supplier.get().format(DateTimeFormatter.ofPattern(match.get(2)));
            } catch (Exception e) {
                LOGGER.warn("Invalid message template encountered for token '{}': {}", token, e.getMessage());
                return "";
            }
        });
    }

    /**
     * Adds a new duration token replacement.
     *
     * @param token    token name to match
     * @param duration duration to use for replacements
     * @return a reference to this object
     */
    public MessageFormat duration(String token, Duration duration)
    {
        final long millis = Math.abs(duration.toMillis());
        return tokenize(token, match -> match.size() > 2 ? DurationFormatUtils.formatDuration(millis, match.get(2))
                                                         : DurationFormatUtils.formatDurationWords(millis, true, true));
    }

    /**
     * Adds a new lazy duration token replacement.
     *
     * @param token    token name to match
     * @param supplier duration supplier to use for replacements
     * @return a reference to this object
     */
    public MessageFormat duration(String token, Supplier<Duration> supplier)
    {
        return tokenize(token, match -> {
            final long millis = Math.abs(supplier.get().toMillis());
            return match.size() > 2 ? DurationFormatUtils.formatDuration(millis, match.get(2))
                                    : DurationFormatUtils.formatDurationWords(millis, true, true);
        });
    }

    /**
     * Adds a new regex replacement.
     *
     * @param regex    regex pattern to match
     * @param replacer match replacer
     * @return a reference to this object
     */
    public MessageFormat regex(Pattern regex, TokenReplacer replacer)
    {
        REPLACEMENTS.put(regex, replacer);
        return this;
    }

    /**
     * Applies this formatter to a given message template to obtain a new
     * formatted message.
     *
     * @param template message template to format
     * @return formatted message
     */
    public String apply(String template)
    {
        // Immediately bail on an empty template
        if (template == null || template.isEmpty()) return "";

        // Handle replacements
        String message = template;
        for (Map.Entry<Pattern, TokenReplacer> entry : REPLACEMENTS.entrySet()) {
            final Pattern token = entry.getKey();
            final TokenReplacer replacer = entry.getValue();

            Matcher matcher = token.matcher(message);
            if (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                do {
                    // Fetch regex groups
                    final int groupCount = matcher.groupCount();
                    ArrayList<String> groups = new ArrayList<>(groupCount);
                    for (int i = 0; i <= groupCount; i++) {
                        String group = matcher.group(i);
                        if (group != null) groups.add(group);
                    }
                    // Append the replacement supplied by the functional callback
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(replacer.replace(groups)));
                } while (matcher.find());
                matcher.appendTail(sb);
                message = sb.toString();
            }
        }

        return message;
    }

    public interface TokenReplacer
    {
        /**
         * Computes the replacement text for a regex match.
         *
         * @param groups list of captured regex groups - first is entire match
         * @return replacement string for entire regex match
         */
        String replace(List<String> groups);
    }
}
