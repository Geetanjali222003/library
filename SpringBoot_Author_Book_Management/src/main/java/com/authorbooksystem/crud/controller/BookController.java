package com.authorbooksystem.crud.controller;

import com.authorbooksystem.crud.dto.request.BookRequestDTO;
import com.authorbooksystem.crud.dto.response.BookResponseDTO;
import com.authorbooksystem.crud.service.BookService;
import com.authorbooksystem.crud.utils.CSVReader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "APIs for managing books in the system")
public class BookController {
    
    @Autowired
    BookService bookService;

    @PostMapping
    @Operation(summary = "Create a new book", description = "Creates a new book with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    public BookResponseDTO create(@RequestBody @Valid BookRequestDTO dto) {
        return bookService.createBook(dto);
    }

    @PostMapping("/import")
    @Operation(summary = "Import books from CSV", description = "Bulk import books from a CSV file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid CSV file format")
    })
    public ResponseEntity<String> importBooks(
            @Parameter(description = "CSV file containing book data") 
            @RequestParam("file") MultipartFile file) {
        List<BookRequestDTO> dtos = CSVReader.readBooksFromCsv(file);
        bookService.createBooksFromCsv(dtos);
        return ResponseEntity.ok("CSV data imported successfully");
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves a list of all books in the system")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully")
    public List<BookResponseDTO> getAll() {
        return bookService.fetchBooks();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully"),
            @ApiResponse(responseCode = "404", description = "Book or Author not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public BookResponseDTO update(
            @Parameter(description = "ID of the book to update", required = true) 
            @PathVariable Long id,
            @RequestBody @Valid BookRequestDTO dto) {
        return bookService.updateBook(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes a book from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the book to delete", required = true) 
            @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }

}
