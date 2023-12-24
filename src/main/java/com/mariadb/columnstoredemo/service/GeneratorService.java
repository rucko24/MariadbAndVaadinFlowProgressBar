package com.mariadb.columnstoredemo.service;

import com.mariadb.columnstoredemo.entities.Book;
import com.mariadb.columnstoredemo.repository.BookRepository;
import com.vaadin.exampledata.ChanceIntegerType;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Log4j2
public class GeneratorService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final BookRepository repository;

    public void generate(int batchSize, int batches) {
        var generator = new ExampleDataGenerator<>(Book.class, LocalDateTime.now());
        generator.setData(Book::setTitle, DataType.BOOK_TITLE);
        generator.setData(Book::setAuthor, DataType.FULL_NAME);
        generator.setData(Book::setPublishDate, DataType.DATE_LAST_10_YEARS);
        generator.setData(Book::setPages, new ChanceIntegerType("integer", "{min: 20, max: 1000}"));
        generator.setData(Book::setImageData, DataType.BOOK_IMAGE_URL);
        for (int batchNumber = 0; batchNumber < batches; batchNumber++) {
            List<Book> books = generator.create(batchSize, SECURE_RANDOM.nextInt());
            repository.saveAllAndFlush(books);
            log.info("Batch " + batchNumber + " completed.");
        }
    }

}
