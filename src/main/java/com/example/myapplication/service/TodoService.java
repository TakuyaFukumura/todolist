package com.example.myapplication.service;

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
            if (todo.getStatus() == Todo.Status.COMPLETED) {
                todo.setStatus(Todo.Status.PENDING);
            } else {
                todo.setStatus(Todo.Status.COMPLETED);
            }
            return todoRepository.save(todo);
        }
        throw new RuntimeException("Todo not found: " + id);
    }

    /**
     * 指定したユーザーの状態別Todo取得
     */
    public List<Todo> getTodosByStatus(User user, Todo.Status status, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return todoRepository.findByUserAndStatus(user, status, sort);
    }

    /**
     * 指定したユーザーの優先度別Todo取得
     */
    public List<Todo> getTodosByPriority(User user, Todo.Priority priority, String sortBy, String sortDirection) {
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
        long completedTodos = todoRepository.countByUserAndStatus(user, Todo.Status.COMPLETED);
        long pendingTodos = todoRepository.countByUserAndStatus(user, Todo.Status.PENDING);
        long inProgressTodos = todoRepository.countByUserAndStatus(user, Todo.Status.IN_PROGRESS);
        long highPriorityTodos = todoRepository.countByUserAndPriority(user, Todo.Priority.HIGH);
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

        switch (sortBy) {
            case "priority":
                return Sort.by(direction, "priority").and(Sort.by("dueDate"));
            case "status":
                return Sort.by(direction, "status").and(Sort.by("dueDate"));
            case "createdAt":
                return Sort.by(direction, "createdAt");
            case "title":
                return Sort.by(direction, "title");
            case "dueDate":
            default:
                return Sort.by(direction, "dueDate").and(Sort.by("priority"));
        }
    }

    /**
     * Todo統計情報クラス
     */
    public static class TodoStats {
        private final long totalTodos;
        private final long completedTodos;
        private final long pendingTodos;
        private final long inProgressTodos;
        private final long highPriorityTodos;
        private final long overdueTodos;
        private final long dueTodayTodos;

        public TodoStats(long totalTodos, long completedTodos, long pendingTodos,
                         long inProgressTodos, long highPriorityTodos,
                         long overdueTodos, long dueTodayTodos) {
            this.totalTodos = totalTodos;
            this.completedTodos = completedTodos;
            this.pendingTodos = pendingTodos;
            this.inProgressTodos = inProgressTodos;
            this.highPriorityTodos = highPriorityTodos;
            this.overdueTodos = overdueTodos;
            this.dueTodayTodos = dueTodayTodos;
        }

        public long getTotalTodos() {
            return totalTodos;
        }

        public long getCompletedTodos() {
            return completedTodos;
        }

        public long getPendingTodos() {
            return pendingTodos;
        }

        public long getInProgressTodos() {
            return inProgressTodos;
        }

        public long getHighPriorityTodos() {
            return highPriorityTodos;
        }

        public long getOverdueTodos() {
            return overdueTodos;
        }

        public long getDueTodayTodos() {
            return dueTodayTodos;
        }

        public double getCompletionRate() {
            return totalTodos > 0 ? (double) completedTodos / totalTodos * 100 : 0;
        }
    }
}
