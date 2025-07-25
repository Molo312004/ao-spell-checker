package com.molo.Ao_SpellChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/words")
public class WordController {

    @Autowired
    private WordService wordService;

    @GetMapping("/remaining")
    public int getRemainingCount() {
        try {
            return wordService.getRemainingWordCount();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/current")
    public String getCurrentWord() {
        try {
            return wordService.getCurrentWord();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/respond")
    public String respond(@RequestParam String action) {
        try {
            return wordService.respond(action);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/download/{tableName}")
    public ResponseEntity<InputStreamResource> downloadWords(@PathVariable String tableName) throws IOException {
        try {
            List<String> words = wordService.getWordsFromTable(tableName);
            File tempFile = File.createTempFile(tableName, ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (String word : words) {
                    writer.write(word);
                    writer.newLine();
                }
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + tableName + ".txt")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate download file: " + e.getMessage());
        }
    }
}
