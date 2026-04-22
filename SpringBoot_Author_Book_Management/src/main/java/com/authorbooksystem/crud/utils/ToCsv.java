package com.authorbooksystem.crud.utils;

import com.authorbooksystem.crud.entity.Author;
import com.authorbooksystem.crud.entity.Book;

import java.util.List;

public class ToCsv {
    public static String bookToCsv(List<Book> books){
        StringBuilder sb=new StringBuilder();
        sb.append("title,genre,authorId\n");

        for(Book book:books){
            sb.append(book.getTitle()).append(",");
            sb.append(book.getGenre()).append(",");
            sb.append(book.getAuthor().getId()).append("\n");
        }
        return sb.toString();
    }
    public static String authorToCsv(List<Author> authors){
        StringBuilder sb=new StringBuilder();
        sb.append("name,email\n");

        for(Author author:authors){
            sb.append(author.getName()).append(",");
            sb.append(author.getEmail()).append(",");
        }
        return sb.toString();
    }
}
