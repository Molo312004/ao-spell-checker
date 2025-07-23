package com.molo.Ao_SpellChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/words")
public class WordController {

    @Autowired
    private WordService wordService;

    @GetMapping("/remaining")
    public int getRemainingCount() {
        return wordService.getRemainingLineCount();
    }

    @GetMapping("/")
    public String homePage() {
        return "index"; // loads index.html from templates
    }

    @GetMapping("/current")
    public String getCurrentWord() {
        if (wordService.isFinished()) {
            return "Finished processing all words.";
        }
        return wordService.getCurrentWord();
    }

    @PostMapping("/respond")
    public String respond(@RequestParam String action) {
        try {
            return wordService.respond(action);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ✅ Download endpoint for Unif2.txt and deletedWords.txt
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get("data", filename);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource file = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
