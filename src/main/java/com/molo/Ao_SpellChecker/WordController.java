package com.molo.Ao_SpellChecker;

import com.molo.Ao_SpellChecker.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/words")
public class WordController {
    @GetMapping("/remaining")
    public int getRemainingCount() {
        return wordService.getRemainingLineCount();
    }
    @GetMapping("/")
        public String homePage() {
        return "index"; // loads index.html from templates
    }

    @Autowired
    private WordService wordService;

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
}
