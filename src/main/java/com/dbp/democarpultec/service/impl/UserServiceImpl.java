package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.UserRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.repository.UserRepository;
import com.dbp.democarpultec.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public UserResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    public UserResponseDto create(UserRequestDto dto) {
        User user = new User();
        updateEntity(user, dto);
        return toResponseDto(userRepository.save(user));
    }

    public UserResponseDto update(Long id, UserRequestDto dto) {
        User user = findEntityById(id);
        updateEntity(user, dto);
        return toResponseDto(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    public User findEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email " + email));
    }

    private void updateEntity(User user, UserRequestDto dto) {
        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStudentCode(dto.getStudentCode());
        user.setCareer(dto.getCareer());
        user.setCycle(dto.getCycle());
    }

    public void updateRating(Long userId, Double rating) {
        User user = findEntityById(userId);
        user.setRating(rating);
        userRepository.save(user);
    }

    private UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .studentCode(user.getStudentCode())
                .career(user.getCareer())
                .cycle(user.getCycle())
                .rating(user.getRating())
                .role(user.getRole())
                .build();
    }
}
