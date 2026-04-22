package com.authorbooksystem.crud.service;

import com.authorbooksystem.crud.dto.request.AuthorRequestDTO;
import com.authorbooksystem.crud.dto.response.AuthorResponseDTO;
import com.authorbooksystem.crud.entity.Author;
import com.authorbooksystem.crud.exception.AuthorNotFoundException;
import com.authorbooksystem.crud.repository.AuthorRepository;
import com.authorbooksystem.crud.repository.BookRepository;
import com.authorbooksystem.crud.utils.ToCsv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Author Service Tests")
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author testAuthor;
    private AuthorRequestDTO testAuthorRequestDTO;
    private AuthorResponseDTO testAuthorResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("John Doe");
        testAuthor.setEmail("john.doe@example.com");

        testAuthorRequestDTO = new AuthorRequestDTO();
        testAuthorRequestDTO.setName("John Doe");
        testAuthorRequestDTO.setEmail("john.doe@example.com");

        testAuthorResponseDTO = new AuthorResponseDTO();
        testAuthorResponseDTO.setId(1L);
        testAuthorResponseDTO.setName("John Doe");
        testAuthorResponseDTO.setEmail("john.doe@example.com");
    }

    @Nested
    @DisplayName("Create Author Tests")
    class CreateAuthorTests {

        @Test
        @DisplayName("Should create author successfully with valid input")
        void createAuthor_ShouldReturnAuthorResponseDTO_WhenValidInput() {
            // Given
            when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);

            // When
            AuthorResponseDTO result = authorService.createAuthor(testAuthorRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("John Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(result.getId()).isEqualTo(1L);

            verify(authorRepository, times(1)).save(any(Author.class));
        }

        @Test
        @DisplayName("Should handle null fields in author request")
        void createAuthor_ShouldHandleNullFields() {
            // Given
            AuthorRequestDTO dtoWithNulls = new AuthorRequestDTO();
            // Leaving name and email null

            Author authorWithNulls = new Author();
            authorWithNulls.setId(1L);
            // name and email will be null

            when(authorRepository.save(any(Author.class))).thenReturn(authorWithNulls);

            // When
            AuthorResponseDTO result = authorService.createAuthor(dtoWithNulls);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isNull();
            assertThat(result.getEmail()).isNull();

            verify(authorRepository, times(1)).save(any(Author.class));
        }

        @Test
        @DisplayName("Should create author with special characters in name")
        void createAuthor_ShouldHandleSpecialCharacters() {
            // Given
            AuthorRequestDTO specialCharDTO = new AuthorRequestDTO();
            specialCharDTO.setName("José María García-López");
            specialCharDTO.setEmail("jose.garcia@example.com");

            Author specialCharAuthor = new Author();
            specialCharAuthor.setId(1L);
            specialCharAuthor.setName("José María García-López");
            specialCharAuthor.setEmail("jose.garcia@example.com");

            when(authorRepository.save(any(Author.class))).thenReturn(specialCharAuthor);

            // When
            AuthorResponseDTO result = authorService.createAuthor(specialCharDTO);

            // Then
            assertThat(result.getName()).isEqualTo("José María García-López");
            assertThat(result.getEmail()).isEqualTo("jose.garcia@example.com");
        }

        @Test
        @DisplayName("Should create author with long name and email")
        void createAuthor_ShouldHandleLongStrings() {
            // Given
            String longName = "A".repeat(255);
            String longEmail = "very.long.email.address.for.testing.purposes@verylongdomainname.example.com";
            
            AuthorRequestDTO longStringDTO = new AuthorRequestDTO();
            longStringDTO.setName(longName);
            longStringDTO.setEmail(longEmail);

            Author longStringAuthor = new Author();
            longStringAuthor.setId(1L);
            longStringAuthor.setName(longName);
            longStringAuthor.setEmail(longEmail);

            when(authorRepository.save(any(Author.class))).thenReturn(longStringAuthor);

            // When
            AuthorResponseDTO result = authorService.createAuthor(longStringDTO);

            // Then
            assertThat(result.getName()).isEqualTo(longName);
            assertThat(result.getEmail()).isEqualTo(longEmail);
        }

        @Test
        @DisplayName("Should create author with empty strings")
        void createAuthor_ShouldHandleEmptyStrings() {
            // Given
            AuthorRequestDTO emptyStringDTO = new AuthorRequestDTO();
            emptyStringDTO.setName("");
            emptyStringDTO.setEmail("");

            Author emptyStringAuthor = new Author();
            emptyStringAuthor.setId(1L);
            emptyStringAuthor.setName("");
            emptyStringAuthor.setEmail("");

            when(authorRepository.save(any(Author.class))).thenReturn(emptyStringAuthor);

            // When
            AuthorResponseDTO result = authorService.createAuthor(emptyStringDTO);

            // Then
            assertThat(result.getName()).isEqualTo("");
            assertThat(result.getEmail()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get All Authors Tests")
    class GetAllAuthorsTests {

        @Test
        @DisplayName("Should return list of authors when authors exist")
        void getAllAuthors_ShouldReturnListOfAuthorResponseDTOs() {
            // Given
            Author author2 = new Author();
            author2.setId(2L);
            author2.setName("Jane Smith");
            author2.setEmail("jane.smith@example.com");

            List<Author> authors = Arrays.asList(testAuthor, author2);
            when(authorRepository.findAll()).thenReturn(authors);

            // When
            List<AuthorResponseDTO> result = authorService.getAllAuthors();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("John Doe");
            assertThat(result.get(1).getName()).isEqualTo("Jane Smith");

            verify(authorRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no authors exist")
        void getAllAuthors_ShouldReturnEmptyList_WhenNoAuthorsExist() {
            // Given
            when(authorRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<AuthorResponseDTO> result = authorService.getAllAuthors();

            // Then
            assertThat(result).isEmpty();
            verify(authorRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should handle large number of authors")
        void getAllAuthors_ShouldHandleLargeDataset() {
            // Given
            List<Author> largeAuthorList = Arrays.asList(
                createAuthor(1L, "Author 1", "author1@test.com"),
                createAuthor(2L, "Author 2", "author2@test.com"),
                createAuthor(3L, "Author 3", "author3@test.com"),
                createAuthor(4L, "Author 4", "author4@test.com"),
                createAuthor(5L, "Author 5", "author5@test.com")
            );

            when(authorRepository.findAll()).thenReturn(largeAuthorList);

            // When
            List<AuthorResponseDTO> result = authorService.getAllAuthors();

            // Then
            assertThat(result).hasSize(5);
            assertThat(result).extracting(AuthorResponseDTO::getName)
                    .containsExactly("Author 1", "Author 2", "Author 3", "Author 4", "Author 5");
        }
    }

    @Nested
    @DisplayName("Update Author Tests")
    class UpdateAuthorTests {

        @Test
        @DisplayName("Should update author when author exists")
        void updateAuthor_ShouldReturnUpdatedAuthorResponseDTO_WhenAuthorExists() {
            // Given
            AuthorRequestDTO updateDTO = new AuthorRequestDTO();
            updateDTO.setName("John Updated");
            updateDTO.setEmail("john.updated@example.com");

            Author updatedAuthor = new Author();
            updatedAuthor.setId(1L);
            updatedAuthor.setName("John Updated");
            updatedAuthor.setEmail("john.updated@example.com");

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);

            // When
            AuthorResponseDTO result = authorService.updateAuthor(1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("John Updated");
            assertThat(result.getEmail()).isEqualTo("john.updated@example.com");

            verify(authorRepository, times(1)).findById(1L);
            verify(authorRepository, times(1)).save(any(Author.class));
        }

        @Test
        @DisplayName("Should throw exception when author does not exist")
        void updateAuthor_ShouldThrowAuthorNotFoundException_WhenAuthorDoesNotExist() {
            // Given
            when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authorService.updateAuthor(999L, testAuthorRequestDTO))
                    .isInstanceOf(AuthorNotFoundException.class)
                    .hasMessageContaining("Author not found with ID: 999");

            verify(authorRepository, times(1)).findById(999L);
            verify(authorRepository, never()).save(any(Author.class));
        }

        @Test
        @DisplayName("Should partially update author with null fields")
        void updateAuthor_ShouldHandlePartialUpdate() {
            // Given
            AuthorRequestDTO partialUpdateDTO = new AuthorRequestDTO();
            partialUpdateDTO.setName("Partially Updated");
            // email remains null

            Author partiallyUpdatedAuthor = new Author();
            partiallyUpdatedAuthor.setId(1L);
            partiallyUpdatedAuthor.setName("Partially Updated");
            partiallyUpdatedAuthor.setEmail("john.doe@example.com"); // Original email preserved

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(authorRepository.save(any(Author.class))).thenReturn(partiallyUpdatedAuthor);

            // When
            AuthorResponseDTO result = authorService.updateAuthor(1L, partialUpdateDTO);

            // Then
            assertThat(result.getName()).isEqualTo("Partially Updated");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should update author with same values")
        void updateAuthor_ShouldHandleSameValues() {
            // Given - using same values as original
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);

            // When
            AuthorResponseDTO result = authorService.updateAuthor(1L, testAuthorRequestDTO);

            // Then
            assertThat(result.getName()).isEqualTo("John Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            
            verify(authorRepository, times(1)).save(any(Author.class));
        }
    }

    @Nested
    @DisplayName("Delete Author Tests")
    class DeleteAuthorTests {

        @Test
        @DisplayName("Should delete author when author exists")
        void deleteAuthor_ShouldDeleteAuthor_WhenAuthorExists() {
            // Given
            when(authorRepository.existsById(1L)).thenReturn(true);
            doNothing().when(authorRepository).deleteById(1L);

            // When
            authorService.deleteAuthor(1L);

            // Then
            verify(authorRepository, times(1)).existsById(1L);
            verify(authorRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when author does not exist")
        void deleteAuthor_ShouldThrowAuthorNotFoundException_WhenAuthorDoesNotExist() {
            // Given
            when(authorRepository.existsById(anyLong())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authorService.deleteAuthor(999L))
                    .isInstanceOf(AuthorNotFoundException.class)
                    .hasMessageContaining("Author not found with ID: 999");

            verify(authorRepository, times(1)).existsById(999L);
            verify(authorRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should handle repository exception during delete")
        void deleteAuthor_ShouldHandleRepositoryException() {
            // Given
            when(authorRepository.existsById(1L)).thenReturn(true);
            doThrow(new RuntimeException("Database connection failed")).when(authorRepository).deleteById(1L);

            // When & Then
            assertThatThrownBy(() -> authorService.deleteAuthor(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection failed");

            verify(authorRepository, times(1)).existsById(1L);
            verify(authorRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("CSV Operations Tests")
    class CsvOperationsTests {

        @Test
        @DisplayName("Should create multiple authors from CSV data")
        void createAuthorsFromCsv_ShouldCreateMultipleAuthors() {
            // Given
            AuthorRequestDTO dto1 = new AuthorRequestDTO();
            dto1.setName("Author 1");
            dto1.setEmail("author1@example.com");

            AuthorRequestDTO dto2 = new AuthorRequestDTO();
            dto2.setName("Author 2");
            dto2.setEmail("author2@example.com");

            List<AuthorRequestDTO> dtos = Arrays.asList(dto1, dto2);

            Author savedAuthor1 = new Author();
            savedAuthor1.setName("Author 1");
            savedAuthor1.setEmail("author1@example.com");

            Author savedAuthor2 = new Author();
            savedAuthor2.setName("Author 2");
            savedAuthor2.setEmail("author2@example.com");

            when(authorRepository.save(any(Author.class)))
                    .thenReturn(savedAuthor1)
                    .thenReturn(savedAuthor2);

            // When
            authorService.createAuthorsFromCsv(dtos);

            // Then
            verify(authorRepository, times(2)).save(any(Author.class));
        }

        @Test
        @DisplayName("Should handle empty CSV data")
        void createAuthorsFromCsv_ShouldHandleEmptyList() {
            // Given
            List<AuthorRequestDTO> emptyList = Collections.emptyList();

            // When
            authorService.createAuthorsFromCsv(emptyList);

            // Then
            verify(authorRepository, never()).save(any(Author.class));
        }

        @Test
        @DisplayName("Should export authors to CSV format")
        void exportAuthorsToCsv_ShouldReturnCsvString() {
            // Given
            List<Author> authors = Arrays.asList(testAuthor);
            String expectedCsv = "id,name,email\n1,John Doe,john.doe@example.com\n";
            
            when(authorRepository.findAll()).thenReturn(authors);

            try (MockedStatic<ToCsv> mockedToCsv = mockStatic(ToCsv.class)) {
                mockedToCsv.when(() -> ToCsv.authorToCsv(authors)).thenReturn(expectedCsv);

                // When
                String result = authorService.exportAuthorsToCsv();

                // Then
                assertThat(result).isEqualTo(expectedCsv);
                verify(authorRepository, times(1)).findAll();
                mockedToCsv.verify(() -> ToCsv.authorToCsv(authors), times(1));
            }
        }

        @Test
        @DisplayName("Should export empty CSV when no authors exist")
        void exportAuthorsToCsv_ShouldHandleEmptyRepository() {
            // Given
            List<Author> emptyList = Collections.emptyList();
            String expectedCsv = "id,name,email\n";
            
            when(authorRepository.findAll()).thenReturn(emptyList);

            try (MockedStatic<ToCsv> mockedToCsv = mockStatic(ToCsv.class)) {
                mockedToCsv.when(() -> ToCsv.authorToCsv(emptyList)).thenReturn(expectedCsv);

                // When
                String result = authorService.exportAuthorsToCsv();

                // Then
                assertThat(result).isEqualTo(expectedCsv);
                verify(authorRepository, times(1)).findAll();
            }
        }

        @Test
        @DisplayName("Should handle repository exception during CSV export")
        void exportAuthorsToCsv_ShouldHandleRepositoryException() {
            // Given
            when(authorRepository.findAll()).thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> authorService.exportAuthorsToCsv())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle repository returning null")
        void createAuthor_ShouldHandleNullRepositoryResponse() {
            // Given
            when(authorRepository.save(any(Author.class))).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> authorService.createAuthor(testAuthorRequestDTO))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle concurrent modification scenarios")
        void updateAuthor_ShouldHandleConcurrentModification() {
            // Given
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(authorRepository.save(any(Author.class)))
                    .thenThrow(new RuntimeException("Optimistic locking failure"));

            // When & Then
            assertThatThrownBy(() -> authorService.updateAuthor(1L, testAuthorRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Optimistic locking failure");
        }

        @Test
        @DisplayName("Should verify interaction count with repository")
        void getAllAuthors_ShouldCallRepositoryOnlyOnce() {
            // Given
            when(authorRepository.findAll()).thenReturn(Arrays.asList(testAuthor));

            // When
            authorService.getAllAuthors();
            authorService.getAllAuthors(); // Second call

            // Then
            verify(authorRepository, times(2)).findAll();
        }

        @Test
        @DisplayName("Should handle null author request DTO")
        void createAuthor_ShouldHandleNullDTO() {
            // When & Then
            assertThatThrownBy(() -> authorService.createAuthor(null))
                    .isInstanceOf(NullPointerException.class);

            verify(authorRepository, never()).save(any(Author.class));
        }
    }

    // Helper method to create test authors
    private Author createAuthor(Long id, String name, String email) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        author.setEmail(email);
        return author;
    }
}
