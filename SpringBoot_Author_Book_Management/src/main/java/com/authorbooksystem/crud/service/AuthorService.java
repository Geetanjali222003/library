package com.authorbooksystem.crud.service;

import com.authorbooksystem.crud.dto.request.AuthorRequestDTO;
import com.authorbooksystem.crud.dto.request.BookRequestDTO;
import com.authorbooksystem.crud.dto.response.AuthorResponseDTO;
import com.authorbooksystem.crud.entity.Author;
import com.authorbooksystem.crud.entity.Book;
import com.authorbooksystem.crud.exception.AuthorNotFoundException;
import com.authorbooksystem.crud.repository.AuthorRepository;
import com.authorbooksystem.crud.repository.BookRepository;
import com.authorbooksystem.crud.utils.ToCsv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {
    //C
    @Autowired AuthorRepository authorRepository;
    @Autowired
    BookRepository bookRepository;
//    public Author createAuthor(Author author){
//        return authorRepository.save(author);
//
//    }
public AuthorResponseDTO createAuthor(AuthorRequestDTO dto){
  Author author=new Author();
  author.setName(dto.getName());
  author.setEmail(dto.getEmail());
  Author saved=authorRepository.save(author);
  return mapToDTO(saved);

}
    //R
//    public List<Author> getAllAuthors(){
//        return authorRepository.findAll();

    public List<AuthorResponseDTO> getAllAuthors(){
     return authorRepository.findAll()
             .stream()
             .map(this::mapToDTO)
             .toList();
    }
    //U
//    public Author updateAuthor(Long id, Author updatedauthor){
//        Author existingAuthor=authorRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
//        existingAuthor.setName(updatedauthor.getName());
//        existingAuthor.setEmail(updatedauthor.getEmail());
//     return authorRepository.save(existingAuthor);
    public AuthorResponseDTO updateAuthor(Long id, AuthorRequestDTO dto){
        Author existingAuthor=authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with ID: " + id));
        existingAuthor.setName(dto.getName());
        existingAuthor.setEmail(dto.getEmail());
        Author updated=authorRepository.save(existingAuthor);
        return mapToDTO(updated);
    }
    //D
    public void deleteAuthor(Long id){
        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFoundException("Author not found with ID: " + id);
        }
        authorRepository.deleteById(id);
    }
    public  void createAuthorsFromCsv(List<AuthorRequestDTO> dtos){
        for(AuthorRequestDTO dto:dtos){
            Author author=new Author();
            author.setName(dto.getName());
            author.setEmail(dto.getEmail());

            authorRepository.save(author);
        }
    }
    public String exportAuthorsToCsv(){
        List<Author> authors=authorRepository.findAll();
        return ToCsv.authorToCsv(authors);
    }
    private AuthorResponseDTO mapToDTO(Author author){
     AuthorResponseDTO dto=new AuthorResponseDTO();
     dto.setId(author.getId());
     dto.setName(author.getName());
     dto.setEmail(author.getEmail());
     return dto;
    }
}
