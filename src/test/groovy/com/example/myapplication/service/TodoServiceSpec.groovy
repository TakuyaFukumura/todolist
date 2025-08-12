package com.example.myapplication.service

import com.example.myapplication.enums.Priority
import com.example.myapplication.enums.Status
import com.example.myapplication.entity.Todo
import com.example.myapplication.entity.User
import com.example.myapplication.repository.TodoRepository
import spock.lang.Specification

import java.time.LocalDate

/**
 * TodoServiceのSpockテスト
 * Todoサービスの基本機能をテストする
 */
class TodoServiceSpec extends Specification {

    def todoRepository = Mock(TodoRepository)
    def todoService = new TodoService(todoRepository)

    def "新しいTodoが正常に作成されること"() {
        given: "新しいTodoエンティティ"
        def user = new User(id: 1L, username: "testuser", password: "password", roles: "USER")
        def todo = new Todo(
                title: "テストタスク",
                description: "テスト用の説明",
                dueDate: LocalDate.now().plusDays(1),
                priority: Priority.HIGH,
                status: Status.PENDING,
                user: user
        )

        when: "Todoを作成"
        def result = todoService.createTodo(todo)

        then: "リポジトリのsaveが1回呼び出される"
        1 * todoRepository.save(todo) >> todo
        and: "作成されたTodoが返される"
        result == todo
    }

    def "Todoの状態が正常にトグルされること"() {
        given: "既存のTodo"
        def user = new User(id: 1L, username: "testuser", password: "password", roles: "USER")
        def todo = new Todo(
                id: 1L,
                title: "テストタスク",
                status: Status.PENDING,
                user: user
        )

        when: "Todoの状態をトグル"
        def result = todoService.toggleTodoStatus(1L)

        then: "リポジトリのfindByIdが1回呼び出される"
        1 * todoRepository.findById(1L) >> Optional.of(todo)
        and: "リポジトリのsaveが1回呼び出される"
        1 * todoRepository.save(_) >> { Todo t ->
            assert t.status == Status.COMPLETED
            return t
        }
        and: "結果が返される"
        result != null
    }

    def "存在しないTodoの状態をトグルしようとすると例外が発生すること"() {
        when: "存在しないTodoの状態をトグル"
        todoService.toggleTodoStatus(999L)

        then: "リポジトリのfindByIdが1回呼び出される"
        1 * todoRepository.findById(999L) >> Optional.empty()
        and: "RuntimeExceptionが発生する"
        thrown(RuntimeException)
    }

    def "ユーザーのTodo統計が正常に取得されること"() {
        given: "テストユーザー"
        def user = new User(id: 1L, username: "testuser", password: "password", roles: "USER")

        when: "Todo統計を取得"
        def stats = todoService.getTodoStats(user)

        then: "各統計メソッドが適切に呼び出される"
        1 * todoRepository.countByUser(user) >> 10L
        1 * todoRepository.countByUserAndStatus(user, Status.COMPLETED) >> 6L
        1 * todoRepository.countByUserAndStatus(user, Status.PENDING) >> 3L
        1 * todoRepository.countByUserAndStatus(user, Status.IN_PROGRESS) >> 1L
        1 * todoRepository.countByUserAndPriority(user, Priority.HIGH) >> 2L
        1 * todoRepository.findOverdueTodos(user, _, _) >> []
        1 * todoRepository.findTodosForToday(user, _, _) >> []

        and: "統計情報が正しく返される"
        stats.totalTodos == 10L
        stats.completedTodos == 6L
        stats.pendingTodos == 3L
        stats.inProgressTodos == 1L
        stats.highPriorityTodos == 2L
        stats.completionRate == 60.0
    }
}