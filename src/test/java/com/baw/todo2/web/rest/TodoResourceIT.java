package com.baw.todo2.web.rest;

import static com.baw.todo2.domain.TodoAsserts.*;
import static com.baw.todo2.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.baw.todo2.IntegrationTest;
import com.baw.todo2.domain.Todo;
import com.baw.todo2.repository.TodoRepository;
import com.baw.todo2.repository.UserRepository;
import com.baw.todo2.service.TodoService;
import com.baw.todo2.service.dto.TodoDTO;
import com.baw.todo2.service.mapper.TodoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TodoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TodoResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/todos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private TodoRepository todoRepositoryMock;

    @Autowired
    private TodoMapper todoMapper;

    @Mock
    private TodoService todoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTodoMockMvc;

    private Todo todo;

    private Todo insertedTodo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Todo createEntity() {
        return new Todo().title(DEFAULT_TITLE).status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Todo createUpdatedEntity() {
        return new Todo().title(UPDATED_TITLE).status(UPDATED_STATUS);
    }

    @BeforeEach
    public void initTest() {
        todo = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedTodo != null) {
            todoRepository.delete(insertedTodo);
            insertedTodo = null;
        }
    }

    @Test
    @Transactional
    void createTodo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);
        var returnedTodoDTO = om.readValue(
            restTodoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(todoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TodoDTO.class
        );

        // Validate the Todo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTodo = todoMapper.toEntity(returnedTodoDTO);
        assertTodoUpdatableFieldsEquals(returnedTodo, getPersistedTodo(returnedTodo));

        insertedTodo = returnedTodo;
    }

    @Test
    @Transactional
    void createTodoWithExistingId() throws Exception {
        // Create the Todo with an existing ID
        todo.setId(1L);
        TodoDTO todoDTO = todoMapper.toDto(todo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTodoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(todoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllTodos() throws Exception {
        // Initialize the database
        insertedTodo = todoRepository.saveAndFlush(todo);

        // Get all the todoList
        restTodoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(todo.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTodosWithEagerRelationshipsIsEnabled() throws Exception {
        when(todoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTodoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(todoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllTodosWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(todoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restTodoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(todoRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getTodo() throws Exception {
        // Initialize the database
        insertedTodo = todoRepository.saveAndFlush(todo);

        // Get the todo
        restTodoMockMvc
            .perform(get(ENTITY_API_URL_ID, todo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(todo.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS));
    }

    @Test
    @Transactional
    void getNonExistingTodo() throws Exception {
        // Get the todo
        restTodoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTodo() throws Exception {
        // Initialize the database
        insertedTodo = todoRepository.saveAndFlush(todo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the todo
        Todo updatedTodo = todoRepository.findById(todo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTodo are not directly saved in db
        em.detach(updatedTodo);
        updatedTodo.title(UPDATED_TITLE).status(UPDATED_STATUS);
        TodoDTO todoDTO = todoMapper.toDto(updatedTodo);

        restTodoMockMvc
            .perform(put(ENTITY_API_URL_ID, todoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(todoDTO)))
            .andExpect(status().isOk());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTodoToMatchAllProperties(updatedTodo);
    }

    @Test
    @Transactional
    void putNonExistingTodo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        todo.setId(longCount.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(put(ENTITY_API_URL_ID, todoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(todoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTodo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        todo.setId(longCount.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTodo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        todo.setId(longCount.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(todoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTodoWithPatch() throws Exception {
        // Initialize the database
        insertedTodo = todoRepository.saveAndFlush(todo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the todo using partial update
        Todo partialUpdatedTodo = new Todo();
        partialUpdatedTodo.setId(todo.getId());

        partialUpdatedTodo.title(UPDATED_TITLE);

        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTodo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTodo))
            )
            .andExpect(status().isOk());

        // Validate the Todo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTodoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTodo, todo), getPersistedTodo(todo));
    }

    @Test
    @Transactional
    void fullUpdateTodoWithPatch() throws Exception {
        // Initialize the database
        insertedTodo = todoRepository.saveAndFlush(todo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the todo using partial update
        Todo partialUpdatedTodo = new Todo();
        partialUpdatedTodo.setId(todo.getId());

        partialUpdatedTodo.title(UPDATED_TITLE).status(UPDATED_STATUS);

        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTodo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTodo))
            )
            .andExpect(status().isOk());

        // Validate the Todo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTodoUpdatableFieldsEquals(partialUpdatedTodo, getPersistedTodo(partialUpdatedTodo));
    }

    @Test
    @Transactional
    void patchNonExistingTodo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        todo.setId(longCount.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, todoDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTodo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        todo.setId(longCount.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(todoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTodo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        todo.setId(longCount.incrementAndGet());

        // Create the Todo
        TodoDTO todoDTO = todoMapper.toDto(todo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTodoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(todoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Todo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTodo() throws Exception {
        // Initialize the database
        insertedTodo = todoRepository.saveAndFlush(todo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the todo
        restTodoMockMvc
            .perform(delete(ENTITY_API_URL_ID, todo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return todoRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Todo getPersistedTodo(Todo todo) {
        return todoRepository.findById(todo.getId()).orElseThrow();
    }

    protected void assertPersistedTodoToMatchAllProperties(Todo expectedTodo) {
        assertTodoAllPropertiesEquals(expectedTodo, getPersistedTodo(expectedTodo));
    }

    protected void assertPersistedTodoToMatchUpdatableProperties(Todo expectedTodo) {
        assertTodoAllUpdatablePropertiesEquals(expectedTodo, getPersistedTodo(expectedTodo));
    }
}
