package com.example.myapplication.service;

import com.example.myapplication.entity.Priority;
import com.example.myapplication.entity.Status;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.entity.User;
import com.example.myapplication.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Todoサービス - ビジネスロジック層
 */
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    /**
     * 指定したユーザーのTodo一覧を取得（並び替え指定可能）
     */
    public List<Todo> getTodosByUser(User user, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return todoRepository.findByUser(user, sort);
    }

    /**
     * 指定したユーザーのTodo一覧を取得（デフォルト並び替え）
     */
    public List<Todo> getTodosByUser(User user) {
        return getTodosByUser(user, "dueDate", "asc");
    }

    /**
     * TodoをIDで取得
     */
    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    /**
     * 新しいTodoを作成
     */
    @Transactional
    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    /**
     * Todoを更新
     */
    @Transactional
    public Todo updateTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    /**
     * Todoを削除
     */
    @Transactional
    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }

    /**
     * Todoの完了状態をトグル
     */
    @Transactional
    public Todo toggleTodoStatus(Long id) {
        Optional<Todo> todoOpt = todoRepository.findById(id);
        if (todoOpt.isPresent()) {
            Todo todo = todoOpt.get();
            if (todo.getStatus() == Status.COMPLETED) {
                todo.setStatus(Status.PENDING);
            } else {
                todo.setStatus(Status.COMPLETED);
            }
            return todoRepository.save(todo);
        }
        throw new RuntimeException("Todo not found: " + id);
    }

    /**
     * 指定したユーザーの状態別Todo取得
     */
    public List<Todo> getTodosByStatus(User user, Status status, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return todoRepository.findByUserAndStatus(user, status, sort);
    }

    /**
     * 指定したユーザーの優先度別Todo取得
     */
    public List<Todo> getTodosByPriority(User user, Priority priority, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return todoRepository.findByUserAndPriority(user, priority, sort);
    }

    /**
     * 指定したユーザーの期限切れTodo取得
     */
    public List<Todo> getOverdueTodos(User user, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return todoRepository.findOverdueTodos(user, LocalDate.now(), sort);
    }

    /**
     * 指定したユーザーの今日期限Todo取得
     */
    public List<Todo> getTodosForToday(User user, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return todoRepository.findTodosForToday(user, LocalDate.now(), sort);
    }

    /**
     * 統計情報取得
     */
    public TodoStats getTodoStats(User user) {
        long totalTodos = todoRepository.countByUser(user);
        long completedTodos = todoRepository.countByUserAndStatus(user, Status.COMPLETED);
        long pendingTodos = todoRepository.countByUserAndStatus(user, Status.PENDING);
        long inProgressTodos = todoRepository.countByUserAndStatus(user, Status.IN_PROGRESS);
        long highPriorityTodos = todoRepository.countByUserAndPriority(user, Priority.HIGH);
        long overdueTodos = todoRepository.findOverdueTodos(user, LocalDate.now(), Sort.unsorted()).size();
        long dueTodayTodos = todoRepository.findTodosForToday(user, LocalDate.now(), Sort.unsorted()).size();

        return new TodoStats(totalTodos, completedTodos, pendingTodos, inProgressTodos,
                highPriorityTodos, overdueTodos, dueTodayTodos);
    }

    /**
     * ソート条件を作成
     */
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        // デフォルトのソート条件を設定
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "dueDate";
        }

        return switch (sortBy) {
            case "priority" -> Sort.by(direction, "priority").and(Sort.by("dueDate"));
            case "status" -> Sort.by(direction, "status").and(Sort.by("dueDate"));
            case "createdAt" -> Sort.by(direction, "createdAt");
            case "title" -> Sort.by(direction, "title");
            default -> Sort.by(direction, "dueDate").and(Sort.by("priority"));
        };
    }

    /**
     * Todo統計情報クラス
     */
    public record TodoStats(long totalTodos, long completedTodos, long pendingTodos, long inProgressTodos,
                            long highPriorityTodos, long overdueTodos, long dueTodayTodos) {

        public double getCompletionRate() {
            return totalTodos > 0 ? (double) completedTodos / totalTodos * 100 : 0;
        }
    }
}
