package com.molo.Ao_SpellChecker;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.io.*;

@Service
public class WordService {

    private final Connection conn;

    public WordService() throws SQLException {
        String url = "jdbc:postgresql://dpg-d4cp86idbo4c73dddfng-a.singapore-postgres.render.com:5432/ao_spellchecker?sslmode=require";
        String user = "molo_user";
        String password = "9vXkHBEUksHtHqdlPwyIfXK812BlZp1o";


        this.conn = DriverManager.getConnection(url, user, password);
    }

    public String getCurrentWord() throws SQLException {
        String sql = "SELECT id, word FROM words ORDER BY id DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("word");
            }
        }
        return null;
    }

    public String respond(String input) throws SQLException {
        input = input.trim().toLowerCase();
        String currentWord = getCurrentWord();
        if (currentWord == null) return "No words remaining.";

        switch (input) {
            case "yes":
                moveWord(currentWord, "verified_ao_words");
                return "Moved to verified words.";
            case "no":
                moveWord(currentWord, "deleted_ao_words");
                return "Moved to deleted words.";
            case "undo":
                return undoLastDelete();
            default:
                return "Invalid input. Use yes / no / undo.";
        }
    }

    public boolean isFinished() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM words")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    public int getRemainingWordCount() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM words")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<String> getWordsFromTable(String tableName) throws SQLException {
        List<String> words = new ArrayList<>();
        String sql = "SELECT word FROM " + tableName + " ORDER BY id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                words.add(rs.getString("word"));
            }
        }
        return words;
    }

    private void moveWord(String word, String targetTable) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String insertSQL = "INSERT INTO " + targetTable + "(word) VALUES (?) ON CONFLICT DO NOTHING";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, word);
                ps.executeUpdate();
            }

            String deleteSQL = "DELETE FROM words WHERE word = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, word);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private String undoLastDelete() throws SQLException {
        conn.setAutoCommit(false);
        try {
            String sql = "SELECT id, word FROM deleted_ao_words ORDER BY id DESC LIMIT 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String word = rs.getString("word");

                    String insertSQL = "INSERT INTO words(word) VALUES (?) ON CONFLICT DO NOTHING";
                    try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                        ps.setString(1, word);
                        ps.executeUpdate();
                    }

                    String deleteSQL = "DELETE FROM deleted_ao_words WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    conn.commit();
                    return "Restored: " + word;
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        return "Nothing to undo.";
    }

    public void downloadTableToFile(String tableName, String filePath) throws SQLException, IOException {
        String sql = "SELECT word FROM " + tableName + " ORDER BY id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            while (rs.next()) {
                writer.write(rs.getString("word"));
                writer.newLine();
            }
        }
    }
}
