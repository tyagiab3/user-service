package com.visitly.mapper;

import com.visitly.dto.RoleResponse;
import com.visitly.dto.UserRoleResponse;
import com.visitly.model.Role;
import com.visitly.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between role-related entities and DTOs.
 *
 * Provides mappings for transforming Role and User entities
 * into lightweight response objects used by the API layer.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

	/**
     * Converts a Role entity into a RoleResponse DTO.
     *
     * @param role The role entity to convert
     * @return a corresponding RoleResponse containing role details
     */
    @Mapping(source = "roleName", target = "name")
    RoleResponse toRoleResponse(Role role);

    /**
     * Builds a UserRoleResponse combining user and role information.
     *
     * @param user The user entity
     * @param role The role entity assigned to the user
     * @return A UserRoleResponse containing mapped user-role data
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "role.roleName", target = "assignedRole")
    UserRoleResponse toUserRoleResponse(User user, Role role);
}
