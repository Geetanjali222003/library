package com.authorbooksystem.crud.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookResponseDTO {
    private Long id;
    private String title;
    private String genre;
    private String authorName;
}
