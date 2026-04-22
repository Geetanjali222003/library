package com.authorbooksystem.crud.service;

import com.authorbooksystem.crud.dto.request.BookRequestDTO;
import com.authorbooksystem.crud.dto.response.BookResponseDTO;
import com.authorbooksystem.crud.entity.Author;
import com.authorbooksystem.crud.entity.Book;
import com.authorbooksystem.crud.exception.AuthorNotFoundException;
import com.authorbooksystem.crud.exception.BookNotFoundException;
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
@DisplayName("Book Service Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private Author testAuthor;
    private Book testBook;
    private BookRequestDTO testBookRequestDTO;
    private BookResponseDTO testBookResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup test author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("John Doe");
        testAuthor.setEmail("john.doe@example.com");

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setGenre("Fiction");
        testBook.setAuthor(testAuthor);

        // Setup request DTO
        testBookRequestDTO = new BookRequestDTO();
        testBookRequestDTO.setTitle("Test Book");
        testBookRequestDTO.setGenre("Fiction");
        testBookRequestDTO.setAuthorId(1L);

        // Setup response DTO
        testBookResponseDTO = new BookResponseDTO();
        testBookResponseDTO.setId(1L);
        testBookResponseDTO.setTitle("Test Book");
        testBookResponseDTO.setGenre("Fiction");
        testBookResponseDTO.setAuthorName("John Doe");
    }

    @Nested
    @DisplayName("Create Book Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Should create book successfully with valid input")
        void createBook_ShouldReturnBookResponseDTO_WhenValidInput() {
            // Given
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);

            // When
            BookResponseDTO result = bookService.createBook(testBookRequestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Book");
            assertThat(result.getGenre()).isEqualTo("Fiction");
            assertThat(result.getAuthorName()).isEqualTo("John Doe");
            assertThat(result.getId()).isEqualTo(1L);

            verify(authorRepository, times(1)).findById(1L);
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw AuthorNotFoundException when author does not exist")
        void createBook_ShouldThrowAuthorNotFoundException_WhenAuthorDoesNotExist() {
            // Given
            when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(testBookRequestDTO))
                    .isInstanceOf(AuthorNotFoundException.class)
                    .hasMessageContaining("Author not found with ID: 1");

            verify(authorRepository, times(1)).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should handle null fields in book request")
        void createBook_ShouldHandleNullFields() {
            // Given
            BookRequestDTO dtoWithNulls = new BookRequestDTO();
            dtoWithNulls.setAuthorId(1L);
            // Leaving title and genre null

            Book bookWithNulls = new Book();
            bookWithNulls.setId(1L);
            bookWithNulls.setAuthor(testAuthor);
            // title and genre will be null

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(bookWithNulls);

            // When
            BookResponseDTO result = bookService.createBook(dtoWithNulls);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isNull();
            assertThat(result.getGenre()).isNull();
            assertThat(result.getAuthorName()).isEqualTo("John Doe");

            verify(authorRepository, times(1)).findById(1L);
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should create book with special characters in title")
        void createBook_ShouldHandleSpecialCharacters() {
            // Given
            BookRequestDTO specialCharDTO = new BookRequestDTO();
            specialCharDTO.setTitle("El Quijote: La Historia de Don Quijote de la Mancha");
            specialCharDTO.setGenre("Clásico Español");
            specialCharDTO.setAuthorId(1L);

            Book specialCharBook = new Book();
            specialCharBook.setId(1L);
            specialCharBook.setTitle("El Quijote: La Historia de Don Quijote de la Mancha");
            specialCharBook.setGenre("Clásico Español");
            specialCharBook.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(specialCharBook);

            // When
            BookResponseDTO result = bookService.createBook(specialCharDTO);

            // Then
            assertThat(result.getTitle()).isEqualTo("El Quijote: La Historia de Don Quijote de la Mancha");
            assertThat(result.getGenre()).isEqualTo("Clásico Español");
        }

        @Test
        @DisplayName("Should create book with long title and genre")
        void createBook_ShouldHandleLongStrings() {
            // Given
            String longTitle = "A".repeat(500);
            String longGenre = "B".repeat(200);
            
            BookRequestDTO longStringDTO = new BookRequestDTO();
            longStringDTO.setTitle(longTitle);
            longStringDTO.setGenre(longGenre);
            longStringDTO.setAuthorId(1L);

            Book longStringBook = new Book();
            longStringBook.setId(1L);
            longStringBook.setTitle(longTitle);
            longStringBook.setGenre(longGenre);
            longStringBook.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(longStringBook);

            // When
            BookResponseDTO result = bookService.createBook(longStringDTO);

            // Then
            assertThat(result.getTitle()).isEqualTo(longTitle);
            assertThat(result.getGenre()).isEqualTo(longGenre);
        }

        @Test
        @DisplayName("Should create book with empty strings")
        void createBook_ShouldHandleEmptyStrings() {
            // Given
            BookRequestDTO emptyStringDTO = new BookRequestDTO();
            emptyStringDTO.setTitle("");
            emptyStringDTO.setGenre("");
            emptyStringDTO.setAuthorId(1L);

            Book emptyStringBook = new Book();
            emptyStringBook.setId(1L);
            emptyStringBook.setTitle("");
            emptyStringBook.setGenre("");
            emptyStringBook.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(emptyStringBook);

            // When
            BookResponseDTO result = bookService.createBook(emptyStringDTO);

            // Then
            assertThat(result.getTitle()).isEqualTo("");
            assertThat(result.getGenre()).isEqualTo("");
        }

        @Test
        @DisplayName("Should handle null book request DTO")
        void createBook_ShouldHandleNullDTO() {
            // When & Then
            assertThatThrownBy(() -> bookService.createBook(null))
                    .isInstanceOf(NullPointerException.class);

            verify(authorRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("Fetch Books Tests")
    class FetchBooksTests {

        @Test
        @DisplayName("Should return list of books when books exist")
        void fetchBooks_ShouldReturnListOfBookResponseDTOs() {
            // Given
            Author author2 = new Author();
            author2.setId(2L);
            author2.setName("Jane Smith");
            author2.setEmail("jane.smith@example.com");

            Book book2 = new Book();
            book2.setId(2L);
            book2.setTitle("Another Book");
            book2.setGenre("Mystery");
            book2.setAuthor(author2);

            List<Book> books = Arrays.asList(testBook, book2);
            when(bookRepository.findAll()).thenReturn(books);

            // When
            List<BookResponseDTO> result = bookService.fetchBooks();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
            assertThat(result.get(0).getAuthorName()).isEqualTo("John Doe");
            assertThat(result.get(1).getTitle()).isEqualTo("Another Book");
            assertThat(result.get(1).getAuthorName()).isEqualTo("Jane Smith");

            verify(bookRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no books exist")
        void fetchBooks_ShouldReturnEmptyList_WhenNoBooksExist() {
            // Given
            when(bookRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<BookResponseDTO> result = bookService.fetchBooks();

            // Then
            assertThat(result).isEmpty();
            verify(bookRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should handle large number of books")
        void fetchBooks_ShouldHandleLargeDataset() {
            // Given
            List<Book> largeBookList = Arrays.asList(
                createBook(1L, "Book 1", "Genre 1", testAuthor),
                createBook(2L, "Book 2", "Genre 2", testAuthor),
                createBook(3L, "Book 3", "Genre 3", testAuthor),
                createBook(4L, "Book 4", "Genre 4", testAuthor),
                createBook(5L, "Book 5", "Genre 5", testAuthor)
            );

            when(bookRepository.findAll()).thenReturn(largeBookList);

            // When
            List<BookResponseDTO> result = bookService.fetchBooks();

            // Then
            assertThat(result).hasSize(5);
            assertThat(result).extracting(BookResponseDTO::getTitle)
                    .containsExactly("Book 1", "Book 2", "Book 3", "Book 4", "Book 5");
        }

        @Test
        @DisplayName("Should handle books with null authors by throwing exception")
        void fetchBooks_ShouldThrowNullPointerException_WhenBooksHaveNullAuthors() {
            // Given
            Book bookWithNullAuthor = new Book();
            bookWithNullAuthor.setId(1L);
            bookWithNullAuthor.setTitle("Orphan Book");
            bookWithNullAuthor.setGenre("Mystery");
            bookWithNullAuthor.setAuthor(null);

            when(bookRepository.findAll()).thenReturn(Arrays.asList(bookWithNullAuthor));

            // When & Then
            assertThatThrownBy(() -> bookService.fetchBooks())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Should update book when book and author exist")
        void updateBook_ShouldReturnUpdatedBookResponseDTO_WhenBookExists() {
            // Given
            BookRequestDTO updateDTO = new BookRequestDTO();
            updateDTO.setTitle("Updated Book");
            updateDTO.setGenre("Updated Genre");
            updateDTO.setAuthorId(1L);

            Book updatedBook = new Book();
            updatedBook.setId(1L);
            updatedBook.setTitle("Updated Book");
            updatedBook.setGenre("Updated Genre");
            updatedBook.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

            // When
            BookResponseDTO result = bookService.updateBook(1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Book");
            assertThat(result.getGenre()).isEqualTo("Updated Genre");
            assertThat(result.getAuthorName()).isEqualTo("John Doe");

            verify(authorRepository, times(1)).findById(1L);
            verify(bookRepository, times(1)).findById(1L);
            verify(bookRepository, times(1)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when author does not exist")
        void updateBook_ShouldThrowRuntimeException_WhenAuthorDoesNotExist() {
            // Given
            when(authorRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(1L, testBookRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Author not found");

            verify(authorRepository, times(1)).findById(1L);
            verify(bookRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when book does not exist")
        void updateBook_ShouldThrowRuntimeException_WhenBookDoesNotExist() {
            // Given
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(999L, testBookRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no such book exists");

            verify(authorRepository, times(1)).findById(1L);
            verify(bookRepository, times(1)).findById(999L);
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should update book with different author")
        void updateBook_ShouldAllowAuthorChange() {
            // Given
            Author newAuthor = new Author();
            newAuthor.setId(2L);
            newAuthor.setName("New Author");
            newAuthor.setEmail("new.author@example.com");

            BookRequestDTO updateDTO = new BookRequestDTO();
            updateDTO.setTitle("Updated Book");
            updateDTO.setGenre("Updated Genre");
            updateDTO.setAuthorId(2L);

            Book updatedBook = new Book();
            updatedBook.setId(1L);
            updatedBook.setTitle("Updated Book");
            updatedBook.setGenre("Updated Genre");
            updatedBook.setAuthor(newAuthor);

            when(authorRepository.findById(2L)).thenReturn(Optional.of(newAuthor));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

            // When
            BookResponseDTO result = bookService.updateBook(1L, updateDTO);

            // Then
            assertThat(result.getAuthorName()).isEqualTo("New Author");
            verify(authorRepository, times(1)).findById(2L);
        }

        @Test
        @DisplayName("Should handle partial update with null fields")
        void updateBook_ShouldHandlePartialUpdate() {
            // Given
            BookRequestDTO partialUpdateDTO = new BookRequestDTO();
            partialUpdateDTO.setTitle("Partially Updated");
            partialUpdateDTO.setAuthorId(1L);
            // genre remains null

            Book partiallyUpdatedBook = new Book();
            partiallyUpdatedBook.setId(1L);
            partiallyUpdatedBook.setTitle("Partially Updated");
            partiallyUpdatedBook.setGenre("Fiction"); // Original genre preserved
            partiallyUpdatedBook.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class))).thenReturn(partiallyUpdatedBook);

            // When
            BookResponseDTO result = bookService.updateBook(1L, partialUpdateDTO);

            // Then
            assertThat(result.getTitle()).isEqualTo("Partially Updated");
            assertThat(result.getGenre()).isEqualTo("Fiction");
        }
    }

    @Nested
    @DisplayName("Delete Book Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Should delete book when book exists")
        void deleteBook_ShouldDeleteBook_WhenBookExists() {
            // Given
            when(bookRepository.existsById(1L)).thenReturn(true);
            doNothing().when(bookRepository).deleteById(1L);

            // When
            bookService.deleteBook(1L);

            // Then
            verify(bookRepository, times(1)).existsById(1L);
            verify(bookRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when book does not exist")
        void deleteBook_ShouldThrowBookNotFoundException_WhenBookDoesNotExist() {
            // Given
            when(bookRepository.existsById(anyLong())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(999L))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("Book not found with ID: 999");

            verify(bookRepository, times(1)).existsById(999L);
            verify(bookRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should handle repository exception during delete")
        void deleteBook_ShouldHandleRepositoryException() {
            // Given
            when(bookRepository.existsById(1L)).thenReturn(true);
            doThrow(new RuntimeException("Database connection failed")).when(bookRepository).deleteById(1L);

            // When & Then
            assertThatThrownBy(() -> bookService.deleteBook(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection failed");

            verify(bookRepository, times(1)).existsById(1L);
            verify(bookRepository, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("CSV Operations Tests")
    class CsvOperationsTests {

        @Test
        @DisplayName("Should create multiple books from CSV data")
        void createBooksFromCsv_ShouldCreateMultipleBooks() {
            // Given
            BookRequestDTO dto1 = new BookRequestDTO();
            dto1.setTitle("Book 1");
            dto1.setGenre("Fiction");
            dto1.setAuthorId(1L);

            BookRequestDTO dto2 = new BookRequestDTO();
            dto2.setTitle("Book 2");
            dto2.setGenre("Mystery");
            dto2.setAuthorId(1L);

            List<BookRequestDTO> dtos = Arrays.asList(dto1, dto2);

            Book savedBook1 = new Book();
            savedBook1.setTitle("Book 1");
            savedBook1.setGenre("Fiction");
            savedBook1.setAuthor(testAuthor);

            Book savedBook2 = new Book();
            savedBook2.setTitle("Book 2");
            savedBook2.setGenre("Mystery");
            savedBook2.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class)))
                    .thenReturn(savedBook1)
                    .thenReturn(savedBook2);

            // When
            bookService.createBooksFromCsv(dtos);

            // Then
            verify(authorRepository, times(2)).findById(1L);
            verify(bookRepository, times(2)).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw RuntimeException when author not found in CSV import")
        void createBooksFromCsv_ShouldThrowRuntimeException_WhenAuthorNotFound() {
            // Given
            BookRequestDTO dto = new BookRequestDTO();
            dto.setTitle("Book 1");
            dto.setGenre("Fiction");
            dto.setAuthorId(999L);

            List<BookRequestDTO> dtos = Arrays.asList(dto);

            when(authorRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookService.createBooksFromCsv(dtos))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("author not found");

            verify(authorRepository, times(1)).findById(999L);
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should handle empty CSV data")
        void createBooksFromCsv_ShouldHandleEmptyList() {
            // Given
            List<BookRequestDTO> emptyList = Collections.emptyList();

            // When
            bookService.createBooksFromCsv(emptyList);

            // Then
            verify(authorRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should export books to CSV format")
        void exportBooksToCsv_ShouldReturnCsvString() {
            // Given
            List<Book> books = Arrays.asList(testBook);
            String expectedCsv = "id,title,genre,author\n1,Test Book,Fiction,John Doe\n";
            
            when(bookRepository.findAll()).thenReturn(books);

            try (MockedStatic<ToCsv> mockedToCsv = mockStatic(ToCsv.class)) {
                mockedToCsv.when(() -> ToCsv.bookToCsv(books)).thenReturn(expectedCsv);

                // When
                String result = bookService.exportBooksToCsv();

                // Then
                assertThat(result).isEqualTo(expectedCsv);
                verify(bookRepository, times(1)).findAll();
                mockedToCsv.verify(() -> ToCsv.bookToCsv(books), times(1));
            }
        }

        @Test
        @DisplayName("Should export empty CSV when no books exist")
        void exportBooksToCsv_ShouldHandleEmptyRepository() {
            // Given
            List<Book> emptyList = Collections.emptyList();
            String expectedCsv = "id,title,genre,author\n";
            
            when(bookRepository.findAll()).thenReturn(emptyList);

            try (MockedStatic<ToCsv> mockedToCsv = mockStatic(ToCsv.class)) {
                mockedToCsv.when(() -> ToCsv.bookToCsv(emptyList)).thenReturn(expectedCsv);

                // When
                String result = bookService.exportBooksToCsv();

                // Then
                assertThat(result).isEqualTo(expectedCsv);
                verify(bookRepository, times(1)).findAll();
            }
        }

        @Test
        @DisplayName("Should handle repository exception during CSV export")
        void exportBooksToCsv_ShouldHandleRepositoryException() {
            // Given
            when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> bookService.exportBooksToCsv())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle repository returning null")
        void createBook_ShouldHandleNullRepositoryResponse() {
            // Given
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> bookService.createBook(testBookRequestDTO))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle concurrent modification scenarios")
        void updateBook_ShouldHandleConcurrentModification() {
            // Given
            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
            when(bookRepository.save(any(Book.class)))
                    .thenThrow(new RuntimeException("Optimistic locking failure"));

            // When & Then
            assertThatThrownBy(() -> bookService.updateBook(1L, testBookRequestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Optimistic locking failure");
        }

        @Test
        @DisplayName("Should verify interaction count with repositories")
        void fetchBooks_ShouldCallRepositoryOnlyOnce() {
            // Given
            when(bookRepository.findAll()).thenReturn(Arrays.asList(testBook));

            // When
            bookService.fetchBooks();
            bookService.fetchBooks(); // Second call

            // Then
            verify(bookRepository, times(2)).findAll();
        }

        @Test
        @DisplayName("Should handle books with very long titles and genres")
        void createBook_ShouldHandleExtremelyLongStrings() {
            // Given
            String extremelyLongTitle = "A".repeat(10000);
            String extremelyLongGenre = "B".repeat(5000);
            
            BookRequestDTO extremeDTO = new BookRequestDTO();
            extremeDTO.setTitle(extremelyLongTitle);
            extremeDTO.setGenre(extremelyLongGenre);
            extremeDTO.setAuthorId(1L);

            Book extremeBook = new Book();
            extremeBook.setId(1L);
            extremeBook.setTitle(extremelyLongTitle);
            extremeBook.setGenre(extremelyLongGenre);
            extremeBook.setAuthor(testAuthor);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(extremeBook);

            // When
            BookResponseDTO result = bookService.createBook(extremeDTO);

            // Then
            assertThat(result.getTitle()).hasSize(10000);
            assertThat(result.getGenre()).hasSize(5000);
        }

        @Test
        @DisplayName("Should handle multiple concurrent CSV imports")
        void createBooksFromCsv_ShouldHandleMultipleImports() {
            // Given
            BookRequestDTO dto1 = new BookRequestDTO();
            dto1.setTitle("Concurrent Book 1");
            dto1.setGenre("Fiction");
            dto1.setAuthorId(1L);

            BookRequestDTO dto2 = new BookRequestDTO();
            dto2.setTitle("Concurrent Book 2");
            dto2.setGenre("Non-Fiction");
            dto2.setAuthorId(1L);

            List<BookRequestDTO> batch1 = Arrays.asList(dto1);
            List<BookRequestDTO> batch2 = Arrays.asList(dto2);

            when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);

            // When
            bookService.createBooksFromCsv(batch1);
            bookService.createBooksFromCsv(batch2);

            // Then
            verify(bookRepository, times(2)).save(any(Book.class));
        }
    }

    // Helper method to create test books
    private Book createBook(Long id, String title, String genre, Author author) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setGenre(genre);
        book.setAuthor(author);
        return book;
    }
}
