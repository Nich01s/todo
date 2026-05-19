package com.todo.service;

import com.todo.dto.*;
import com.todo.entity.Todo;
import com.todo.common.BizException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TodoServiceImplTest {

    @Autowired private TodoService todoService;
    @Autowired private AuthService authService;

    private Long currentUserId;

    @BeforeEach
    void setUp() {
        var req = new RegisterRequest();
        req.setUsername("todos-user-" + System.nanoTime());
        req.setPassword("password");
        currentUserId = authService.register(req).getUserId();
    }

    private Todo createSampleTodo(String title) {
        var req = new TodoCreateRequest();
        req.setTitle(title);
        return todoService.create(currentUserId, req);
    }

    @Test
    void shouldCreateTodo() {
        var req = new TodoCreateRequest();
        req.setTitle("Buy groceries");
        req.setPriority(2);
        req.setDueDate(LocalDate.now().plusDays(3));

        var todo = todoService.create(currentUserId, req);

        assertThat(todo.getId()).isNotNull();
        assertThat(todo.getTitle()).isEqualTo("Buy groceries");
        assertThat(todo.getPriority()).isEqualTo(2);
        assertThat(todo.getCompleted()).isEqualTo(0);
        assertThat(todo.getDueDate()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(todo.getUserId()).isEqualTo(currentUserId);
    }

    @Test
    void shouldDefaultPriorityToMedium() {
        var req = new TodoCreateRequest();
        req.setTitle("Read book");

        var todo = todoService.create(currentUserId, req);

        assertThat(todo.getPriority()).isEqualTo(1);
    }

    @Test
    void shouldQueryTodosByCompletionStatus() {
        // Create two pending todos
        createSampleTodo("Task A");
        createSampleTodo("Task B");

        // Toggle one to completed
        var todos = todoService.query(currentUserId, 0, null, null, null, null, 1, 10);
        var toggleMe = todos.getRecords().get(0);
        todoService.toggle(currentUserId, toggleMe.getId());

        // Query completed
        var completed = todoService.query(currentUserId, 1, null, null, null, null, 1, 10);
        assertThat(completed.getTotal()).isEqualTo(1);
        assertThat(completed.getRecords().get(0).getCompleted()).isEqualTo(1);

        // Query pending
        var pending = todoService.query(currentUserId, 0, null, null, null, null, 1, 10);
        assertThat(pending.getTotal()).isEqualTo(1);
        assertThat(pending.getRecords().get(0).getCompleted()).isEqualTo(0);
    }

    @Test
    void shouldToggleCompletion() {
        var todo = createSampleTodo("Toggle me");

        assertThat(todo.getCompleted()).isEqualTo(0);

        var toggled = todoService.toggle(currentUserId, todo.getId());
        assertThat(toggled.getCompleted()).isEqualTo(1);

        var untoggled = todoService.toggle(currentUserId, todo.getId());
        assertThat(untoggled.getCompleted()).isEqualTo(0);
    }

    @Test
    void shouldDeleteOwnTodo() {
        var todo = createSampleTodo("Delete me");

        todoService.delete(currentUserId, todo.getId());

        // Verifying deletion: updating a deleted todo should throw
        var updateReq = new TodoUpdateRequest();
        updateReq.setTitle("Updated");
        assertThatThrownBy(() -> todoService.update(currentUserId, todo.getId(), updateReq))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("待办不存在");
    }

    @Test
    void shouldNotAccessOtherUsersTodos() {
        var todo = createSampleTodo("My todo");

        assertThatThrownBy(() -> todoService.delete(99999L, todo.getId()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("待办不存在");
    }

    @Test
    void shouldNotToggleOtherUsersTodo() {
        var todo = createSampleTodo("My todo");

        assertThatThrownBy(() -> todoService.toggle(99999L, todo.getId()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("待办不存在");
    }

    @Test
    void shouldNotUpdateOtherUsersTodo() {
        var todo = createSampleTodo("My todo");

        var updateReq = new TodoUpdateRequest();
        updateReq.setTitle("Hacked");
        assertThatThrownBy(() -> todoService.update(99999L, todo.getId(), updateReq))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("待办不存在");
    }

    @Test
    void shouldSearchByKeyword() {
        createSampleTodo("Learn Spring Boot");
        createSampleTodo("Buy coffee");

        var result = todoService.query(currentUserId, null, null, null, "Spring", null, 1, 10);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().get(0).getTitle()).contains("Spring");
    }

    @Test
    void shouldSortByDueDate() {
        var req1 = new TodoCreateRequest();
        req1.setTitle("Early task");
        req1.setDueDate(LocalDate.now().plusDays(1));
        todoService.create(currentUserId, req1);

        var req2 = new TodoCreateRequest();
        req2.setTitle("Later task");
        req2.setDueDate(LocalDate.now().plusDays(10));
        todoService.create(currentUserId, req2);

        var result = todoService.query(currentUserId, null, null, null, null, "due_date", 1, 10);
        var records = result.getRecords();
        assertThat(records.get(0).getDueDate()).isBefore(records.get(1).getDueDate());
    }

    @Test
    void shouldSortByPriority() {
        var req1 = new TodoCreateRequest();
        req1.setTitle("Low prio");
        req1.setPriority(0);
        todoService.create(currentUserId, req1);

        var req2 = new TodoCreateRequest();
        req2.setTitle("High prio");
        req2.setPriority(2);
        todoService.create(currentUserId, req2);

        var result = todoService.query(currentUserId, null, null, null, null, "priority", 1, 10);
        var records = result.getRecords();
        assertThat(records.get(0).getPriority()).isEqualTo(2); // highest first
    }

    @Test
    void shouldFilterByPriority() {
        createSampleTodo("No priority set"); // defaults to 1

        var req = new TodoCreateRequest();
        req.setTitle("High prio task");
        req.setPriority(2);
        todoService.create(currentUserId, req);

        var highPrio = todoService.query(currentUserId, null, 2, null, null, null, 1, 10);
        assertThat(highPrio.getTotal()).isEqualTo(1);
        assertThat(highPrio.getRecords().get(0).getTitle()).isEqualTo("High prio task");
    }

    @Test
    void shouldUpdateTodoFields() {
        var todo = createSampleTodo("Original title");

        var updateReq = new TodoUpdateRequest();
        updateReq.setTitle("Updated title");
        updateReq.setDescription("New description");

        var updated = todoService.update(currentUserId, todo.getId(), updateReq);

        assertThat(updated.getTitle()).isEqualTo("Updated title");
        assertThat(updated.getDescription()).isEqualTo("New description");
    }

    @Test
    void shouldReturnEmptyPageWhenNoTodos() {
        var result = todoService.query(currentUserId, null, null, null, null, null, 1, 10);
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getRecords()).isEmpty();
    }
}
