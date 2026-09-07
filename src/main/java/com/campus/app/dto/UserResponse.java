package com.campus.app.dto;

import com.campus.app.entity.User;
import com.campus.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String registerNo;
    private Role role;
    private String department;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .registerNo(u.getRegisterNo())
                .role(u.getRole())
                .department(u.getDepartment())
                .build();
    }
}
