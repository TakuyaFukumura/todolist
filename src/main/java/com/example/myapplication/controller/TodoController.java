package com.example.myapplication.controller;

import com.example.myapplication.enums.Priority;
import com.example.myapplication.enums.Status;
import com.example.myapplication.entity.Todo;
import com.example.myapplication.entity.User;
import com.example.myapplication.service.TodoService;
import com.example.myapplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Todo管理コントローラー
 */
@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final UserService userService;

    /**
     * Todo一覧表示
     */
    @GetMapping
    public String list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "sort", defaultValue = "dueDate") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String sortDirection,
            @RequestParam(name = "filter", required = false) String filter,
            Model model) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Todo> todos;
        String filterDisplay = "すべて";

        // フィルター処理
        switch (filter != null ? filter : "all") {
            case "pending":
                todos = todoService.getTodosByStatus(user, Status.PENDING, sortBy, sortDirection);
                filterDisplay = "未開始";
                break;
            case "in_progress":
                todos = todoService.getTodosByStatus(user, Status.IN_PROGRESS, sortBy, sortDirection);
                filterDisplay = "進行中";
                break;
            case "completed":
                todos = todoService.getTodosByStatus(user, Status.COMPLETED, sortBy, sortDirection);
                filterDisplay = "完了";
                break;
            case "high_priority":
                todos = todoService.getTodosByPriority(user, Priority.HIGH, sortBy, sortDirection);
                filterDisplay = "高優先度";
                break;
            case "overdue":
                todos = todoService.getOverdueTodos(user, sortBy, sortDirection);
                filterDisplay = "期限切れ";
                break;
            case "due_today":
                todos = todoService.getTodosForToday(user, sortBy, sortDirection);
                filterDisplay = "今日期限";
                break;
            default:
                todos = todoService.getTodosByUser(user, sortBy, sortDirection);
        }

        // 統計情報
        TodoService.TodoStats stats = todoService.getTodoStats(user);

        model.addAttribute("todos", todos);
        model.addAttribute("stats", stats);
        model.addAttribute("currentSort", sortBy);
        model.addAttribute("currentDirection", sortDirection);
        model.addAttribute("currentFilter", filter != null ? filter : "all");
        model.addAttribute("filterDisplay", filterDisplay);

        return "todos/list";
    }

    /**
     * Todo新規作成フォーム
     */
    @GetMapping("/new")
    public String newTodo(Model model) {
        model.addAttribute("todo", new Todo());
        model.addAttribute("priorities", Priority.values());
        return "todos/form";
    }

    /**
     * Todo編集フォーム
     */
    @GetMapping("/{id}/edit")
    public String editTodo(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Todo> todoOpt = todoService.getTodoById(id);
        if (todoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "指定されたタスクが見つかりません。");
            return "redirect:/todos";
        }

        Todo todo = todoOpt.get();
        if (!todo.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "このタスクを編集する権限がありません。");
            return "redirect:/todos";
        }

        model.addAttribute("todo", todo);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("statuses", Status.values());
        return "todos/form";
    }

    /**
     * Todo作成処理
     */
    @PostMapping
    public String createTodo(@ModelAttribute Todo todo,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "todos/form";
        }

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        todo.setUser(user);
        todoService.createTodo(todo);

        redirectAttributes.addFlashAttribute("success", "新しいタスクを作成しました。");
        return "redirect:/todos";
    }

    /**
     * Todo更新処理
     */
    @PostMapping("/{id}")
    public String updateTodo(@PathVariable Long id,
                             @ModelAttribute Todo todo,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "todos/form";
        }

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Todo> existingTodoOpt = todoService.getTodoById(id);
        if (existingTodoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "指定されたタスクが見つかりません。");
            return "redirect:/todos";
        }

        Todo existingTodo = existingTodoOpt.get();
        if (!existingTodo.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "このタスクを更新する権限がありません。");
            return "redirect:/todos";
        }

        // 既存のTodoの情報を保持
        todo.setId(id);
        todo.setUser(user);
        todo.setCreatedAt(existingTodo.getCreatedAt());

        todoService.updateTodo(todo);

        redirectAttributes.addFlashAttribute("success", "タスクを更新しました。");
        return "redirect:/todos";
    }

    /**
     * Todo削除処理
     */
    @PostMapping("/{id}/delete")
    public String deleteTodo(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Todo> todoOpt = todoService.getTodoById(id);
        if (todoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "指定されたタスクが見つかりません。");
            return "redirect:/todos";
        }

        Todo todo = todoOpt.get();
        if (!todo.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "このタスクを削除する権限がありません。");
            return "redirect:/todos";
        }

        todoService.deleteTodo(id);
        redirectAttributes.addFlashAttribute("success", "タスクを削除しました。");
        return "redirect:/todos";
    }

    /**
     * Todo完了状態切り替え
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public String toggleTodo(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Todo> todoOpt = todoService.getTodoById(id);
        if (todoOpt.isEmpty()) {
            return "error";
        }

        Todo todo = todoOpt.get();
        if (!todo.getUser().getId().equals(user.getId())) {
            return "error";
        }

        try {
            todoService.toggleTodoStatus(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}
