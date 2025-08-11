package com.example.myapplication.entity;

import lombok.Getter;

/**
 * タスク状態列挙型
 */
@Getter
public enum Status {
    PENDING("未開始"),
    IN_PROGRESS("進行中"),
    COMPLETED("完了");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }
}