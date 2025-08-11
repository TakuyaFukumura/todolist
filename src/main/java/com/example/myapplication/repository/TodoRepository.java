package com.example.myapplication.repository;

import com.example.myapplication.entity.Todo;
import com.example.myapplication.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Todoエンティティのデータアクセスリポジトリ
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * 指定したユーザーのTodoを全て取得（並び替え可能）
     */
    List<Todo> findByUser(User user, Sort sort);

    /**
     * 指定したユーザーのTodoを全て取得
     */
    List<Todo> findByUser(User user);

    /**
     * 指定したユーザーの指定した状態のTodoを取得
     */
    List<Todo> findByUserAndStatus(User user, Todo.Status status, Sort sort);

    /**
     * 指定したユーザーの指定した優先度のTodoを取得
     */
    List<Todo> findByUserAndPriority(User user, Todo.Priority priority, Sort sort);

    /**
     * 指定したユーザーの期限切れTodoを取得
     */
    @Query("SELECT t FROM Todo t WHERE t.user = :user AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Todo> findOverdueTodos(@Param("user") User user, @Param("currentDate") LocalDate currentDate, Sort sort);

    /**
     * 指定したユーザーの今日期限のTodoを取得
     */
    @Query("SELECT t FROM Todo t WHERE t.user = :user AND t.dueDate = :currentDate AND t.status != 'COMPLETED'")
    List<Todo> findTodosForToday(@Param("user") User user, @Param("currentDate") LocalDate currentDate, Sort sort);

    /**
     * 指定したユーザーの完了済みタスク数を取得
     */
    long countByUserAndStatus(User user, Todo.Status status);

    /**
     * 指定したユーザーの優先度別タスク数を取得
     */
    long countByUserAndPriority(User user, Todo.Priority priority);

    /**
     * 指定したユーザーの総タスク数を取得
     */
    long countByUser(User user);
}
