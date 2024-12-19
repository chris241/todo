package com.baw.todo2.service.mapper;

import com.baw.todo2.domain.Todo;
import com.baw.todo2.domain.User;
import com.baw.todo2.service.dto.TodoDTO;
import com.baw.todo2.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Todo} and its DTO {@link TodoDTO}.
 */
@Mapper(componentModel = "spring")
public interface TodoMapper extends EntityMapper<TodoDTO, Todo> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    TodoDTO toDto(Todo s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
