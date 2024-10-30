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

    /*
    class Group {
        int index;
        HashSet<Position> positions;
        HashMap<Position, HashSet<Position>> borders;

        Group(int index) {
            this.index = index;
            positions = new HashSet<>();
        }

        void init() {
            borders = new Hashtable<>();
            for (var pos: positions) {
                update(pos);
            }
        }

        void update(Position pos) {
            if (!positions.contains(pos)) {
                // TODO exception
                throw new Error("Group %d doesn't have position %s".format(
                        index, pos));
            }
            var adjacent = getAdjacentPositions(pos, false);
            borders.remove(pos);
            if (adj.size() > 0) borders.put(pos, adj);
        }

        int size() {
            return positions.size();
        }
    }
    */

    Board board;
    Location location[];    // rank and index from a position
    int position[][];       // position from a group rank and index

    /*
    Vector<Group> groups; // TODO change for array
    HashMap<Position, Group> grid; // TODO change for array
    Symmetry symmetry;
    */

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

    /*
    boolean validate() {
        boolean valid = grid.size() == board.size;
        for (int index = 0; valid && index < board.dimension; index++) {
            valid = groups.get(index).size() == board.dimension;
        }
        return valid;
    }

    static final int ROOT = Board.ROOT;
    static final int DIM = Board.DIM;

    static final int MAX_CYCLES = 100;
    static final int MAX_STEPS = 200;
    static final int TABU_TIME = 10;

    static final int DROW[] = new int[] {0, 0, 1, -1};
    static final int DCOL[] = new int[] {1, -1, 0, 0};

    static boolean LOG = false;

    enum Model {
        MONOLOTUS(
            "001111122000111222300012225330444255333444555336444855366678885666777888667777788"),
        ONION(
            "000001111002234411022333441023333341526666641522666447528868847558888877555577777");

        String layout;

        Model(String layout) {
            this.layout = layout;
        }
    }

    class Position {

        int row, column;

        Position(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public int hashCode() {
            return row * 9 + column;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Position)
                return row == ((Position) object).row &&
                       column == ((Position) object).column;
            return false;
        }

        @Override
        public String toString() {
            return String.format("(%d,%d)", row, column);
        }
    }

    class Group {

        int index;
        HashSet<Position> positions;
        Hashtable<Position, HashSet<Position>> borders;

        Group(int index) {
            this.index = index;
            positions = new HashSet<>();
        }

        void init() {
            borders = new Hashtable<>();
            for (Position pos: positions)
                update(pos);
        }

        void update(Position pos) {
            if (!positions.contains(pos))
                throw new Error(String.format(
                    "Group %d doesn't have position %s!", index, pos));
            HashSet<Position> adj = getAdjacentPositions(pos, false);
            borders.remove(pos);
            if (adj.size() > 0)
                borders.put(pos, adj);
        }

        int size() {
            return positions.size();
        }
    }

    Vector<Group> groups;
    Hashtable<Position, Group> grid;
    Symmetry symmetry;

    Layout() {
        groups = new Vector<>();
        grid = new Hashtable<>();
        for (int i = 0; i < DIM; i++)
            groups.add(new Group(i));
    }

    int getGroup(int row, int column) {
        return grid.get(new Position(row, column)).index;
    }

    void addPositionToGroup(Position pos, Group group) {
        if (group.positions.contains(pos))
            throw new Error(String.format(
                "Adding position %s to group %d that already contains it!",
                pos, group.index));
        if (grid.containsKey(pos))
            removePosition(pos);
        group.positions.add(pos);
        grid.put(pos, group);
    }

    Group removePosition(Position pos) {
        if (!grid.containsKey(pos))
            throw new Error(String.format(
                "Removing a free position %s from grid!", pos));
        Group group = grid.remove(pos);
        group.positions.remove(pos);
        return group;
    }

    void loadRegularLayout() {
        for (int row = 0; row < DIM; row++)
            for (int col = 0; col < DIM; col++) {
                int index = (row / ROOT) * ROOT + col / ROOT;
                addPositionToGroup(new Position(row, col),
                        groups.get(index));
            }
    }

    void loadIrregularLayout(String layout) {
        for (int i = 0; i < layout.length(); i++) {
            Position pos = new Position(i / DIM, i % DIM);
            int index = layout.charAt(i) - '0';
            addPositionToGroup(pos, groups.get(index));
        }
    }

    HashSet<Position> getAdjacentPositions(Position pos, boolean sameIndex) {
        HashSet<Position> list = new HashSet<>();
        int index = grid.get(pos).index;
        for (int i = 0; i < DROW.length; i++) {
            Position npos = new Position(pos.row + DROW[i],
                    pos.column + DCOL[i]);
            Group next = grid.get(npos);
            if (next != null && ((sameIndex && index == next.index) ||
                        (!sameIndex && index != next.index)))
                list.add(npos);
        }
        return list;
    }

    boolean isBridge(Position bridge) {
        Vector<Position> queue = new Vector<>();
        HashSet<Position> connected = new HashSet<>();

        Group group = grid.get(bridge);
        HashSet<Position> links = getAdjacentPositions(bridge, true);
        Position init = links.iterator().next();
        queue.add(init);
        connected.add(init);
        while (!queue.isEmpty()) {
            Position pos = queue.remove(0);
            for (int i = 0; i < DROW.length; i++) {
                Position npos = new Position(pos.row + DROW[i],
                        pos.column + DCOL[i]);
                if (!npos.equals(bridge) && !connected.contains(npos)) {
                    Group other = grid.get(npos);
                    if (other != null && other.index == group.index) {
                        queue.add(npos);
                        connected.add(npos);
                    }
                }
            }
        }
        return connected.size() != group.size() - 1;
    }

    void produceIrregularLayout() {

        Symmetry sym = Symmetry.randomLayout();
        // System.out.printf("LAYOUT SYMMETRY: %s\n", sym);

        Vector<Position> bridge = new Vector<>();
        Hashtable<Position, Integer> taboo = new Hashtable<>();
        Vector<Group> round = new Vector<>();

        // Init groups and adjacency lists
        loadRegularLayout();
        for (int index = 0; index < DIM; index++) {
            groups.get(index).init();
            round.add(groups.get(index));
        }
        Collections.shuffle(round);

        // Perform many cycles, each cycle starting with a group
        int first_index = 0;
        int cycles = 0;
        while (cycles < MAX_CYCLES) {

            // In each cycle, select a bordering position from a group
            // and add it to another; then continue the same with the
            // other one, until everything is balanced
            Group group = round.get(first_index);
            first_index = (first_index + 1) % round.size();
            int steps = 0;
            while (steps < MAX_STEPS) {

                steps++;


                if (LOG) {
                    print();
                    System.out.printf("S%03d  G=%d |G|=%d\n", steps,
                            group.index, group.size());
                }

                // Create list of available positions
                Vector<Position> available = new Vector<>();
                Vector<Position> starts = new Vector<>();
                Vector<Integer> shouldRemove = new Vector<>();
                for (Position start: group.borders.keySet())
                    for (Position cand: group.borders.get(start))
                        if (!isBridge(cand)) {
                            if (sym != Symmetry.NONE) {
                                if (sym == Symmetry.FLIP &&
                                        cand.row == DIM / 2)
                                    continue;
                                if (sym == Symmetry.MIRROR &&
                                        cand.column == DIM / 2)
                                    continue;
                                if (sym == Symmetry.ROTATION &&
                                        cand.column == DIM / 2 &&
                                        cand.row == DIM / 2)
                                    continue;
                            }
                            available.add(cand);
                            starts.add(start);
                            if (taboo.containsKey(cand))
                                shouldRemove.add(available.size() - 1);
                        }

                if (LOG) {
                    System.out.printf("A=");
                    for (int i = 0; i < available.size(); i++)
                        System.out.printf(" %s->%s[%d]%s",
                                starts.get(i), available.get(i),
                                grid.get(available.get(i)).index,
                                shouldRemove.contains(i) ? "-T" : "");
                }


                if (shouldRemove.size() < available.size()) {
                    for (int i = shouldRemove.size() - 1; i >= 0; i--) {
                        int k = shouldRemove.get(i);
                        available.remove(k);
                        starts.remove(k);
                    }
                }

                if (available.size() == 0)
                    throw new Error("Cannot create layout!");

                // Select one position
                int chosenIndex = (int) (Math.random() * available.size());
                Position chosen = available.get(chosenIndex);
                Position start = starts.get(chosenIndex);

                if (LOG) {
                    System.out.printf("\n|A-T|=%d  CHOSEN=%s [%d]  START=%s\n",
                            available.size(), chosen,
                            grid.get(chosen).index,
                            start);
                    System.console().readLine();
                }

                int times = sym == Symmetry.ROTATION ||
                            sym == Symmetry.FLIP ||
                            sym == Symmetry.MIRROR ? 2 : 1;
                for (int repeat = 0; repeat < times; repeat++) {
                    if (repeat == 1) {
                        switch (sym) {
                            case FLIP:
                                chosen = new Position(DIM - chosen.row - 1,
                                            chosen.column);
                                start = new Position(DIM - start.row - 1,
                                        start.column);
                                break;
                            case MIRROR:
                                chosen = new Position(chosen.row,
                                        DIM - chosen.column - 1);
                                start = new Position(start.row,
                                        DIM - start.column - 1);
                                break;
                            case ROTATION:
                                chosen = new Position(DIM - chosen.row - 1,
                                        DIM - chosen.column - 1);
                                start = new Position(DIM - start.row - 1,
                                        DIM - start.column - 1);
                                break;
                        }
                        group = grid.get(start);
                    }

                    // Change the grid and modify associated structures
                    Group lost = removePosition(chosen);
                    lost.borders.remove(chosen);
                    addPositionToGroup(chosen, group);
                    group.update(chosen);

                    // Update adjacencies
                    for (int k = 0; k < DROW.length; k++) {
                        Position pos = new Position(chosen.row + DROW[k],
                                chosen.column + DCOL[k]);
                        Group which = grid.get(pos);
                        if (which != null)
                            which.update(pos);
                    }

                    // Update taboos
                    Vector<Position> ended = new Vector<>();
                    Vector<Position> tabooed = new Vector<>(taboo.keySet());
                    for (Position pos: tabooed) {
                        int value = taboo.get(pos) - 1;
                        taboo.put(pos, value);
                        if (value <= 0)
                            ended.add(pos);
                    }
                    for (Position pos: ended)
                        taboo.remove(pos);
                    taboo.put(chosen, TABU_TIME);

                    group = lost;
                }

                // Pass on to next group
                if (group.size() == DIM)
                    break;
            }

            // Increment cycles
            cycles += steps;
        }
        if (!validate())
            throw new Error("Invalid final configuration!");
        symmetry = sym;
    }

    boolean validate() {
        boolean valid = grid.size() == DIM * DIM;
        for (int index = 0; valid && index < DIM; index++)
            valid = groups.get(index).size() == DIM;
        return valid;
    }

    void print() {
        for (int row = 0; row < DIM; row++) {
            for (int col = 0; col < DIM; col++) {
                Group group = grid.get(new Position(row, col));
                System.out.print(group != null ? " " + group.index : " .");
            }
            System.out.println();
        }
    }

    static Layout createRegularLayout() {
        Layout layout = new Layout();
        layout.loadRegularLayout();
        if (!layout.validate())
            throw new Error("Invalid layout!");
        return layout;
    }

    static Layout createLayout(String code) {
        Layout layout = new Layout();
        layout.loadIrregularLayout(code);
        if (!layout.validate())
            throw new Error("Invalid layout!");
        return layout;
    }

    static Layout createLayout(Model model) {
        return createLayout(model.layout);
    }

    static Layout createRandomLayout() {
        Layout layout = new Layout();
        //System.out.printf("[");
        layout.produceIrregularLayout();
        //System.out.printf("]");
        return layout;
    }

    static Symmetry createRandomParityCells(Board board, Symmetry symmetry) {
        HashSet<Integer> cells;
        int parity;
        int attempts = 0;

        do {
            if (++attempts > 5)
                return null;
            Vector<Integer> positions = new Vector<>();
            for (int i = 0; i < Board.SIZE; i++)
                positions.add(i);
            Collections.shuffle(positions);

            parity = Math.random() < 0.5 ? Board.PARITY_EVEN : Board.PARITY_ODD;
            symmetry = symmetry == Symmetry.RANDOM ?
                    Symmetry.randomParityLayout() : symmetry;
            cells = new HashSet<>();

            for (int pos: positions)
                if (board.solution[pos] % 2 == parity && !cells.contains(pos)) {
                    HashSet<Integer> candidates = new HashSet<>();
                    candidates.add(pos);
                    int r = board.getRegionFromPosition(Region.ROW, pos);
                    int c = board.getRegionFromPosition(Region.COLUMN, pos);
                    if (symmetry == Symmetry.MIRROR
                            || symmetry == Symmetry.DOUBLE_ROTATION
                            || symmetry == Symmetry.DOUBLE_MIRROR)
                        candidates.add(board.getPositionFromCoord(
                                r, Board.DIM - c - 1));
                    if (symmetry == Symmetry.FLIP
                            || symmetry == Symmetry.DOUBLE_ROTATION
                            || symmetry == Symmetry.DOUBLE_MIRROR)
                        candidates.add(board.getPositionFromCoord(
                                Board.DIM - r - 1, c));
                    if (symmetry == Symmetry.ROTATION
                            || symmetry == Symmetry.DOUBLE_MIRROR
                            || symmetry == Symmetry.DOUBLE_ROTATION)
                        candidates.add(board.getPositionFromCoord(
                                Board.DIM - r - 1, Board.DIM - c - 1));
                    if (symmetry == Symmetry.TRANSPOSE
                            || symmetry == Symmetry.DOUBLE_ROTATION)
                        candidates.add(board.getPositionFromCoord(c, r));
                    if (symmetry == Symmetry.DOUBLE_ROTATION) {
                        candidates.add(board.getPositionFromCoord(
                                c, Board.DIM - r - 1));
                        candidates.add(board.getPositionFromCoord(
                                Board.DIM - c - 1, r));
                        candidates.add(board.getPositionFromCoord(
                                Board.DIM - c - 1, Board.DIM - r - 1));
                    }
                    boolean valid = true;
                    for (int position: candidates)
                        if (board.solution[position] % 2 != parity) {
                            valid = false;
                            break;
                        }
                    if (valid)
                        cells.addAll(candidates);
                    if (cells.size() > 10)
                        break;
                }
        } while (cells.size() < 5);
        // System.out.printf("Parity Symmetry: %s", symmetry);
        board.setParityCells(cells);
        return symmetry;
    }
    */
}
