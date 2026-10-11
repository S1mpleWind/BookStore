package com.reins.bookstore.service;

import com.reins.bookstore.entity.Book;
import com.reins.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AssistantSkillService {
    private final BookRepository bookRepository;

    public AssistantSkillService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    //* skills are defined here
    public Map<String, Object> checkInventory(String isbn) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isbn", isbn);
        if (isbn == null || isbn.isBlank()) {
            result.put("found", false);
            result.put("message", "ISBN 不能为空");
            return result;
        }

        Book book = bookRepository.findByIsbn(isbn.trim()).orElse(null);
        if (book == null) {
            result.put("found", false);
            result.put("message", "本店未找到该 ISBN 对应的图书");
            return result;
        }

        int inventory = book.getInventory() == null ? 0 : book.getInventory();
        result.put("found", true);
        result.put("title", book.getTitle());
        result.put("inventory", inventory);
        result.put("inStock", inventory > 0);
        return result;
    }

    public Map<String, Object> getCompetitorPrice(String isbn) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isbn", isbn);
        result.put("competitor", "模拟竞品商城");
        result.put("currency", "CNY");
        result.put("status", "simulated");

        int priceCents = bookRepository.findByIsbn(isbn == null ? "" : isbn.trim())
                .map(book -> {
                    int local = book.getPrice() == null ? 0 : (int) Math.round(book.getPrice());
                    int adjustment = Math.abs((isbn == null ? 0 : isbn.hashCode()) % 801) - 400;
                    return Math.max(100, local + adjustment);
                })
                .orElseGet(() -> 1999 + Math.abs((isbn == null ? 0 : isbn.hashCode()) % 2001));
        result.put("price", priceCents);
        return result;
    }
}
