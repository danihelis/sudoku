package com.danihelis.sudoku;

import java.util.*;

public class Layout {

    static Layout createRegularLayout(Board board) {
        Layout layout = new Layout(board);
        layout.loadRegularLayout();
        // TODO exception
        // if (!layout.validate()) throw new Error("Invalid layout!");
        return layout;
    }

    static Layout createIrregularLayout(Board board) {
        Layout layout = new Layout(board);
        layout.loadIrregularLayout();
        // TODO exception
        // if (!layout.validate()) throw new Error("Invalid layout!");
        return layout;
    }

    class State {
        Location location;
        HashSet<Location> available;

        State(HashSet<Location> available) {
        }
    }

    Board board;
    Location location[];    // rank and index from a position
    int position[][];       // position from a group rank and index

    Layout(Board board) {
        this.board = board;
        location = new Location[board.positions];
        position = new int[board.dimension][board.dimension];
    }

    void loadRegularLayout() {
        for (int rank = 0; rank < board.dimension; rank++) {
            for (int index = 0; index < board.dimension; index++) {
                int row = (rank / board.rows) * board.rows +
                        index / board.columns;
                int column = (rank % board.rows) * board.columns +
                        index % board.columns;
                int pos = board.intoPosition(row, column);
                position[rank][index] = pos;
                location[pos] = new Location(rank, index);
            }
        }
    }

    void loadIrregularLayout() {
        for (int pos = 0; pos < board.positions; pos++) {
            location[pos] = null;
        }
        for (int rank = 0; rank < board.dimension - 1; rank++) {
            var available = new HashSet<Integer>();
            for (int pos = 0; pos < board.positions; pos++) {
                if (location[pos] == null) {
                    available.add(pos);
                    break;
                }
            }
            var index = 0;
            while (index < board.dimension) {
                if (available.isEmpty()) throw new Error("invalid state");
                int i = (int) (Math.random() * available.size());
                var pos = new Vector<Integer>(available).get(i);
                available.remove(pos);
                location[pos] = new Location(rank, index);
                position[rank][index] = pos;
                if (isStateValid(board.dimension - index - 1)) {
                    for (var border: getNeighbours(pos)) {
                        if (location[border] == null) available.add(border);
                    }
                    index++;
                } else {
                    location[pos] = null;
                }
            }
        }
        int index = 0;
        for (int pos = 0; pos < board.positions; pos++) {
            if (location[pos] == null) {
                location[pos] = new Location(board.dimension - 1, index);
                position[board.dimension - 1][index] = pos;
                index++;
            }
        }
    }

    static final int[][] D = new int[][] {
            new int[] {0, -1, 0, 1}, new int[] {-1, 0, 1, 0}};

    Vector<Integer> getNeighbours(int position) {
        var list = new Vector<Integer>();
        Location loc = board.intoLocation(position);
        for (int k = 0; k < 4; k++) {
            var n = new Location(loc.rank + D[0][k], loc.index + D[1][k]);
            if (n.isValid(board)) list.add(board.intoPosition(n));
        }
        return list;
    }

    boolean isStateValid(int notAllocated) {
        var visited = new boolean[board.positions];
        for (int pos = 0; pos < board.positions; pos++) {
            visited[pos] = location[pos] != null;
        }
        for (int pos = 0; pos < board.positions; pos++) {
            if (visited[pos]) continue;
            var stack = new Stack<Integer>();
            int count = 0;
            stack.push(pos);
            visited[pos] = true;
            while (!stack.isEmpty()) {
                var p = stack.pop();
                count++;
                for (var q: getNeighbours(p)) {
                    if (!visited[q]) {
                        visited[q] = true;
                        stack.push(q);
                    }
                }
            }
            int remaining = count % board.dimension;
            if (remaining > notAllocated) return false;
        }
        return true;
    }
}
