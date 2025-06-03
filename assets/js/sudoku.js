"use strict";

function shuffle_array(array) {
  for (let i = 0; i < array.length - 1; i++) {
    let j = i + Math.floor(Math.random() * (array.length - i));
    if (i != j) {
      let swap = array[i];
      array[i] = array[j];
      array[j] = swap;
    }
  }
  return array;
}

const choice_array = (array) => array[Math.floor(Math.random() * array.length)];

function into_value(bit) {
  let value = 0;
  while (bit > 0) {
    value++;
    bit >>= 1;
  }
  return value;
}

const into_bit = (value) => 1 << (value - 1);

const div = (a, b) => (a - a % b) / b;

function difficulty_level(difficulty) {
  switch (difficulty) {
    case "easy": return 1;
    case "normal": return 2;
    case "hard": return 3;
  }
  return 0;
}

class DigitStream {

  static encode(array) {
    let content = new Uint8Array(1 + Math.ceil(array.length / 2));
    content[0] = array.length;
    for (let i = 0, j = 1; i < array.length; i += 2, j++) {
      content[j] = (array[i] % 0xf) | (((array[i + 1] || 0) % 0xf) << 4);
    }
    console.log(array);
    console.log(content);
    return content.toBase64();
  }

  static decode(string) {
    console.log(string);
    let content = Uint8Array.fromBase64(string);
    console.log(content);
    let array = [];
    let size = content[0];
    if (size !== (content.length - 1) * 2
        && size + 1 !== (content.length - 1) * 2) {
      throw "invalid content size";
    }
    for (let i = 1; i < content.length; i++) {
      let value = content[i];
      array.push(value & 0xf);
      if (array.length < size) array.push((value >> 4) & 0xf);
    }
    console.log(array);
    return array;
  }
}


class Cell {

  static DIAGONAL_1 = 0;
  static DIAGONAL_2 = 1;
  static BOTH_DIAGONALS = 2;

  constructor(rank, index, region = "row") {
    this.rank = rank;
    this.index = index;
    this.region = region;
  }

  get_ranks() {
    return this.region === "diagonal" && this.rank === Cell.BOTH_DIAGONALS
        ? [Cell.DIAGONAL_1, Cell.DIAGONAL_2] : [this.rank];
  }

  equals(object) {
    return this.rank === object?.rank && this.index === object?.index
      && this.region === object?.region;
  }

  is_valid(board) {
    return this.rank >= 0 && this.rank < board.ranks(this.rank)
        && this.index >= 0 && this.index < board.dimension;
  }

  into_position(board) {
    return board.into_position(this);
  }

  static from_position(board, position, region = "row") {
    return board.into_cell(position, region);
  }

  into_array() {
    return [this.rank, this.index];
  }
}

class Board {

  rows;       // number of rows in the inner box
  columns;    // number of columns in the inner box
  dimension;  // number of different values in a box
  positions;  // number of values in the whole board
  regions;    // number of regions (3 or 4)
  given;      // value of a given in position (0 if none)
  solution;   // solution in position (0 if unknown)
  candidate;  // bit mask with candidates in position
  possible;   // number of possible candidates in position

  constructor(type = "standard", layout_symmetry, rows = 3, columns = 3,
      skip_layout = false) {
    this.rows = rows || 3;
    this.columns = columns || 3;
    this.type = type;
    this.dimension = this.rows * this.columns;
    this.positions = this.dimension * this.dimension;
    this.given = new Int16Array(this.positions);
    this.solution = new Int16Array(this.positions);
    this.candidate = new Int16Array(this.positions);
    this.possible = new Int16Array(this.positions);
    this.regions = ["row", "column", "box"];
    if (type === "diagonal") this.regions.push(type);
    this.layout = skip_layout ? null
        : type != "irregular" ? Layout.create_regular_layout(this)
        : Layout.create_irregular_layout(this, layout_symmetry);
  }

  static copy(board) {
    let copied = new Board(board.type, null, board.rows, board.columns, true);
    copied.given = board.given.slice();
    copied.solution = board.solution.slice();
    copied.candidate = board.candidate.slice();
    copied.possible = board.possible.slice();
    copied.regions = board.regions;
    copied.difficulty = board.difficulty;
    copied.symmetry = board.symmetry;
    copied.layout = board.layout;
    return copied;
  }

  create_position_array() {
    let list = new Int16Array(this.positions);
    for (let i = 0; i < this.positions; i++) list[i] = i;
    return list;
  }

  into_position(cell) {
    switch (cell.region) {
      case "row":
        return cell.rank * this.dimension + cell.index;
      case "column":
        return cell.index * this.dimension + cell.rank;
      case "box":
        return this.layout.position[cell.rank][cell.index];
      case "diagonal":
        return cell.index * this.dimension
          + (rank === 0 ? index : dimension - index - 1);
    }
    debugger;
    throw `invalid region: ${ cell.region }`;
  }

  into_cell(position, region = "row") {
    switch (region) {
      case "row":
        return new Cell(div(position, this.dimension),
            position % this.dimension, region);
      case "column":
        return new Cell(position % this.dimension,
            div(position, this.dimension), region);
      case "box":
        return this.layout.cell[position];
      case "diagonal":
        let coord = this.into_cell(position, "row");
        let diagonal_1 = coord.rank === coord.index;
        let diagonal_2 = coord.rank === this.dimension - coord.index - 1;
        if (!diagonal_1 && !diagonal_2) return null;
        return new Cell(main && second ? Cell.BOTH_DIAGONALS
            : diagonal_1 ? Cell.DIAGONAL_1 : Cell.DIAGONAL_2,
            div(coord.rank, this.dimension), region);
    }
    throw `invalid region: ${ cell.region }`;
  }

  ranks(region) {
    return region === "diagonal" ? 2 : this.dimension;
  }

  get_symmetric_positions(position, symmetry) {
    let list = [];
    let cell = this.into_cell(position);
    symmetry = symmetry ?? this.symmetry;
    let self = this;

    function push(row, column) {
      list.push(self.into_position(new Cell(row, column)));
    }

    if (symmetry === "mirror" || symmetry === "double_rotation"
        || symmetry === "double_mirror") {
      push(cell.rank, this.dimension - cell.index - 1);
    }
    if (symmetry === "flip" || symmetry === "double_rotation"
        || symmetry === "double_mirror") {
      push(this.dimension - cell.rank - 1, cell.index);
    }
    if (symmetry === "rotation" || symmetry === "double_rotation"
        || symmetry === "double_mirror") {
      push(this.dimension - cell.rank - 1, this.dimension - cell.index - 1);
    }
    if (symmetry === "transpose" || symmetry === "double_rotation") {
      push(cell.index, cell.rank);
    }
    if (symmetry === "double_rotation") {
      push(cell.index, this.dimension - cell.rank - 1);
      push(this.dimension - cell.index - 1, cell.rank);
      push(this.dimension - cell.index - 1, this.dimension - cell.rank - 1);
    }
    return list;
  }

  reset_solution() {
    this.solution.fill(0);
    this.candidate.fill((1 << this.dimension) - 1);
    this.possible.fill(this.dimension);
    for (let pos = 0; pos < this.positions; pos++) {
      if (this.given[pos]) this.mark_value(pos, this.given[pos]);
    }
  }

  is_solved() {
    for (let pos = 0; pos < this.positions; pos++) {
      if (!this.solution[pos]) return false;
    }
    return true;
  }

  mark(position) {
    this.mark_bit(position, this.candidate[position]);
  }

  mark_value(position, value) {
    this.solution[position] = value;
    this.update_candidates(position, into_bit(value));
  }

  mark_bit(position, bit) {
    this.solution[position] = into_value(bit);
    this.update_candidates(position, bit);
  }

  update_candidates(position, bit) {
    for (let region of this.regions) {
      let cell = this.into_cell(position, region);
      if (!cell) continue;

      if (region === "diagonal" && cell.rank === Cell.BOTH_DIAGONALS) {
        for (let index = 0; index < this.dimension; index++) {
          let pos = this.into_position(new Cell(Cell.DIAGONAL_1, index, region));
          this.remove_candidates(pos, bit);
        }
        cell.rank = Cell.DIAGONAL_2;
      }

      for (let index = 0; index < this.dimension; index++) {
        let pos = this.into_position(new Cell(cell.rank, index, region));
        this.remove_candidates(pos, bit);
      }
    }
    this.candidate[position] = 0;
    this.possible[position] = 0;
  }

  remove_candidates(position, mask) {
    let removed = mask & this.candidate[position];
    this.candidate[position] &= ~mask;
    for (; removed > 0; removed >>= 1) {
      if ((removed & 1) != 0) this.possible[position]--;
    }
  }

  get_positions_with_candidate(region, rank, bit) {
    let list = [];
    for (let index = 0; index < this.dimension; index++) {
      let pos = this.into_position(new Cell(rank, index, region));
      if (this.candidate[pos] & bit) list.push(pos);
    }
    return list;
  }

  get_rank(row, col) {
    let pos = this.into_position(new Cell(row % this.dimension,
        col % this.dimension));
    return this.layout.location[pos].rank;
  }

  export() {
    let array = [...this.given];
    array.unshift(this.type === "diagonal" ? 1
        : this.type === "irregular" ? 2 : 0);
    return DigitStream.encode(array);
  }

  static import(sequence) {
    let array = DigitStream.decode(sequence);
    let type = array[0] === 0 ? "standard" : array[0] === "diagonal" ? 1
        : array[0] === "irregular" ? 2 : null;
    if (type === null) throw "invalid board type";

    let board = new Board(type);
    for (let pos = 0; pos < board.positions; pos++) {
      board.given[pos] = array[pos + 1];
    }
    return board;
  }

  invalid_solution_at(position) {
    let value = this.solution[position];
    if (!value) return false;
    for (let region of this.regions) {
      let cell = this.into_cell(position, region);
      for (let rank of cell.get_ranks() ?? []) {
        for (let index = 0; index < this.dimension; index++) {
          if (index !== cell.index) {
            let pos = this.into_position(new Cell(rank, index, region));
            if (this.given[pos] === value || this.solution[pos] === value) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
}


class Layout {

  static create_regular_layout(board) {
    let layout = new Layout(board);
    layout.load_regular_layout();
    return layout;
  }

  static create_irregular_layout(board, symmetry) {
    let layout = new Layout(board);
    let created = false;
    while (!created) {
      try {
        layout.load_irregular_layout(symmetry);
        created = true;
      } catch (e) {
        if (e !== "invalid layout") throw e;
      }
    }
    return layout;
  }

  location;   // rank and index from a position
  position;   // position from a group rank and index

  constructor(board) {
    this.board = board;
    this.cell = new Array(board.positions);
    this.position = new Array(board.dimension);
    for (let i = 0; i < this.position.length; i++) {
      this.position[i] = new Int16Array(board.dimension);
    }
  }

  load_regular_layout() {
    for (let rank = 0; rank < this.board.dimension; rank++) {
      for (let index = 0; index < this.board.dimension; index++) {
        let row = div(rank, this.board.rows) * this.board.rows
            + div(index, this.board.columns);
        let column = (rank % this.board.rows) * this.board.columns
            + index % this.board.columns;
        let pos = this.board.into_position(new Cell(row, column));
        this.position[rank][index] = pos;
        this.cell[pos] = new Cell(rank, index);
      }
    }
  }

  load_irregular_layout(symmetry) {
    if (!symmetry) {
      symmetry = "rotation";
    } else if (symmetry == "random") {
      symmetry = choice_array(["rotation", "mirror", "flip"]);
    } else if (symmetry === "none") {
      symmetry = null;
    }

    this.cell.fill(null);
    for (let rank = 0; rank < this.board.dimension - 1; rank++) {
      let same = false;
      let available = new Set();
      for (let pos = 0; pos < this.board.positions; pos++) {
        if (!this.cell[pos]) {
          available.add(pos);
          break;
        }
      }
      let index = 0;
      while (index < this.board.dimension) {
        if (!available.size) throw "invalid layout";
        let i = Math.floor(Math.random() * available.size);
        let pos = [...available][i];
        let spos = null;
        available.delete(pos);
        if (this.cell[pos]) continue;

        if (symmetry) {
          spos = this.board.get_symmetric_positions(pos, symmetry)[0];
          if (spos === pos && !same) {
            if (index > this.board.dimension / 2) continue;
            for (spos = 0; spos < this.board.positions; spos++) {
              if (this.cell[spos] && this.cell[spos].rank === rank + 1) {
                this.cell[spos].rank--;
                this.cell[spos].index += index;
                this.position[rank][index] = spos;
              }
            }
            same = true;
            index *= 2;
            spos = null;
          } else if (spos === pos) {
            spos = null;
          } else {
            if (same && index >= this.board.dimension - 1) continue;
            let srank = same ? rank : rank + 1;
            this.cell[spos] = new Cell(srank, index);
            this.position[srank][index] = spos;
            if (available.has(spos)) available.delete(spos);
          }
        }

        this.cell[pos] = new Cell(rank, index);
        this.position[rank][index] = pos;

        let not_allocated = this.board.dimension - index - 1;
        if (symmetry && !same) not_allocated *= 2;
        if (this.is_state_valid(not_allocated)) {
          for (const border of this.get_neighbours(pos)) {
            if (!this.cell[border]) available.add(border);
          }
          index++;
          if (same && spos) index++;
        } else {
          this.cell[pos] = null;
          if (spos) this.cell[spos] = null;
        }
      }
      if (symmetry && !same) rank++;
    }

    let index = 0;
    for (let pos = 0; pos < this.board.positions; pos++) {
      if (!this.cell[pos]) {
        this.cell[pos] = new Cell(this.board.dimension - 1, index);
        this.position[this.board.dimension - 1][index] = pos;
        index++;
      }
    }
  }

  static D = [[0, -1, 0, 1], [-1, 0, 1, 0]];

  get_neighbours(position) {
    let list = [];
    let cell = this.board.into_cell(position);
    for (let k = 0; k < 4; k++) {
      let n = new Cell(cell.rank + Layout.D[0][k], cell.index + Layout.D[1][k]);
      if (n.is_valid(this.board)) list.push(this.board.into_position(n));
    }
    return list;
  }

  is_state_valid(not_allocated) {
    let visited = new Array(this.board.positions);
    for (let pos = 0; pos < this.board.positions; pos++) {
      visited[pos] = this.cell[pos] && true;
    }
    for (let pos = 0; pos < this.board.positions; pos++) {
      if (visited[pos]) continue;
      let stack = [];
      let count = 0;
      stack.push(pos);
      visited[pos] = true;
      while (stack.length) {
        let p = stack.pop();
        count++;
        for (const q of this.get_neighbours(p)) {
          if (!visited[q]) {
            visited[q] = true;
            stack.push(q);
          }
        }
      }
      let remaining = count % this.board.dimension;
      if (remaining > not_allocated) return false;
    }
    return true;
  }
}


class Solver {

  static SOLUTION_FOUND = 1;
  static SOLUTION_NOT_FOUND = -1;

  constructor(board) {
    this.board = board;
    board.reset_solution();
    board.difficulty = null;
    this.techniques = this.guesses = this.solutions = 0;
  }

  has_unique_solution() {
    return this.solutions === 1;
  }

  solve_and_evaluate(may_guess) {
    let result = this.solve(may_guess, true);
    this.board.boring = this.board.difficulty === "normal"
        && this.techniques < 5;
    return this.has_unique_solution();
  }

  solve(may_guess, check_uniqueness) {
    let result;
    while (!result) {
      result = this.solve_one_step();
    }
    if (result === Solver.SOLUTION_FOUND) {
      if (this.solutions === 0) {
        this.board.difficulty = this.guesses > 0 ? "hard"
            : this.techniques > 0 ? "normal" : "easy";
      }
      this.solutions++;
      return true;
    }
    if (!may_guess) return false;
    if (++this.guesses > 1000) throw "too many guesses";

    let position = -1;
    let less = this.board.dimension + 1;
    for (let i = 0; i < this.board.positions; i++) {
      let pos = this.randomized?.[i] ?? i;
      if (!this.board.candidate[pos] && !this.board.solution[pos]) {
        return false;
      } else if (this.board.candidate[pos]) {
        if (this.board.possible[pos] < less) {
          position = pos;
          less = this.board.possible[pos];
        }
      }
    }
    let backup = Board.copy(this.board);
    let mask = this.board.candidate[position];
    for (let i = 0, bit = 1; i < this.board.dimension; i++, bit <<= 1) {
      if (mask & bit) {
        this.board.mark_bit(position, bit);
        let solved = this.solve(true, check_uniqueness);
        if (check_uniqueness && this.solutions > 1) return false;
        if (solved && !check_uniqueness) return true;
        let difficulty = this.board.difficulty;
        this.board = backup;
        this.board.difficulty = difficulty;
      }
    }
    return false;
  }

  solve_one_step() {
    if (this.board.is_solved()) return Solver.SOLUTION_FOUND;
    if (this.mark_single_on_cell() || this.mark_single_on_region()) {
      return;
    }
    if (this.guesses === 0) this.techniques++;
    if (this.board.type === "diagonal"
        && this.check_pointing_pair("diagonal")) {
      return;
    } else if (this.board.type === "irregular"
        && this.check_pointing_pair("box")) {
      return;
    } else if (this.check_naked_pairs() || this.check_grid_reduction()
        || this.check_hidden_pairs()) {
      return;
    }
    return Solver.SOLUTION_NOT_FOUND;
  }

  mark_single_on_cell() {
    let change = false;
    for (let pos = 0; pos < this.board.positions; pos++) {
      if (this.board.possible[pos] === 1) {
        this.board.mark(pos);
        if (this.debug) {
          console.log("mark_single_on_cell", this.board.into_cell(pos), "=>",
              this.board.solution[pos]);
        }
        change = true;
      }
    }
    return change;
  }

  mark_single_on_region() {
    let positions = new Int16Array(this.board.dimension);
    for (let region of this.board.regions) {
      for (let rank = 0; rank < this.board.ranks(region); rank++) {
        let once = 0;
        let many = 0;
        for (let index = 0; index < this.board.dimension; index++) {
          let cell = new Cell(rank, index, region);
          positions[index] = this.board.into_position(cell);
          let mask = this.board.candidate[positions[index]];
          many |= mask & once;
          once |= mask;
        }
        let unique = once & ~many;
        if (!unique) continue;

        for (let pos of positions) {
          let mask = unique & this.board.candidate[pos];
          if (!mask) continue;
          this.board.mark_bit(pos, mask);
          if (this.debug) {
            console.log("mark_single_on_region", [region, rank], "cell",
                this.board.into_cell(pos), "=>", this.board.solution[pos]);
          }
        }
        return true;
      }
    }
    return false;
  }

  check_naked_pairs() {
    let positions = new Int16Array(this.board.dimension);
    let mask = new Int16Array(this.board.dimension);
    for (let region of this.board.regions) {
      for (let rank = 0; rank < this.board.ranks(region); rank++) {
        let total = 0;
        for (let index = 0; index < this.board.dimension; index++) {
          let cell = new Cell(rank, index, region);
          positions[index] = this.board.into_position(cell);
          if (this.board.possible[positions[index]] === 2) {
            mask[total++] = this.board.candidate[positions[index]];
          }
        }

        let modified = false;
        for (let i = 0; i < total - 1; i++) {
          for (let j = i + 1; j < total; j++) {
            if (mask[i] !== mask[j]) continue;
            for (let k = 0; k < this.board.dimension; k++) {
              let cand = this.board.candidate[positions[k]];
              if (cand !== mask[i] && (cand & mask[i])) {
                this.board.remove_candidates(positions[k], mask[i]);
                modified = true;
              }
            }
          }
        }

        if (modified) {
          if (this.debug) {
            console.log("check_naked_pairs", [region, rank]);
          }
          return true;
        }
      }
    }
    return false;
  }

  get_ranks(positions, region, expected) {
    let ranks = new Int16Array(this.board.ranks(region));
    let total = 0;
    for (let i = 0; total <= expected && i < positions.length; i++) {
      let cell = this.board.into_cell(positions[i], region);
      let different = true;
      for (let k = 0; different && k < total; k++) {
        different = ranks[k] !== cell.rank;
      }
      if (different) ranks[total++] = cell.rank;
    }
    return total === expected ? ranks.slice(0, total) : null;
  }

  check_grid_reduction(region, area) {
    if (region === undefined) {
      return this.check_grid_reduction("box", "row")
          || this.check_grid_reduction("box", "column")
          || this.check_grid_reduction("row", "box")
          || this.check_grid_reduction("column", "box")
          || (this.board.type === "diagonal"
              && this.check_grid_reduction("diagonal", "box"));
    }

    for (let rank = 0; rank < this.board.ranks(region); rank++) {
      for (let i = 0, bit = 1; i < this.board.dimension; i++, bit <<= 1) {
        let list = this.board.get_positions_with_candidate(region, rank, bit);
        let ranks = this.get_ranks(list, area, 1);
        if (!ranks) continue;

        let area_rank = ranks[0];
        let modified = false;
        for (let index = 0; index < this.board.dimension; index++) {
          let pos = this.board.into_position(new Cell(area_rank, index, area));
          let cell = this.board.into_cell(pos, region);
          if (cell && (cell.rank === rank ||
              (region === "diagonal" && cell.rank === Cell.BOTH_DIAGONALS))) {
            continue;
          }
          if (this.board.candidate[pos] & bit) {
            this.board.remove_candidates(pos, bit);
            modified = true;
          }
        }

        if (modified) {
          if (this.debug) {
            console.log("check_grid_reduction", [region, rank], "into",
                [area, area_rank], "=>", into_value(bit));
          }
          return true;
        }
      }
    }
    return false;
  }

  check_hidden_pairs() {
    let positions = new Array(this.board.dimension);
    let hidden = new Int16Array(this.board.dimension);
    for (let region of this.board.regions) {
      for (let rank = 0; rank < this.board.ranks(region); rank++) {
        let total = 0;
        for (let i = 0, bit = 1; i < this.board.dimension; i++, bit <<= 1) {
          positions[i] = this.board.get_positions_with_candidate(region,
              rank, bit);
          if (positions[i].length === 2) hidden[total++] = i;
        }

        let modified = false;
        for (let i = 0; i < total - 1; i++) {
          for (let j = i + 1; j < total; j++) {
            let equal = true;
            for (let k = 0; equal && k < 2; k++) {
              equal = positions[hidden[i]][k] === positions[hidden[j]][k];
            }
            if (!equal) continue;
            let mask = (1 << hidden[i]) | (1 << hidden[j]);
            for (let k = 0; k < 2; k++) {
              let pos = positions[hidden[i]][k];
              let cand = this.board.candidate[pos];
              if (cand === mask) continue;
              this.board.remove_candidates(pos,
                  this.board.candidate[pos] & ~mask);
              modified = true;
              if (this.debug) {
                console.log("check_hidden_pairs", [region, rank], "=>",
                    hidden[i] + 1, hidden[j] + 1);
              }
            }
          }
        }
        if (modified) return true;
      }
    }
    return false;
  }

  check_pointing_pair(region) {
    for (let rank = 0; rank < this.board.ranks(region); rank++) {
      for (let i = 0, bit = 1; i < this.board.dimension; i++, bit <<= 1) {
        let positions = this.board.get_positions_with_candidate(
            region, rank, bit);
        if (positions.length !== 2) continue;
        let row = new Int16Array(2);
        let col = new Int16Array(2);
        for (let k = 0; k < 2; k++) {
          var cell = this.board.into_cell(positions[k]);
          row[k] = cell.rank;
          col[k] = cell.index;
        }
        if (row[0] === row[1] || col[0] === col[1]) continue;

        let modified = false;
        for (let k = 0; k < 2; k++) {
          let pos = this.board.into_position(new Cell(row[k], col[1 - k]));
          if (this.board.candidate[pos] & bit) {
            this.board.remove_candidates(pos, bit);
            modified = true;
          }
        }
        if (modified) {
          if (this.debug) {
            console.log("check_pointing_pair", [region, rank], "=>",
                into_value(bit));
          }
          return true;
        }
      }
    }
    return false;
  }
}


class Creator {

  static ATTEMPTS_BEFORE_NEW_PUZZLE = 30;
  static MAXIMUM_ATTEMPTS = 300;

  static create(type = "standard", difficulty = "normal", symmetry, layout) {
    let creator = new Creator();
    return creator.execute(type, difficulty, symmetry, layout);
  }

  execute(type = "standard", difficulty = "normal", symmetry, layout) {
    let level = difficulty_level(difficulty);
    this.total_attempts = 0;

    while (this.total_attempts < Creator.MAXIMUM_ATTEMPTS) {

      this.initial = new Board(type, layout);
      let solver = new Solver(this.initial);
      solver.randomized = this.initial.create_position_array();
      shuffle_array(solver.randomized);
      try {
        solver.solve(true, false);
      } catch (e) {
        if (e !== "too many guesses") throw e;
        continue;
      }
      this.initial.given = this.initial.solution.slice();
      this.initial.difficulty = "easy";
      if (!symmetry) {
        symmetry = choice_array(["rotation", "mirror", "flip", "transpose",
            "double_mirror", "double_rotation"]);
      }
      this.initial.symmetry = symmetry;

      let created = false;
      let attempts = 0;
      while (!created && attempts < Creator.ATTEMPTS_BEFORE_NEW_PUZZLE) {

        let positions = this.initial.create_position_array();
        shuffle_array(positions);
        let board = Board.copy(this.initial);
        this.puzzle = new Board(board);
        attempts++;
        this.total_attempts++;

        for (let pos of positions) {

          if (!board.given[pos]) continue;
          board.given[pos] = 0;
          for (let p of board.get_symmetric_positions(pos)) {
            board.given[p] = 0;
          }

          solver = new Solver(board);
          let solved = false;
          try {
            solved = solver.solve_and_evaluate(difficulty === "hard");
          } catch (e) {
            if (e !== "too many guesses") throw e;
          }

          if (solved && solver.has_unique_solution() &&
              (!level || difficulty_level(board.difficulty) <= level)) {
            created = !level || board.difficulty === difficulty;
            this.puzzle = Board.copy(board);  // store board
          } else {
            board = Board.copy(this.puzzle);  // restore previous
          }
        }
      }

      if (created) return this.puzzle;
    }

    throw "cannot create puzzle";
  }
}
