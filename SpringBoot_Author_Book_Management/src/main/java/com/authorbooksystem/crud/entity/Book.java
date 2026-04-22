package com.authorbooksystem.crud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Book {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String genre;
//    private String author;
    @ManyToOne
    @JoinColumn(name="author_id")
    private Author author;
}
