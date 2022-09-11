package io.github.dwcarrot.kitpackmgr.storage;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Selector implements Predicate<Player> {

    public static final String EMPTY = "[]";

    private String raw;

    private IRule[] rules;

    public Selector() {
        this.raw = Selector.EMPTY;
        this.rules = null;
    }

    public Selector(@NonNull String s) throws ParseException {
        parse(s);
    }

    public void parse(@NonNull String s) throws ParseException {
        int start = 0;
        int end = s.length();
        if(s.length() < 2) {
            throw new ParseException(s, end);
        }
        if(s.charAt(start) != '[') {
            throw new ParseException(s, start);
        }
        start++;
        if(s.charAt(end - 1) != ']') {
            throw new ParseException(s, end - 1);
        }
        end--;

        List<Range> parts = new ArrayList<>();
        int index = start;
        int objectDepth = 0;
        for(int i = index; i < end; ++i) {
            char c = s.charAt(i);
            switch(c) {
                case '{' -> {
                    objectDepth += 1;
                }
                case '}' -> {
                    objectDepth -= 1;
                    if(objectDepth < 0) {
                        throw new ParseException(s, i);
                    }
                }
                case ',' -> {
                    if(objectDepth == 0) {
                        parts.add(new Range(index, i));
                        index = i + 1;
                    }
                }
            }
        }
        if(index < end) {
            parts.add(new Range(index, end));
        }

        this.raw = s;
        this.rules = new IRule[parts.size()];
        try {
            int i = 0;
            for(Range r : parts) {
                this.rules[i++] = Selector.parseOne(s, r.start, r.end);
            }
        } catch (ParseException e) {
            this.raw = Selector.EMPTY;
            this.rules = null;
            throw e;
        }
    }

    public String getRaw() {
        return this.raw;
    }

    public boolean isEmpty() {
        return this.rules == null;
    }

    public static IRule parseOne(String s, int start, int end) throws ParseException {
        int index = s.indexOf('=', start);
        if(index < 0) {
            throw new ParseException(s, start);
        }
        switch(s.substring(start, index)) {
            case "tag":
                return new TagRule(s, index + 1, end);
            default:
                throw new ParseException(s, start);
        }
    }

    @Override
    public boolean test(Player player) {
        if(this.rules != null) {
            Object[] cache = new Object[TestDataType.max()];
            cache[TestDataType.Tag.index] = player.getScoreboardTags();
            for(IRule rule : this.rules) {
                int i = rule.getDataType().index;
                if(!rule.test(player, cache[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}

class Range {

    public int start;
    public int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }
}

enum TestDataType {
    Tag(0),
    ;

    public final int index;

    TestDataType(int i) {
        this.index = i;
    }

    public static int max() {
        return TestDataType.values().length;
    }
}

interface IRule {

    boolean test(Player player, Object data);

    TestDataType getDataType();
}

class TagRule implements IRule {

    final String name;
    final boolean negative;

    TagRule(String s, int start, int end) {
        boolean negative = false;
        if(s.charAt(start) == '!') {
            negative = true;
            start++;
        }
        this.name = s.substring(start, end);
        this.negative = negative;
    }

    TagRule(String name, boolean negative) {
        this.name = name;
        this.negative = negative;
    }

    @Override
    public boolean test(Player player, Object data) {
        Set<String> tags = (Set<String>) data;
        return tags.contains(this.name) ^ this.negative;
    }

    @Override
    public TestDataType getDataType() {
        return TestDataType.Tag;
    }
}