import { useState, useEffect, useRef } from 'react';
import { Fireworks } from '@fireworks-js/react'
import { Grid } from './Grid.jsx';
import { ButtonPanel } from './ButtonPanel.jsx';
import { TapModeInput } from './TapModeInput.jsx';
import { Board, Solver, Cell, into_bit, choice_array } from '../sudoku/sudoku.js';


function getInititalGridState(puzzle) {
  const state = [];
  for (let pos = 0; pos < puzzle.positions; pos++) {
    state.push({
      value: puzzle.given[pos] || 0,
      candidates: 0,
      editable: !puzzle.given[pos],
      error: false,
    });
  }
  return state;
}


function getInitialButtonState(puzzle) {
  const state = {};
  for (let i = 1; i <= puzzle.dimension; i++) {
    state[i] = {disabled: false, toggled: false};
  }
  for (const key of ['erase', 'pencil', 'undo', 'redo']) {
    state[key] = {disabled: /.*do$/.test(key), toggled: false};
  }
  return state;
}

export function PuzzlePage({puzzle, onSolved, showSolution = false}) {
  const [gridState, setGridState] = useState();
  const [buttonState, setButtonState] = useState();
  const [highlight, setHighlight] = useState();
  const [tapMode, setTapMode] = useState();
  const [cursor, setCursor] = useState();
  const [history, setHistory] = useState();
  const [solution, setSolution] = useState();
  const [solved, setSolved] = useState(false);
  const [showFireworks, setShowFireworks] = useState(true);

  useEffect(() => {
    let solver = new Solver(Board.copy(puzzle));
    solver.solve(true, false);
    setSolution(solver.board);
    puzzle.reset_solution();

    setGridState(getInititalGridState(puzzle));
    setButtonState(getInitialButtonState(puzzle));
    setHighlight(null);
    setCursor(null);
    setHistory({past: [], future: []});
    setSolved(false);
    setShowFireworks(true);
  }, [puzzle]);

  useEffect(() => {
    if (!buttonState) return;
    const state = {...buttonState};
    for (let i = 1; i <= puzzle.dimension; i++) {
      const count = gridState.filter(s => s.value == i).length || 0;
      state[i] = {...state[i], disabled: count >= puzzle.dimension};
    }
    state.undo = {...state.undo, disabled: history.past.length === 0};
    state.redo = {...state.redo, disabled: history.future.length === 0};
    setButtonState(state);

    let check = !!solution;
    for (let pos = 0; check && pos < puzzle.positions; pos++) {
      check = gridState[pos].value == solution.solution[pos];
    }
    if (check) {
      setCursor(null);
      setHighlight(null);
      setSolved(true);
    }
  }, [history, gridState]);

  useEffect(() => {
    if (!showSolution) return;
    let state = [...gridState];
    let reverse = [];
    let positions = [];
    for (let pos = 0; pos < puzzle.positions; pos++) {
      if (!puzzle.given[pos] && !state[pos].value) {
        changeValue(pos, solution.solution[pos], state, reverse);
        positions.push(pos);
      }
    }
    let except = 0; // for debug only
    while (except-- > 0) {
      let pos = choice_array(positions);
      changeValue(pos, 0, state, reverse);
    }
    setGridState(state);
    setShowFireworks(false);
  }, [showSolution]);

  const updateErrors = (state, value) => {
    for (let pos = 0; pos < puzzle.positions; pos++) {
      if (puzzle.solution[pos] === value) {
        state[pos] = {...state[pos], error: puzzle.invalid_solution_at(pos)};
      }
    }
  };

  const changeValue = (pos, value, state, reverse) => {
    const previous = state[pos].value;
    const candidates = state[pos].candidates;
    if (previous === value) return false;

    reverse.push(['V', pos, previous]);
    if (candidates) {
      for (let index = 1; index < puzzle.dimension; index++) {
        const bit = into_bit(index);
        if (candidates & bit) reverse.push(['C', pos, index]);
      }
    }

    state[pos] = {...state[pos], value: value, candidates: 0, error: false};
    puzzle.solution[pos] = value;
    updateErrors(state, previous);
    if (value) updateErrors(state, value);
    return true;
  };

  const changeCandidates = (pos, value, state, reverse) => {
    if (state[pos].value) return false;

    let candidates = state[pos].candidates;
    if (value === 0) {
      if (candidates === 0) return false;
      for (let index = 1; index <= puzzle.dimension; index++) {
        const bit = into_bit(index);
        if (candidates & bit) reverse.push(['C', pos, index]);
      }
      candidates = 0;
    } else {
      if (reverse) reverse.push(['C', pos, value]);
      const bit = into_bit(parseInt(value));
      if (candidates & bit) candidates &= ~bit;
      else candidates |= bit;
    }
    state[pos] = {...state[pos], candidates: candidates};
    return true;
  };

  const rememberChangeValue = (pos, value = 0) => {
    const previous = gridState[pos].value;
    if (previous === value) return false;

    let state = [...gridState];
    let reverse = [];
    if (value == 0) {
      changeCandidates(pos, 0, state, reverse);
    } else {
      let bit = into_bit(value);
      for (let region of puzzle.regions) {
        let cell = puzzle.into_cell(pos, region);
        for (let rank of cell?.get_ranks() ?? []) {
          for (let index = 0; index < puzzle.dimension; index++) {
            if (index !== cell.index) {
              let p = puzzle.into_position(new Cell(rank, index, region));
              if (state[p].candidates & bit) {
                changeCandidates(p, value, state, reverse);
              }
            }
          }
        }
      }
    }
    changeValue(pos, value, state, reverse);
    setGridState(state);
    setHistory({past: [...history.past, reverse], future: []});
    return true;
  };

  const rememberChangeCandidates = (pos, value = 0) => {
    let state = [...gridState];
    let reverse = [];
    if (!changeCandidates(pos, value, state, reverse)) return false;
    setGridState(state);
    setHistory({past: [...history.past, reverse], future: []});
    return true;
  };

  const redoActionStack = (stack) => {
    let state = [...gridState];
    let reverse = [];
    for (const [action, pos, value] of stack) {
      if (action === 'V') changeValue(pos, value, state, reverse);
      else changeCandidates(pos, value, state, reverse);
    }
    setGridState(state);
    return reverse.reverse();
  }

  const handleGridClick = (pos, value) => {
    const selectable = gridState[pos].editable;
    if (value) setHighlight(highlight == value ? null : value);
    if (selectable) setCursor(pos);
  };

  const handleButtonClick = (key) => {
    if (buttonState[key].disabled) return;
    if (key === 'pencil') {
      const state = {...buttonState};
      state.pencil = {...state.pencil, toggled: !state.pencil.toggled};
      setButtonState(state);
    } else if (/\d+/.test(key) && cursor !== null) {
      let value = parseInt(key);
      if (buttonState.pencil.toggled) rememberChangeCandidates(cursor, value);
      else rememberChangeValue(cursor, value);
    } else if (key === 'erase' && cursor !== null) {
      rememberChangeValue(cursor) || rememberChangeCandidates(cursor);
    } else if (key === 'undo') {
      let newHistory = {past: [...history.past], future: [...history.future]};
      newHistory.future.unshift(redoActionStack(newHistory.past.pop()));
      setHistory(newHistory);
    } else if (key === 'redo') {
      let newHistory = {past: [...history.past], future: [...history.future]};
      newHistory.past.push(redoActionStack(newHistory.future.shift()));
      setHistory(newHistory);
    }
  };

  const handleTapToggle = () => {
    const state = {...buttonState};
    for (let i = 1; i <= puzzle.dimension; i++) {
      state[i] = {...state[i], toggled: false};
    }
    state.erase = {...state.erase, toggled: false};
    setButtonState(state);
    setCursor(null);
    setTapMode(!tapMode);
  };

  return (
    <div className="flex flex-col items-center gap-1 h-full justify-evenly relative">
      <div className="flex flex-col gap-4 items-center">
        <div className="uppercase text-sm text-gray-500">
          <span className="">{puzzle.type}</span>
          <span className="mx-2">&diams;</span>
          <span className="">{puzzle.difficulty}</span>
        </div>
        {gridState && <Grid puzzle={puzzle} cursor={cursor} highlight={highlight} state={gridState} onClick={handleGridClick} />}
      </div>
      <div className="h-25">
        {!solved ? (
          <div className="max-w-xs flex flex-col items-center gap-5">
            {buttonState && <ButtonPanel state={buttonState} onClick={handleButtonClick} />}
            {/*<TapModeInput tapMode={tapMode} onToggle={handleTapToggle} />*/}
          </div>
        ) : null}
      </div>
      {solved && showFireworks ? (
        <Fireworks
            options={{opacity: 0.5, flickering: 0}}
            style={{top: 0, left: 0, width: '100%', height: '100%', position: 'absolute', background: 'rgb(0 0 0 / 0)'}}
          />
      ) : null}
    </div>
  );
}
