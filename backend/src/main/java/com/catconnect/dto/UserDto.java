package com.catconnect.dto;

import com.catconnect.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String city;
    private Boolean isAdmin;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .city(user.getCity())
                .isAdmin(user.getIsAdmin())
                .build();
    }
}
