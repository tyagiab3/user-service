package com.visitly.mapper;

import com.visitly.dto.UserRoleResponse;
import com.visitly.model.Role;
import com.visitly.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting user and role entities into a combined user-role response.
 *
 * Used to produce DTOs that represent which roles are assigned to specific users.
 * 
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    UserRoleMapper INSTANCE = Mappers.getMapper(UserRoleMapper.class);

    /**
     * Builds a UserRoleResponse using information from both User and Role entities.
     *
     * @param user The user entity containing basic user details
     * @param role The role entity assigned to the user
     * @return A response object containing combined user-role data
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "role.roleName", target = "assignedRole")
    UserRoleResponse toUserRoleResponse(User user, Role role);
}
