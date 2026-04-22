package com.authorbooksystem.crud.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request DTO for creating or updating a book")
public class BookRequestDTO {
    
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")
    @Schema(description = "Book title", example = "The Great Gatsby", required = true)
    private String title;
    
    @NotBlank(message = "Genre cannot be blank")
    @Size(min = 2, max = 50, message = "Genre must be between 2 and 50 characters")
    @Schema(description = "Book genre", example = "Fiction", required = true)
    private String genre;
    
    @NotNull(message = "Author ID cannot be null")
    @Schema(description = "ID of the author who wrote this book", example = "1", required = true)
    private Long authorId;
}
