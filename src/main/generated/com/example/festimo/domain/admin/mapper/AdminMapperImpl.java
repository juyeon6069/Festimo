package com.example.festimo.domain.admin.mapper;

import com.example.festimo.domain.admin.dto.AdminDTO;
import com.example.festimo.domain.user.domain.User;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-23T16:13:06+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Homebrew)"
)
public class AdminMapperImpl implements AdminMapper {

    @Override
    public AdminDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        Long userId = null;
        String userName = null;
        String nickname = null;
        String email = null;
        String role = null;
        String gender = null;
        Float ratingAvg = null;
        LocalDateTime createdDate = null;

        userId = user.getId();
        userName = user.getUserName();
        nickname = user.getNickname();
        email = user.getEmail();
        if ( user.getRole() != null ) {
            role = user.getRole().name();
        }
        if ( user.getGender() != null ) {
            gender = user.getGender().name();
        }
        ratingAvg = user.getRatingAvg();
        createdDate = user.getCreatedDate();

        AdminDTO adminDTO = new AdminDTO( userId, userName, nickname, email, role, createdDate, gender, ratingAvg );

        return adminDTO;
    }
}
