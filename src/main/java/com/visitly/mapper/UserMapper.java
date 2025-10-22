package com.visitly.mapper;

import com.visitly.dto.RegisterUserRequest;
import com.visitly.dto.UserProfileResponse;
import com.visitly.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between user entities and DTOs.
 *
 * Handles transformations between User, RegisterUserRequest,
 * and UserProfileResponse objects within the application.
 * 
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Converts a RegisterUserRequest DTO into a User entity.
     * Ignores fields that are managed automatically such as ID, roles, and lastLogin.
     *
     * @param dto The registration request containing user input data
     * @return A User entity ready for persistence
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    User toEntity(RegisterUserRequest dto);
    
    /**
     * Converts a User entity into a UserProfileResponse DTO.
     *
     * @param user The user entity to convert
     * @return A response object containing user profile details
     */
    UserProfileResponse toUserProfileResponse(User user);
}
