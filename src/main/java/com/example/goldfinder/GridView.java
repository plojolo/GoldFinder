package com.example.goldfinder;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class GridView {
    Canvas canvas;
    int columnCount, rowCount;
    boolean[][] goldAt, vWall, hWall, breakWall, teleport, slow;


    public GridView(Canvas canvas, int columnCount, int rowCount) {
        this.canvas = canvas;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        goldAt = new boolean[columnCount][rowCount];
        vWall = new boolean[columnCount+1][rowCount];
        hWall = new boolean[columnCount][rowCount+1];
        breakWall = new boolean[columnCount][rowCount];
        teleport = new boolean[columnCount][rowCount];
        slow = new boolean[columnCount][rowCount];
    }

    public void repaint(){
        canvas.getGraphicsContext2D().clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        for(int column =0; column<columnCount;column++)
            for(int row=0;row<rowCount;row++) {
                if (goldAt[column][row]) {
                    canvas.getGraphicsContext2D().setFill(Color.YELLOW);
                    canvas.getGraphicsContext2D().fillOval(column * cellWidth(), row * cellHeight(), cellWidth(), cellHeight());
                }
                if (breakWall[column][row]) {
                    canvas.getGraphicsContext2D().setFill(Color.ORANGE);
                    canvas.getGraphicsContext2D().fillRect(column * cellWidth(), row * cellHeight(), cellWidth(), cellHeight());
                }
                if (teleport[column][row]) {
                    canvas.getGraphicsContext2D().setFill(Color.PURPLE);
                    canvas.getGraphicsContext2D().fillRect(column * cellWidth(), row * cellHeight(), cellWidth(), cellHeight());
                }
                if (slow[column][row]) {
                    canvas.getGraphicsContext2D().setFill(Color.LIGHTBLUE);
                    canvas.getGraphicsContext2D().fillRect(column * cellWidth(), row * cellHeight(), cellWidth(), cellHeight());
                }
            }
        canvas.getGraphicsContext2D().setStroke(Color.WHITE);
        for(int column =0; column<columnCount;column++)
            for(int row=0;row<rowCount;row++){
                    if(vWall[column][row])
                        canvas.getGraphicsContext2D().strokeLine(column * cellWidth(), row * cellHeight(),column * cellWidth(), (row+1) * cellHeight());
                if(hWall[column][row])
                    canvas.getGraphicsContext2D().strokeLine(column * cellWidth(), row * cellHeight(),(column+1) * cellWidth(), row * cellHeight());
            }
        paintBorder();
    }

    private double cellWidth(){ return (canvas.getWidth()/columnCount); }
    private double cellHeight(){ return (canvas.getHeight()/rowCount); }

    public void paintToken(int column, int row) {
        canvas.getGraphicsContext2D().setFill(Color.GREEN);
        canvas.getGraphicsContext2D().fillRect(column*cellWidth(),row*cellHeight(),cellWidth(),cellHeight());
    }

    public void paintEnemy(int column, int row) {
        canvas.getGraphicsContext2D().setFill(Color.RED);
        canvas.getGraphicsContext2D().fillRect(column*cellWidth(),row*cellHeight(),cellWidth(),cellHeight());
    }

    public void paintRobber(int column, int row) {
        canvas.getGraphicsContext2D().setFill(Color.GREY);
        canvas.getGraphicsContext2D().fillRect(column*cellWidth(),row*cellHeight(),cellWidth(),cellHeight());
    }

    public void paintCop(int column, int row) {
        canvas.getGraphicsContext2D().setFill(Color.BLUE);
        canvas.getGraphicsContext2D().fillRect(column*cellWidth(),row*cellHeight(),cellWidth(),cellHeight());
    }

    public void paintGold(int column, int row) {
        goldAt[column][row] = true;
    }
    public void removeGold(int column, int row) {
        goldAt[column][row] = false;
    }
    public void paintBreakWall(int column, int row) {
        breakWall[column][row] = true;
    }
    public void removeBreakWall(int column, int row) {
        breakWall[column][row] = false;
    }
    public void paintTeleport(int column, int row) {
        teleport[column][row] = true;
    }
    public void removeTeleport(int column, int row) {
        teleport[column][row] = false;
    }
    public void paintSlow(int column, int row) {
        slow[column][row] = true;
    }
    public void removeSlow(int column, int row) {
        slow[column][row] = false;
    }

    public void paintVWall(int column, int row) {
        vWall[column][row] = true;
    }

    public void paintHWall(int column, int row) {
        hWall[column][row] = true;
    }



    public void removeVWall(int column, int row) {
        vWall[column][row] = false;
    }
    public void removeHWall(int column, int row) {
        hWall[column][row] = false;
    }

    public void paintBorder() {
        canvas.getGraphicsContext2D().setStroke(Color.WHITE);
        canvas.getGraphicsContext2D().strokeRect(0,0,canvas.getWidth(),canvas.getHeight());
    }

    public void removeAllItems(int x, int y) {
        goldAt[x][y] = false;
        breakWall[x][y] = false;
        teleport[x][y] = false;
        slow[x][y] = false;
    }
}
