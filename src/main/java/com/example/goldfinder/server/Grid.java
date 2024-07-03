package com.example.goldfinder.server;

import java.util.*;

public class Grid {
    public int goldCount = 0;

    int slowCount;
    int teleportCount;
    int breakWallCount;

    boolean[][] hWall, vWall;
    String[][] items;
    int columnCount, rowCount;

    private final Random random;

    public Grid(int columnCount, int rowCount, Random random) {
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.random = random;

        RandomMaze randomMaze = new RandomMaze(columnCount, rowCount, .1, random);
        randomMaze.generate();
        hWall = randomMaze.hWall;
        vWall = randomMaze.vWall;

        slowCount = columnCount * rowCount / 10;
        teleportCount = columnCount * rowCount / 10;
        breakWallCount = columnCount * rowCount / 10;

        items = new String[columnCount][rowCount];

        generateItems();
        System.out.println(Arrays.deepToString(items));

    }
    // put randomly all items in the grid and fill the gris with golds
    public void generateItems(){

        List<int[]> allPositions = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                allPositions.add(new int[] { i, j });
            }
        }

        Collections.shuffle(allPositions);
        System.out.println(slowCount + " " + teleportCount + " " + breakWallCount + " " + goldCount);
        // put all items
        for (int i = 0; i < slowCount; i++) {
            int[] position = allPositions.remove(0);
            items[position[0]][position[1]] = "SLOW";
        }

        for (int i = slowCount; i < slowCount + teleportCount; i++) {
            int[] position = allPositions.remove(0);
            items[position[0]][position[1]] = "TELEPORT";
        }

        for (int i = slowCount + teleportCount; i < slowCount + teleportCount + breakWallCount; i++) {
            int[] position = allPositions.remove(0);
            items[position[0]][position[1]] = "BREAKWALL";
        }

        // fill the rest with gold
        goldCount = columnCount * rowCount - slowCount - teleportCount - breakWallCount;
        for (int i = slowCount + teleportCount + breakWallCount; i < columnCount * rowCount; i++) {
            int[] position = allPositions.remove(0);
            items[position[0]][position[1]] = "GOLD";
        }


    }

    public boolean leftWall(int column, int row) {
        if (column == 0)
            return true;
        return vWall[column][row];
    }

    public boolean rightWall(int column, int row) {
        if (column == columnCount - 1)
            return true;
        return vWall[column + 1][row];
    }

    public boolean upWall(int column, int row) {
        if (row == 0)
            return true;
        return hWall[column][row];
    }

    public boolean downWall(int column, int row) {
        if (row == rowCount - 1)
            return true;
        return hWall[column][row + 1];
    }

    boolean hasItem(int column, int row) {
        return !items[column][row].equals("EMPTY");
    }

    public void removeItem(int column, int row) {
        if (items[column][row].equals("GOLD")) {
            goldCount--;
        }
        items[column][row] = "EMPTY";
    }

    public String getItem(int column, int row) {
        return items[column][row];
    }



    public void removeBreakWall(int column, int row) {
        breakWallCount--;
        items[column][row] = "EMPTY";
    }

    public void breakWalls(int x, int y) {
        // remove adjencent walls
        if (x > 0) {
            if (vWall[x][y]) {
                vWall[x][y] = false;
            }
        }
        if (x < columnCount - 1) {
            if (vWall[x + 1][y]) {
                vWall[x + 1][y] = false;
            }
        }
        if (y > 0) {
            if (hWall[x][y]) {
                hWall[x][y] = false;
            }
        }
        if (y < rowCount - 1) {
            if (hWall[x][y + 1]) {
                hWall[x][y + 1] = false;
            }
        }

    }
}
