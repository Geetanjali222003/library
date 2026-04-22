package com.authorbooksystem.crud.utils;

import com.authorbooksystem.crud.dto.request.AuthorRequestDTO;
import com.authorbooksystem.crud.dto.request.BookRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    //readbook
    public static List<BookRequestDTO> readBooksFromCsv(MultipartFile file){
        List<BookRequestDTO> list=new ArrayList<>();

        try(BufferedReader reader=
                new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;

            reader.readLine();

            while ((line=reader.readLine()) !=null){
                String[] parts=line.split(",");


                BookRequestDTO dto=new BookRequestDTO();
                dto.setTitle(parts[0]);
                dto.setGenre(parts[1]);
                dto.setAuthorId(Long.parseLong(parts[2]));
                list.add(dto);
            }
        }
        catch (Exception e){
            throw new RuntimeException("ERROR while reading CSV file");
        }
        return list;
    }
    //read author
    public static List<AuthorRequestDTO> readAuthorsFromCsv(MultipartFile file) {
        List<AuthorRequestDTO> list2 = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;

            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");


                AuthorRequestDTO dto = new AuthorRequestDTO();
                dto.setName(parts[0]);
                dto.setEmail(parts[1]);
                list2.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR while reading CSV file");
        }
        return list2;
    }
}
