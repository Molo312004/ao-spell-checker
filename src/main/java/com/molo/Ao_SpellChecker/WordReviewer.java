package com.molo.Ao_SpellChecker;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class WordReviewer {

    static final String INPUT_FILE = "D:\\Projects\\Ao-SpellChecker\\data\\test.txt";
    static final String DELETED_FILE = "D:\\Projects\\Ao-SpellChecker\\data\\deletedWords.txt";
    static final String CHECKPOINT_MARKER = "->checkpoint";

    public static void main(String[] args) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(INPUT_FILE)));
        Scanner scanner = new Scanner(System.in);

        // Find checkpoint (if exists)
        int pointer = findCheckpoint(lines);

        while (pointer >= 0) {
            String currentLine = lines.get(pointer).trim();
            if (currentLine.isEmpty() || currentLine.equals(CHECKPOINT_MARKER)) {
                pointer--;
                continue;
            }

            // Add checkpoint below current line
            lines = updateCheckpoint(lines, pointer);
            Files.write(Paths.get(INPUT_FILE), lines);

            System.out.println("Current word: " + currentLine);
            System.out.print("Enter (yes / no / undo): ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "yes":
                    pointer--;
                    break;

                case "no":
                    saveDeletedWord(pointer, lines.get(pointer));
                    lines.set(pointer, " "); // leave space
                    Files.write(Paths.get(INPUT_FILE), lines);
                    pointer--;
                    break;

                case "undo":
                    String restored = undoDelete(lines);
                    if (restored != null) {
                        System.out.println("Restored: " + restored);
                        Files.write(Paths.get(INPUT_FILE), lines);
                    }
                    break;

                default:
                    System.out.println("Invalid input. Please enter yes / no / undo.");
            }
        }

        // Cleanup checkpoint after finishing
        lines = removeCheckpoint(lines);
        Files.write(Paths.get(INPUT_FILE), lines);
        System.out.println("Finished processing all lines.");
    }

    // Finds checkpoint and returns line above it, or last line if none
    private static int findCheckpoint(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(CHECKPOINT_MARKER)) {
                return i - 1;
            }
        }
        return lines.size() - 1;
    }

    // Removes any previous checkpoint and adds a new one below pointer
    private static List<String> updateCheckpoint(List<String> lines, int pointer) {
        lines = removeCheckpoint(lines);
        if (pointer + 1 < lines.size()) {
            lines.set(pointer + 1, CHECKPOINT_MARKER);
        } else {
            lines.add(CHECKPOINT_MARKER);
        }
        return lines;
    }

    // Removes existing checkpoint line if present
    private static List<String> removeCheckpoint(List<String> lines) {
        return new ArrayList<>(lines.stream()
                .filter(line -> !line.trim().equals(CHECKPOINT_MARKER))
                .toList());
    }

    // Save deleted word to file in format: index|word
    private static void saveDeletedWord(int index, String word) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(DELETED_FILE, true));
        writer.write(index + "|" + word);
        writer.newLine();
        writer.close();
    }

    // Undo last deletion
    private static String undoDelete(List<String> lines) throws IOException {
        Path path = Paths.get(DELETED_FILE);
        if (!Files.exists(path)) {
            System.out.println("No deleted words to undo.");
            return null;
        }

        List<String> deletedLines = new ArrayList<>(Files.readAllLines(path));
        if (deletedLines.isEmpty()) {
            System.out.println("No deleted words to undo.");
            return null;
        }

        String lastEntry = deletedLines.remove(deletedLines.size() - 1);
        String[] parts = lastEntry.split("\\|");
        if (parts.length != 2) {
            return null;
        }

        int index = Integer.parseInt(parts[0]);
        String word = parts[1];

        lines.set(index, word);
        Files.write(path, deletedLines); // update deletedWords.txt
        return word;
    }
}
