package com.authorbooksystem.crud.controller;

import com.authorbooksystem.crud.dto.request.AuthorRequestDTO;
import com.authorbooksystem.crud.dto.response.AuthorResponseDTO;
import com.authorbooksystem.crud.service.AuthorService;
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
@RequestMapping("/api/authors")
@Tag(name = "Author Management", description = "APIs for managing authors in the system")
//Used by Swagger/OpenAPI → groups and documents these APIs in Swagger UI
public class AuthorController {
    
    @Autowired
    private AuthorService authorService;

    @PostMapping
    @Operation(summary = "Create a new author", description = "Creates a new author with the provided information")
    // Part of :contentReference[oaicite:0]{index=0}
    // summary → short title shown in Swagger UI
    // description → detailed explanation of what this API does
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public AuthorResponseDTO create(@RequestBody @Valid AuthorRequestDTO dto) {
        return authorService.createAuthor(dto);
    }

    @PostMapping("/import")
    @Operation(summary = "Import authors from CSV", description = "Bulk import authors from a CSV file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authors imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid CSV file format")
    })
    public ResponseEntity<String> importAuthors(
            @Parameter(description = "CSV file containing author data")
            //Swagger → describes this parameter in UI
            @RequestParam("file") MultipartFile file) {
        // @RequestParam → reads file from request (form-data)
        // MultipartFile → represents uploaded file (CSV in this case)
        List<AuthorRequestDTO> dtos = CSVReader.readAuthorsFromCsv(file);
        // step 1: Read CSV file and convert it into DTO list
        // Each row in CSV → one AuthorRequestDTO object
        authorService.createAuthorsFromCsv(dtos);
        return ResponseEntity.ok("CSV data imported successfully");
    }

    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieves a list of all authors in the system")
    @ApiResponse(responseCode = "200", description = "List of authors retrieved successfully")
    public List<AuthorResponseDTO> getAll() {
        return authorService.getAllAuthors();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an author", description = "Updates an existing author's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public AuthorResponseDTO update(
            @Parameter(description = "ID of the author to update", required = true) 
            @PathVariable Long id,
            @RequestBody @Valid AuthorRequestDTO dto) {
        return authorService.updateAuthor(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an author", description = "Deletes an author from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the author to delete", required = true) 
            @PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.ok().build();
    }

}
