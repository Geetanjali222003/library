package com.authorbooksystem.crud.repository;

import com.authorbooksystem.crud.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

}
