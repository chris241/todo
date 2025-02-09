package com.baw.todo2.web.rest;

import com.baw.todo2.repository.TodoRepository;
import com.baw.todo2.service.TodoService;
import com.baw.todo2.service.dto.TodoDTO;
import com.baw.todo2.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.baw.todo2.domain.Todo}.
 */
@RestController
@RequestMapping("/api/todos")
public class TodoResource {

    private static final Logger LOG = LoggerFactory.getLogger(TodoResource.class);

    private static final String ENTITY_NAME = "todo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TodoService todoService;

    private final TodoRepository todoRepository;

    public TodoResource(TodoService todoService, TodoRepository todoRepository) {
        this.todoService = todoService;
        this.todoRepository = todoRepository;
    }

    /**
     * {@code POST  /todos} : Create a new todo.
     *
     * @param todoDTO the todoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new todoDTO, or with status {@code 400 (Bad Request)} if the todo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TodoDTO> createTodo(@RequestBody TodoDTO todoDTO) throws URISyntaxException {
      LOG.debug("REST request to save Todo : {}", todoDTO);
        if (todoDTO.getId() != null) {
            throw new BadRequestAlertException("A new todo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        todoDTO = todoService.save(todoDTO);
        return ResponseEntity.created(new URI("/api/todos/" + todoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, todoDTO.getId().toString()))
            .body(todoDTO);
    }

    /**
     * {@code PUT  /todos/:id} : Updates an existing todo.
     *
     * @param id the id of the todoDTO to save.
     * @param todoDTO the todoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated todoDTO,
     * or with status {@code 400 (Bad Request)} if the todoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the todoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> updateTodo(@PathVariable(value = "id", required = false) final Long id, @RequestBody TodoDTO todoDTO)
        throws URISyntaxException {
        LOG.debug("REST request to update Todo : {}, {}", id, todoDTO);
        if (todoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, todoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!todoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        todoDTO = todoService.update(todoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, todoDTO.getId().toString()))
            .body(todoDTO);
    }

    /**
     * {@code PATCH  /todos/:id} : Partial updates given fields of an existing todo, field will ignore if it is null
     *
     * @param id the id of the todoDTO to save.
     * @param todoDTO the todoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated todoDTO,
     * or with status {@code 400 (Bad Request)} if the todoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the todoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the todoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TodoDTO> partialUpdateTodo(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody TodoDTO todoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Todo partially : {}, {}", id, todoDTO);
        if (todoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, todoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!todoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TodoDTO> result = todoService.partialUpdate(todoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, todoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /todos} : get all the todos.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of todos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TodoDTO>> getAllTodos(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Todos");
        Page<TodoDTO> page;
        if (eagerload) {
            page = todoService.findAllWithEagerRelationships(pageable);
        } else {
            page = todoService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /todos/:id} : get the "id" todo.
     *
     * @param id the id of the todoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the todoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> getTodo(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Todo : {}", id);
        Optional<TodoDTO> todoDTO = todoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(todoDTO);
    }

    /**
     * {@code DELETE  /todos/:id} : delete the "id" todo.
     *
     * @param id the id of the todoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Todo : {}", id);
       todoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
