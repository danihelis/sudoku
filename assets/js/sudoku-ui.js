"use strict";

class Screen {

  constructor() {
    let search = new URLSearchParams(window.location.search);
    if (search.has("p")) this.load(search.get("p"));
    else this.configure();
  }

  configure() {
    let self = this;
    $(".buttons .play").on("click", () => self.play());

    this.options = {
      type: ["standard"],
      difficulty: ["easy", "normal", "hard"],
    };
    this.choices = {
      type: "standard",
      difficulty: "normal",
    };

    for (let key in this.options) {
      let option = $("#option-template").clone();
      option.attr("id", `option-${key}`);
      $(".options").append(option);

      function change(delta) {
        let options = self.options[key];
        let index = options.indexOf(self.choices[key]);
        index = (index + delta + options.length) % options.length;
        self.choices[key] = options[index];
        $(`#option-${key} span.value`).text(options[index]);
      }

      if (this.options[key].length === 1) {
        option.find(".selector").addClass("disabled");
      } else {
        option.find(".selector-left").on("click", () => change(-1));
        option.find(".selector-right").on("click", () => change(1));
      }
      option.toggle(true);
      change(0);
    }

    $(".loading").toggle(false);
    $(".buttons, .options").toggle(true);
  }

  load(sequence) {
    $(".buttons, .options, .loading").toggle(false);
    let puzzle = Board.import(sequence);
    let solver = new Solver(puzzle);
    if (!solver.solve_and_evaluate(true)) throw "invalid puzzle";

    let sudoku = new Sudoku(puzzle);
  }

  play() {
    let self = this;
    $(".buttons, .options").toggle(false);
    $(".loading").toggle(true);

    function create() {
      let puzzle = Creator.create(self.choices.type, self.choices.difficulty);
      console.log(puzzle);
      let query = new URLSearchParams();
      query.append("p", puzzle.export());
      window.location.replace(`?${query}`);
    }

    setTimeout(create, 0);
    console.log("click");
  }
}

class Sudoku {

  constructor(puzzle) {
    this.puzzle = puzzle;
    this.board = Board.copy(this.puzzle);
    this.board.reset_solution();
    this.board.candidate.fill(0);

    console.log(this.puzzle.type, this.puzzle.difficulty, this.puzzle.symmetry);

    this.selected = null;
    this.guess_mode = false;
    this.history = [];
    this.future = [];
    this.highlight = null;
    this.solved = false;
    this.start_time = Date.now();

    this.create_grid();
    this.create_toolbar();
    // this.fill_solution_but(2);

    $(".header .difficulty").text(this.puzzle.difficulty);
  }

  create_grid() {
    $(".grid").empty();

    for (let pos = 0; pos < this.puzzle.positions; pos++) {
      let row, col;
      [row, col] = this.puzzle.into_cell(pos).into_array();

      let cell = $(`<div class="cell" data-pos="${pos}" id="cell-${pos}"></div>`);
      if (this.puzzle.given[pos]) {
        cell.append($(`<span class="given">${this.puzzle.given[pos]}</span>`));
      }
      $(".grid").append(cell);

      let self = this;
      cell.on("click", () => self.grid_clicked(pos, cell));

      if (col == this.puzzle.dimension - 1) cell.addClass("right-border");
      if (row === this.puzzle.dimension - 1) cell.addClass("bottom-border");

      let above = this.puzzle.into_position(new Cell(row - 1, col));
      if (row === 0 || this.puzzle.layout.cell[pos].rank
          !== this.puzzle.layout.cell[above].rank) {
        cell.addClass("top-border");
      }

      let side = this.puzzle.into_position(new Cell(row, col - 1));
      if (col === 0 || this.puzzle.layout.cell[pos].rank
          !== this.puzzle.layout.cell[side].rank) {
        cell.addClass("left-border");
      }
    }

    $(".board").toggle(true);
  }

  grid_clicked(pos, cell) {
    if (this.solved) return;
    this.selected = pos;
    $(".selected").removeClass("selected");
    cell.addClass("selected");

    let value = this.board.given[pos] || this.board.solution[pos];
    if (value && value === this.highlight) {
      this.highlight = null;
      $(".highlight").removeClass("highlight");
      $(`.guess-${value}`).not(":empty").removeClass("highlight");

    } else if (value) {
      $(".highlight").removeClass("highlight");
      $(`.guess-${value}`).not(":empty").removeClass("highlight");

      this.highlight = value;
      for (let p = 0; p < this.board.positions; p++) {
        if (this.board.given[p] == value || this.board.solution[p] == value) {
          $(`#cell-${p}`).addClass("highlight");
        }
      }
      $(`.guess-${value}`).not(":empty").addClass("highlight");
    }
  }

  create_toolbar() {
    let self = this;
    $(".toolbar").empty();

    let buttons = [];
    for (let i = 1; i <= this.puzzle.dimension; i++) {
      buttons.push({id: i});
    }
    buttons.push({id: 0, icon: "fa-solid fa-xmark"});
    buttons.push({id: "guess", icon: "fa-solid fa-sm fa-pencil"});
    buttons.push({id: "undo", icon: "fa-solid fa-sm fa-rotate-left"});
    buttons.push({id: "redo", icon: "fa-solid fa-sm fa-rotate-right"});

    for (let data of buttons) {
      let button = $(`<div class="button"></div>`);
      button.attr("id", `btn-${data.id}`);
      button.css("grid-area", `btn-${data.id}`);
      if (!data.icon) button.append($(`<span>${data.id}</span>`));
      else button.append($(`<i class="${data.icon}"></i>`));
      $(".toolbar").append(button);
      button.on("click", () => self.button_clicked(data.id, button));
    }

    this.update_toolbar_history();
    $(".toolbar").toggle(true);
  }

  fill_solution_but(number) {
    let positions = [];
    for (let pos = 0; pos < this.board.positions; pos++) {
      if (!this.board.given[pos]) {
        this.set_solution(pos, this.puzzle.solution[pos]);
        positions.push(pos);
      }
    }
    number = number ?? 0;
    while (number-- > 0) {
      let pos = choice_array(positions);
      this.set_solution(pos, 0);
    }
  }

  update_toolbar_history() {
    $("#btn-undo").toggleClass("disabled", this.history.length === 0);
    $("#btn-redo").toggleClass("disabled", this.future.length === 0);
  }

  check_puzzle_solved() {
    for (let pos = 0; pos < this.board.positions; pos++) {
      if (this.board.solution[pos] !== this.puzzle.solution[pos]) return;
    }
    this.selected = null;
    $(".selected").removeClass("selected");
    $(".highlight").removeClass("highlight");
    $(".toolbar").empty().toggle(false);
    $(".grid").css("cursor", "default");

    this.solved = true;
    this.time = div((Date.now() - this.start_time), 1000);
    let minutes = div(this.time, 60);
    let seconds = this.time % 60;
    $(".time .value").text(`${minutes}' ${seconds}"`);
    $(".time").toggle(true);

    let self = this;
    let interval = null;
    let position = 0;
    let direction = 1;

    function animate_solve() {
      $(".highlight-complete").removeClass("highlight-complete");
      if (position >= self.board.positions) {
        clearInterval(interval);
        return;
      }
      let cell = self.board.into_cell(position);
      while (cell.index >= 0 && cell.rank < self.board.dimension) {
        let p = self.board.into_position(cell);
        $(`#cell-${p}`).addClass("highlight-complete");
        cell.rank++;
        cell.index--;
      }
      position += direction;
      if (position % self.board.dimension === 0) {
        direction = self.board.dimension;
        position = direction - 1;
      }
    }

    interval = setInterval(animate_solve, 30);
  }

  button_clicked(value, button) {
    if (button.hasClass("disabled")) return;

    if (value === "undo" || value === "redo") {
      this.revert_actions(value);

    } else if (value == "guess") {
      this.guess_mode = !this.guess_mode;
      button.toggleClass("toggled", this.guess_mode);
      $(".button > span").toggleClass("guess", this.guess_mode);

    } else if (typeof value === "number" && this.selected !== null &&
        !this.board.given[this.selected]) {
      if (value === 0 || !this.guess_mode) {
        this.input_solution(this.selected, value);
      } else if (!this.board.solution[this.selected]) {
        this.input_guess(this.selected, value);
      }
    }
  }

  validate_solution_value(value) {
    let any_invalid = false;
    let count = 0;
    for (let pos = 0; pos < this.board.positions; pos++) {
      if (this.board.solution[pos] === value) {
        let invalid = this.board.invalid_solution_at(pos);
        $(`#cell-${pos}`).find("span").toggleClass("error", invalid);
        count++;
        any_invalid |= invalid;
      }
    }
    $(`#btn-${value}`).toggleClass("disabled",
        !any_invalid && count >= this.board.dimension);
  }

  set_solution(pos, value) {
    let cell = $(`#cell-${pos}`);
    cell.removeClass("error highlight");
    cell.empty();
    if (value >= 1 && value <= this.puzzle.dimension) {
      cell.append($(`<span class="answer">${value}</span>`));
    }

    let previous = this.board.solution[pos];
    let reverse = [];

    if (this.board.candidate[pos]) {
      for (let p = 1; p <= this.board.dimension; p++) {
        if (this.board.candidate[pos] & into_bit(p)) {
          reverse.push({type: "guess", pos: pos, value: p});
        }
      }
      this.board.candidate[pos] = 0;
    }

    if (value !== previous) {
      this.board.solution[pos] = value;
      if (previous) this.validate_solution_value(previous);
      if (value) {
        this.validate_solution_value(value);
        if (value === this.highlight) cell.addClass("highlight");
      }
      reverse.unshift({type: "solution", pos: pos, value: previous});
    }

    return reverse;
  }

  input_solution(pos, value) {
    let reverse = this.set_solution(pos, value);
    if (!reverse.length) return;

    if (value) {
      for (let region of this.board.regions) {
        let cell = this.board.into_cell(pos, region);
        for (let rank of cell.get_ranks() ?? []) {
          for (let index = 0; index < this.board.dimension; index++) {
            if (index !== cell.index) {
              let p = this.board.into_position(new Cell(rank, index, region));
              if (this.board.candidate[p] & into_bit(value)) {
                reverse.push(...this.set_guess(p, value));
              }
            }
          }
        }
      }
    }

    this.history.push(reverse);
    this.future = [];
    this.update_toolbar_history();
    this.check_puzzle_solved();
  }

  set_guess(pos, value) {
    if (!$(`#cell-${pos} .guess-${value}`).length) {
      let guess = $(`<div class="guess"></div>`);
      for (let j = 1; j <= 9; j++) {
        guess.append($(`<span class="guess-${j}"></span>`));
      }
      $(`#cell-${pos}`).empty().append(guess);
    }

    let cell = $(`#cell-${pos} .guess-${value}`);
    let bit = into_bit(value);
    if (this.board.candidate[pos] & into_bit(value)) {
      cell.empty().removeClass("highlight");
      this.board.candidate[pos] &= ~bit;
    } else {
      cell.text(value);
      this.board.candidate[pos] |= bit;
      if (value === this.highlight) cell.addClass("highlight");
    }

    return [{type: "guess", pos: pos, value: value}];
  }

  input_guess(pos, value) {
    let reverse = this.set_guess(pos, value);
    this.history.push(reverse);
    this.future = [];
    this.update_toolbar_history();
  }

  execute_actions(actions) {
    let reverse = [];
    for (let action of actions) {
      if (action.type === "solution") {
        reverse.push(...this.set_solution(action.pos, action.value));
      } else if (action.type === "guess") {
        reverse.push(...this.set_guess(action.pos, action.value));
      }
    }
    return reverse.reverse();
  }

  revert_actions(option) {
    if (option === "undo" && this.history.length) {
      let reverse = this.execute_actions(this.history.pop());
      this.future.unshift(reverse)
    }
    if (option === "redo" && this.future.length) {
      let reverse = this.execute_actions(this.future.shift());
      this.history.push(reverse);
    }
    this.update_toolbar_history();
  }

}

$(() => new Screen());
