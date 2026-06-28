package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.UserRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.enums.Carreras;
import com.dbp.democarpultec.repository.UserRepository;
import com.dbp.democarpultec.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserWhenValidData() {
        UserRequestDto dto = UserRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("999999999")
                .studentCode("U202410032")
                .career(Carreras.Ciencia_de_la_Computacion)
                .cycle(6)
                .build();

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Juan");
        savedUser.setLastName("Perez");
        savedUser.setEmail("juan@test.com");
        savedUser.setPhone("999999999");
        savedUser.setStudentCode("U202410032");
        savedUser.setCareer(Carreras.Ciencia_de_la_Computacion);
        savedUser.setCycle(6);
        savedUser.setRating(4.5);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto result = userService.create(dto);

        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertEquals("Perez", result.getLastName());
        assertEquals("juan@test.com", result.getEmail());
        assertEquals(4.5, result.getRating());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldReturnUserWhenIdExists() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setLastName("Perez");
        user.setEmail("juan@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Juan", result.getName());

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.findById(99L);
        });

        verify(userRepository).findById(99L);
    }

    @Test
    void shouldUpdateUserWhenValidData() {
        UserRequestDto dto = UserRequestDto.builder()
                .name("Pedro")
                .lastName("Lopez")
                .email("pedro@test.com")
                .phone("111111111")
                .studentCode("U202520034")
                .career(Carreras.Ciencia_de_Datos)
                .cycle(7)
                .build();

        User existing = new User();
        existing.setId(1L);

        User updated = new User();
        updated.setId(1L);
        updated.setName("Pedro");
        updated.setLastName("Lopez");
        updated.setEmail("pedro@test.com");
        updated.setRating(4.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(updated);

        UserResponseDto result = userService.update(1L, dto);

        assertNotNull(result);
        assertEquals("Pedro", result.getName());
        assertEquals("Lopez", result.getLastName());

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldDeleteUserWhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }
}
