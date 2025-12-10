package org.delcom.app.services;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;
    
    // Data dummy
    private final UUID mockUserId = UUID.randomUUID();
    private final String mockName = "Test User";
    private final String mockEmail = "test@example.com";
    private final String mockPassword = "oldpassword";
    private final User mockUser = new User(mockName, mockEmail, mockPassword);

    // Memberi ID pada mockUser agar terlihat seperti objek dari DB
    {
        mockUser.setId(mockUserId);
    }
    
    // --- 1. Test createUser ---
    @Test
    void createUser_shouldCreateUserObjectAndCallRepositorySave() {
        String newName = "New User";
        String newEmail = "new@example.com";
        String newPassword = "newpassword";
        
        // Setup: Gunakan ArgumentCaptor untuk menangkap objek User yang dibuat
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        
        // Ketika repository.save dipanggil, kembalikan mockUser (simulasi setelah disimpan)
        when(userRepository.save(userCaptor.capture())).thenReturn(mockUser);

        // Eksekusi
        User result = userService.createUser(newName, newEmail, newPassword);

        // Verifikasi pemanggilan save
        verify(userRepository, times(1)).save(any(User.class));
        
        // Verifikasi objek User yang dibuat service sebelum disimpan
        User capturedUser = userCaptor.getValue();
        assertEquals(newName, capturedUser.getName());
        assertEquals(newEmail, capturedUser.getEmail());
        assertEquals(newPassword, capturedUser.getPassword());
        
        // Verifikasi hasil
        assertEquals(mockUser, result);
    }

    // --- 2. Test getUserByEmail ---
    @Test
    void getUserByEmail_found_shouldReturnUser() {
        // Setup: Ketika repository dipanggil, kembalikan Optional.of(mockUser)
        when(userRepository.findFirstByEmail(mockEmail)).thenReturn(Optional.of(mockUser));

        // Eksekusi
        User result = userService.getUserByEmail(mockEmail);

        // Verifikasi
        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findFirstByEmail(mockEmail);
    }

    @Test
    void getUserByEmail_notFound_shouldReturnNull() {
        // Setup: Ketika repository dipanggil, kembalikan Optional.empty()
        when(userRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());

        // Eksekusi
        User result = userService.getUserByEmail("nonexistent@example.com");

        // Verifikasi
        assertNull(result); // Memastikan orElse(null) bekerja
    }
    
    // --- 3. Test getUserById ---
    @Test
    void getUserById_found_shouldReturnUser() {
        // Setup: Ketika repository dipanggil, kembalikan Optional.of(mockUser)
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Eksekusi
        User result = userService.getUserById(mockUserId);

        // Verifikasi
        assertNotNull(result);
        assertEquals(mockUser, result);
        verify(userRepository, times(1)).findById(mockUserId);
    }

    @Test
    void getUserById_notFound_shouldReturnNull() {
        // Setup: Ketika repository dipanggil, kembalikan Optional.empty()
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Eksekusi
        User result = userService.getUserById(UUID.randomUUID());

        // Verifikasi
        assertNull(result); // Memastikan orElse(null) bekerja
    }

    // --- 4. Test updateUser ---
    @Test
    void updateUser_userFound_shouldUpdateFieldsAndSave() {
        String updatedName = "Updated Name";
        String updatedEmail = "updated@example.com";
        
        // Setup: Kembalikan objek User yang akan dimodifikasi service
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        // Setup: save mengembalikan objek yang sudah diupdate
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Eksekusi
        User result = userService.updateUser(mockUserId, updatedName, updatedEmail);

        // Verifikasi hasil
        assertNotNull(result);
        assertEquals(updatedName, result.getName());
        assertEquals(updatedEmail, result.getEmail());
        
        // Verifikasi save dipanggil
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateUser_userNotFound_shouldReturnNull() {
        // Setup: User tidak ditemukan
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Eksekusi
        User result = userService.updateUser(UUID.randomUUID(), "Name", "email@test.com");

        // Verifikasi
        assertNull(result);
        // Verifikasi bahwa save tidak dipanggil (cabang if (user == null))
        verify(userRepository, never()).save(any(User.class));
    }
    
    // --- 5. Test updatePassword ---
    @Test
    void updatePassword_userFound_shouldUpdatePasswordAndSave() {
        String newPassword = "securenewpassword";
        
        // Setup: Kembalikan objek User yang akan dimodifikasi service
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        // Setup: save mengembalikan objek yang sudah diupdate
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Eksekusi
        User result = userService.updatePassword(mockUserId, newPassword);

        // Verifikasi hasil
        assertNotNull(result);
        assertEquals(newPassword, result.getPassword());
        
        // Verifikasi save dipanggil
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updatePassword_userNotFound_shouldReturnNull() {
        String newPassword = "securenewpassword";
        // Setup: User tidak ditemukan
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Eksekusi
        User result = userService.updatePassword(UUID.randomUUID(), newPassword);

        // Verifikasi
        assertNull(result);
        // Verifikasi bahwa save tidak dipanggil (cabang if (user == null))
        verify(userRepository, never()).save(any(User.class));
    }
}