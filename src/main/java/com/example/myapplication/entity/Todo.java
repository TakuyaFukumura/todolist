package com.example.myapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ToDoタスクエンティティ
 */
@Entity
@Table(name = "todo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * タスクタイトル
     */
    @Column(nullable = false, length = 255)
    private String title;

    /**
     * タスク詳細説明
     */
    @Column(length = 1000)
    private String description;

    /**
     * 期限日
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * 優先度
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    /**
     * タスク状態
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    /**
     * 作成日時
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 所有者ユーザーID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 優先度列挙型
     */
    public enum Priority {
        HIGH("高"), MEDIUM("中"), LOW("低");
        
        private final String displayName;
        
        Priority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * タスク状態列挙型
     */
    public enum Status {
        PENDING("未開始"), IN_PROGRESS("進行中"), COMPLETED("完了");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * タスクが期限切れかどうか判定
     */
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && status != Status.COMPLETED;
    }

    /**
     * タスクが今日期限かどうか判定
     */
    public boolean isDueToday() {
        return dueDate != null && dueDate.equals(LocalDate.now()) && status != Status.COMPLETED;
    }
}
