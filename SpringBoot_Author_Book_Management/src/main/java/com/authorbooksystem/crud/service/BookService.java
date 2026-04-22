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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    BookRepository bookRepository;
    @Autowired
    AuthorRepository authorRepository;
    //C
    public BookResponseDTO createBook(BookRequestDTO dto){
        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(()-> new AuthorNotFoundException("Author not found with ID: " + dto.getAuthorId()));
         Book book= new Book();
         book.setTitle(dto.getTitle());
         book.setAuthor(author);
         book.setGenre(dto.getGenre());
         Book saved=bookRepository.save(book);
         return mapToDTO(saved);

    }
    //R
    public List<BookResponseDTO> fetchBooks(){
        return  bookRepository.findAll().stream().map(this::mapToDTO).toList();
    }
    //U
    public BookResponseDTO updateBook (Long id,BookRequestDTO dto){
        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(()-> new RuntimeException("Author not found"));
        Book existingbook=bookRepository.findById(id)
                .orElseThrow(()->new RuntimeException("no such book exists"));
                existingbook.setTitle(dto.getTitle());
                existingbook.setAuthor(author);
                existingbook.setGenre(dto.getGenre());
                Book updated=bookRepository.save(existingbook);
                return  mapToDTO(updated);
    }
    //D
    public void deleteBook(Long id){
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
    }
    public  void createBooksFromCsv(List<BookRequestDTO> dtos){
        for(BookRequestDTO dto:dtos){
            Author author=authorRepository.findById(dto.getAuthorId()).orElseThrow(()->new RuntimeException("author not found"));

            Book book=new Book();
            book.setTitle(dto.getTitle());
            book.setGenre(dto.getGenre());
            book.setAuthor(author);

            bookRepository.save(book);
        }
    }
    public String exportBooksToCsv(){
        List<Book> books=bookRepository.findAll();
        return ToCsv.bookToCsv(books);
    }
    private BookResponseDTO mapToDTO(Book book){
        BookResponseDTO dto=new BookResponseDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setGenre(book.getGenre());
        dto.setAuthorName(book.getAuthor().getName());
        return dto;
    }

}
