package com.example.goldfinder.server.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class LeaderBoard {
    private static HashMap<String, Integer> leaderBoard;
    static File file = new File("leaderBoard.txt");

    // init leaderBoard file if not exists
    static {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void saveLeaderBoard() {
        createLeaderBoardFile();
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            leaderBoard.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(e -> writer.println(e.getKey() + " : " + e.getValue()));
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void loadLeaderBoard() {
        createLeaderBoardFile();
        leaderBoard = new HashMap<>();
        try {
            java.util.Scanner scanner = new java.util.Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" : ");
                leaderBoard.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addScore(String name, int score) {
        loadLeaderBoard();
        if (leaderBoard == null) {
            leaderBoard = new HashMap<>();
        }
        if (leaderBoard.containsKey(name)) {
            leaderBoard.put(name, leaderBoard.get(name) + score);
        } else {
            leaderBoard.put(name, score);
        }
        saveLeaderBoard();
    }

    public static synchronized String getLeaderBoard(int n){
        loadLeaderBoard();
        if (leaderBoard == null) {
            return "No leaderBoard yet";
        }
        StringBuilder sb = new StringBuilder();
        leaderBoard.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(n)
                .forEach(e -> sb.append(e.getKey().trim()).append(":").append(e.getValue()).append("\\n"));
        return sb.toString();
    }

    private static synchronized void createLeaderBoardFile() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}