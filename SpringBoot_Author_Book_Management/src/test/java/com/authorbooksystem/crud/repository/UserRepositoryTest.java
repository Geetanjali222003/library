package com.authorbooksystem.crud.repository;

import com.authorbooksystem.crud.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
    }

    @Nested
    @DisplayName("findByUsername() method tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should return user when user exists")
        void findByUsername_ShouldReturnUser_WhenUserExists() {
            // Given
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("testuser");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
            assertThat(found.get().getPassword()).isEqualTo("encodedPassword");
            assertThat(found.get().getRole()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should return empty when user does not exist")
        void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
            // When
            Optional<User> found = userRepository.findByUsername("nonexistent");

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Should be case sensitive by default")
        void findByUsername_ShouldBeCaseSensitive() {
            // Given
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("TESTUSER");

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Should handle special characters in username")
        void findByUsername_ShouldHandleSpecialCharacters() {
            // Given
            testUser.setUsername("test@email.com");
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("test@email.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("test@email.com");
        }

        @Test
        @DisplayName("Should handle null username parameter")
        void findByUsername_ShouldHandleNullUsername() {
            // When
            Optional<User> found = userRepository.findByUsername(null);

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Should handle empty string username")
        void findByUsername_ShouldHandleEmptyUsername() {
            // When
            Optional<User> found = userRepository.findByUsername("");

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Should handle whitespace-only username")
        void findByUsername_ShouldHandleWhitespaceOnlyUsername() {
            // When
            Optional<User> found = userRepository.findByUsername("   ");

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Should handle very long username")
        void findByUsername_ShouldHandleLongUsername() {
            // Given
            String longUsername = "a".repeat(255); // Assuming max length is 255
            testUser.setUsername(longUsername);
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername(longUsername);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo(longUsername);
        }
    }

    @Nested
    @DisplayName("save() method tests")
    class SaveTests {

        @Test
        @DisplayName("Should persist user successfully")
        void save_ShouldPersistUser() {
            // When
            User saved = userRepository.save(testUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("testuser");
            assertThat(saved.getPassword()).isEqualTo("encodedPassword");
            assertThat(saved.getRole()).isEqualTo("USER");

            // Verify it's actually persisted
            User found = entityManager.find(User.class, saved.getId());
            assertThat(found).isNotNull();
            assertThat(found.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should update existing user")
        void save_ShouldUpdateExistingUser() {
            // Given
            User saved = entityManager.persistAndFlush(testUser);
            Long userId = saved.getId();

            // When
            saved.setPassword("newEncodedPassword");
            saved.setRole("ADMIN");
            User updated = userRepository.save(saved);

            // Then
            assertThat(updated.getId()).isEqualTo(userId);
            assertThat(updated.getPassword()).isEqualTo("newEncodedPassword");
            assertThat(updated.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Should handle null values appropriately")
        void save_ShouldHandleNullValues() {
            // Given
            testUser.setPassword(null);
            testUser.setRole(null);

            // When
            User saved = userRepository.save(testUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("testuser");
            assertThat(saved.getPassword()).isNull();
            assertThat(saved.getRole()).isNull();
        }
    }

    @Nested
    @DisplayName("Data integrity tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should save user with minimum required fields")
        void save_ShouldAllowMinimumRequiredFields() {
            // Given
            User minimalUser = new User();
            minimalUser.setUsername("minimal");

            // When
            User saved = userRepository.save(minimalUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUsername()).isEqualTo("minimal");
        }

        @Test
        @DisplayName("Should handle different role values")
        void save_ShouldHandleDifferentRoles() {
            // Test different role values
            String[] roles = {"USER", "ADMIN", "MODERATOR", "GUEST"};
            
            for (String role : roles) {
                User user = new User();
                user.setUsername("user_" + role.toLowerCase());
                user.setPassword("password");
                user.setRole(role);

                User saved = userRepository.save(user);
                
                assertThat(saved.getRole()).isEqualTo(role);
            }
        }

        @Test
        @DisplayName("Should handle multiple users with different usernames")
        void save_ShouldAllowMultipleUsers() {
            // Given
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword("password1");
            user1.setRole("USER");

            User user2 = new User();
            user2.setUsername("user2");
            user2.setPassword("password2");
            user2.setRole("ADMIN");

            // When
            User saved1 = userRepository.save(user1);
            User saved2 = userRepository.save(user2);

            // Then
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
            assertThat(userRepository.findByUsername("user1")).isPresent();
            assertThat(userRepository.findByUsername("user2")).isPresent();
        }
    }

    @Nested
    @DisplayName("Repository operations tests")
    class RepositoryOperationsTests {

        @Test
        @DisplayName("Should count users correctly")
        void count_ShouldReturnCorrectCount() {
            // Given
            assertThat(userRepository.count()).isZero();
            
            entityManager.persistAndFlush(testUser);
            
            User anotherUser = new User();
            anotherUser.setUsername("anotheruser");
            anotherUser.setPassword("anotherpassword");
            anotherUser.setRole("ADMIN");
            entityManager.persistAndFlush(anotherUser);

            // When & Then
            assertThat(userRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should delete user correctly")
        void delete_ShouldRemoveUser() {
            // Given
            User saved = entityManager.persistAndFlush(testUser);
            assertThat(userRepository.count()).isEqualTo(1);

            // When
            userRepository.delete(saved);

            // Then
            assertThat(userRepository.count()).isZero();
            assertThat(userRepository.findByUsername("testuser")).isNotPresent();
        }

        @Test
        @DisplayName("Should check if user exists by id")
        void existsById_ShouldReturnCorrectBoolean() {
            // Given
            User saved = entityManager.persistAndFlush(testUser);

            // When & Then
            assertThat(userRepository.existsById(saved.getId())).isTrue();
            assertThat(userRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Should find user by id")
        void findById_ShouldReturnCorrectUser() {
            // Given
            User saved = entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should delete user by id")
        void deleteById_ShouldRemoveUser() {
            // Given
            User saved = entityManager.persistAndFlush(testUser);
            Long userId = saved.getId();
            assertThat(userRepository.count()).isEqualTo(1);

            // When
            userRepository.deleteById(userId);

            // Then
            assertThat(userRepository.count()).isZero();
            assertThat(userRepository.existsById(userId)).isFalse();
        }

        @Test
        @DisplayName("Should find all users")
        void findAll_ShouldReturnAllUsers() {
            // Given
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword("password1");
            user1.setRole("USER");

            User user2 = new User();
            user2.setUsername("user2");
            user2.setPassword("password2");
            user2.setRole("ADMIN");

            entityManager.persistAndFlush(user1);
            entityManager.persistAndFlush(user2);

            // When
            var allUsers = userRepository.findAll();

            // Then
            assertThat(allUsers).hasSize(2);
            assertThat(allUsers).extracting(User::getUsername)
                    .containsExactlyInAnyOrder("user1", "user2");
        }
    }

    @Nested
    @DisplayName("Edge case and constraint tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle usernames with leading/trailing spaces")
        void findByUsername_ShouldHandleUsernameWithSpaces() {
            // Given
            testUser.setUsername("  testuser  ");
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("  testuser  ");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("  testuser  ");
        }

        @Test
        @DisplayName("Should handle unicode characters in username")
        void findByUsername_ShouldHandleUnicodeCharacters() {
            // Given
            testUser.setUsername("测试用户");
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("测试用户");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("测试用户");
        }

        @Test
        @DisplayName("Should handle numeric username")
        void findByUsername_ShouldHandleNumericUsername() {
            // Given
            testUser.setUsername("12345");
            entityManager.persistAndFlush(testUser);

            // When
            Optional<User> found = userRepository.findByUsername("12345");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("12345");
        }

        @Test
        @DisplayName("Should handle empty password and role")
        void save_ShouldHandleEmptyStrings() {
            // Given
            testUser.setPassword("");
            testUser.setRole("");

            // When
            User saved = userRepository.save(testUser);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getPassword()).isEqualTo("");
            assertThat(saved.getRole()).isEqualTo("");
        }

        @Test
        @DisplayName("Should handle very long password")
        void save_ShouldHandleLongPassword() {
            // Given
            String longPassword = "a".repeat(1000);
            testUser.setPassword(longPassword);

            // When
            User saved = userRepository.save(testUser);

            // Then
            assertThat(saved.getPassword()).isEqualTo(longPassword);
        }

        @Test
        @DisplayName("Should handle special characters in password")
        void save_ShouldHandleSpecialCharactersInPassword() {
            // Given
            testUser.setPassword("P@ssw0rd!@#$%^&*()_+");

            // When
            User saved = userRepository.save(testUser);

            // Then
            assertThat(saved.getPassword()).isEqualTo("P@ssw0rd!@#$%^&*()_+");
        }
    }

    @Nested
    @DisplayName("Performance and batch operations tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle bulk user creation efficiently")
        void save_ShouldHandleBulkOperations() {
            // Given
            int numberOfUsers = 50;
            
            // When
            for (int i = 0; i < numberOfUsers; i++) {
                User user = new User();
                user.setUsername("bulkuser" + i);
                user.setPassword("password" + i);
                user.setRole(i % 2 == 0 ? "USER" : "ADMIN");
                userRepository.save(user);
            }

            // Then
            assertThat(userRepository.count()).isEqualTo(numberOfUsers);
            
            // Verify some random users
            assertThat(userRepository.findByUsername("bulkuser10")).isPresent();
            assertThat(userRepository.findByUsername("bulkuser25")).isPresent();
            assertThat(userRepository.findByUsername("bulkuser49")).isPresent();
        }

        @Test
        @DisplayName("Should find users efficiently after multiple operations")
        void findByUsername_ShouldRemainEfficient() {
            // Given - Create multiple users
            for (int i = 0; i < 20; i++) {
                User user = new User();
                user.setUsername("perfuser" + i);
                user.setPassword("password" + i);
                user.setRole("USER");
                entityManager.persist(user);
            }
            entityManager.flush();

            // When & Then - Multiple lookups should all work correctly
            for (int i = 0; i < 20; i++) {
                Optional<User> found = userRepository.findByUsername("perfuser" + i);
                assertThat(found).isPresent();
                assertThat(found.get().getUsername()).isEqualTo("perfuser" + i);
            }

            // Verify non-existent users
            assertThat(userRepository.findByUsername("perfuser20")).isNotPresent();
            assertThat(userRepository.findByUsername("perfuser99")).isNotPresent();
        }
    }
}
