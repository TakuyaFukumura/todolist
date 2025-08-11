package com.example.myapplication.entity;

import lombok.Getter;

/**
 * 優先度列挙型
 */
@Getter
public enum Priority {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }
}