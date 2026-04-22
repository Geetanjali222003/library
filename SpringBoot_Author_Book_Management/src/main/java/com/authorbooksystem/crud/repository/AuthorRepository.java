package com.authorbooksystem.crud.repository;

import com.authorbooksystem.crud.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author,Long> {

}
