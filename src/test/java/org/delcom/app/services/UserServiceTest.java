package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User testUser;
    private final String NAME = "Jane Doe";
    private final String EMAIL = "jane.doe@example.com";
    private final String PASSWORD = "oldpassword";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User(NAME, EMAIL, PASSWORD); 
        testUser.setId(userId);
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser(NAME, EMAIL, PASSWORD);

        assertNotNull(createdUser);
        assertEquals(NAME, createdUser.getName());
        assertEquals(EMAIL, createdUser.getEmail());
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserByEmail_Found() {
        when(userRepository.findFirstByEmail(EMAIL)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserByEmail(EMAIL);

        assertNotNull(foundUser);
        assertEquals(EMAIL, foundUser.getEmail());
    }
    
    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findFirstByEmail(EMAIL)).thenReturn(Optional.empty());

        User foundUser = userService.getUserByEmail(EMAIL);

        assertNull(foundUser);
    }

    @Test
    void testGetUserById_Found() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    void testUpdateUser_Success() {
        String newName = "Janet Doe";
        String newEmail = "janet.doe@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]); 

        User updatedUser = userService.updateUser(userId, newName, newEmail);

        assertNotNull(updatedUser);
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User updatedUser = userService.updateUser(userId, "New Name", "new.email@example.com");

        assertNull(updatedUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdatePassword_Success() {
        String newPassword = "supernewpassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        User updatedUser = userService.updatePassword(userId, newPassword);

        assertNotNull(updatedUser);
        assertEquals(newPassword, updatedUser.getPassword());
    }
    
    @Test
    void testUpdatePassword_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User updatedUser = userService.updatePassword(userId, "anypassword");

        assertNull(updatedUser);
        verify(userRepository, never()).save(any(User.class));
    }
}