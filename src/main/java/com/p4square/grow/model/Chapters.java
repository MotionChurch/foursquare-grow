package com.p4square.grow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

/**
 * The chapters of the training section.
 */
public enum Chapters {
    INTRODUCTION,
    SEEKER,
    BELIEVER,
    DISCIPLE,
    TEACHER,
    LEADER;

    /**
     * A case-insensitive version of Chapters.valueOf().
     */
    @JsonCreator
    public static Chapters fromString(String s) {
        return valueOf(s.toUpperCase());
    }

    @JsonValue
    public String identifier() {
        return toString().toLowerCase();
    }

    /**
     * Convert the Chapter to a score, if possible.
     */
    public Optional<Double> toScore() {
        switch (this) {
            case SEEKER:
            case BELIEVER:
            case DISCIPLE:
            case TEACHER:
                return Optional.of(Score.numericScore(this.toString()));
            default:
                return Optional.empty();
        }
    }
}
