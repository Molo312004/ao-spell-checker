package com.molo.Ao_SpellChecker;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class WordService {

    private static final String INPUT_FILE = "D:\\Projects\\Ao-SpellChecker\\data\\Unif2.txt";
    private static final String DELETED_FILE = "D:\\Projects\\Ao-SpellChecker\\data\\deletedWords.txt";

    private List<String> lines;
    private int pointer;

    public WordService() throws IOException {
        this.lines = new ArrayList<>(Files.readAllLines(Paths.get(INPUT_FILE)));
        this.pointer = lines.size() - 1; // Start from the last line
    }

    public String getCurrentWord() {
        while (pointer >= 0) {
            String currentLine = lines.get(pointer).trim();
            if (!currentLine.isEmpty()) {
                return currentLine;
            }
            pointer--;
        }
        return null;
    }

    public String respond(String input) throws IOException {
        input = input.trim().toLowerCase();

        switch (input) {
            case "yes":
                pointer--;
                break;

            case "no":
                saveDeletedWord(pointer, lines.get(pointer));
                lines.set(pointer, " "); // Replace with space
                writeToFile();
                pointer--;
                break;

            case "undo":
                String restored = undoDelete();
                if (restored != null) {
                    writeToFile();
                    return "Restored: " + restored;
                }
                return "Nothing to undo.";

            default:
                return "Invalid input. Use yes / no / undo.";
        }

        writeToFile();
        return "OK";
    }

    public boolean isFinished() {
        return pointer < 0;
    }

    public int getRemainingLineCount() {
        int count = 0;
        for (int i = pointer; i >= 0; i--) {
            if (!lines.get(i).trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void writeToFile() throws IOException {
        Files.write(Paths.get(INPUT_FILE), lines);
    }

    private void saveDeletedWord(int index, String word) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(DELETED_FILE, true));
        writer.write(index + "|" + word);
        writer.newLine();
        writer.close();
    }

    private String undoDelete() throws IOException {
        Path path = Paths.get(DELETED_FILE);
        if (!Files.exists(path)) return null;

        List<String> deletedLines = new ArrayList<>(Files.readAllLines(path));
        if (deletedLines.isEmpty()) return null;

        String lastEntry = deletedLines.remove(deletedLines.size() - 1);
        String[] parts = lastEntry.split("\\|");
        if (parts.length != 2) return null;

        int index = Integer.parseInt(parts[0]);
        String word = parts[1];

        if (index >= 0 && index < lines.size()) {
            lines.set(index, word);
        }

        Files.write(path, deletedLines); // update deletedWords.txt
        return word;
    }
}
