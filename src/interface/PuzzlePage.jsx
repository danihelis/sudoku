import { useState } from 'react';
import { Grid } from './Grid.jsx';
import { ButtonPanel } from './ButtonPanel.jsx';
import { TapModeInput } from './TapModeInput.jsx';
import { into_bit } from '../sudoku/sudoku.js';


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


export function PuzzlePage({puzzle}) {
  const [gridState, setGridState] = useState(getInititalGridState(puzzle));
  const [buttonState, setButtonState] = useState(getInitialButtonState(puzzle));
  const [highlight, setHighlight] = useState(null);
  const [tapMode, setTapMode] = useState(false);
  const [cursor, setCursor] = useState(null);
  const diagonal = false;

  const updateErrors = (state, value) => {
    for (let pos = 0; pos < puzzle.positions; pos++) {
      if (puzzle.solution[pos] === value) {
        state[pos] = {...state[pos], error: puzzle.invalid_solution_at(pos)};
      }
    }
  };

  const changeValue = (pos, value = 0) => {
    const previous = gridState[pos].value;
    if (previous === value) return false;

    const state = [...gridState];
    state[pos] = {...state[pos], value: value, error: false, candidates: 0};
    puzzle.solution[pos] = value;
    updateErrors(state, previous);
    if (value) updateErrors(state, value);
    setGridState(state);
    return true;
  };

  const changeCandidates = (pos, value = 0) => {
    if (gridState[pos].value) return false;
    let candidates = gridState[pos].candidates;
    if (value === 0) {
      if (candidates === 0) return false;
      candidates = 0;
    } else {
      const bit = into_bit(parseInt(value));
      if (candidates & bit) candidates &= ~bit;
      else candidates |= bit;
    }
    const state = [...gridState];
    state[pos] = {...state[pos], candidates: candidates};
    setGridState(state);
    return true;
  };

  const handleGridClick = (pos, value) => {
    const selectable = gridState[pos].editable;
    if (value) setHighlight(highlight == value ? null : value);
    if (selectable) setCursor(pos);
  };

  const handleButtonClick = (key) => {
    if (buttonState[key].disabled) return;
    if (key === "pencil") {
      const state = {...buttonState};
      state.pencil = {...state.pencil, toggled: !state.pencil.toggled};
      setButtonState(state);
    } else if (/\d+/.test(key) && cursor !== null) {
      if (buttonState.pencil.toggled) changeCandidates(cursor, key);
      else changeValue(cursor, parseInt(key));
    } else if (key === "erase" && cursor !== null) {
      changeValue(cursor) || changeCandidates(cursor);
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
    <div className="flex flex-col items-center mt-10 gap-5">
      <Grid puzzle={puzzle} cursor={cursor} highlight={highlight} state={gridState} onClick={handleGridClick} />
      <div className="max-w-xs flex flex-col items-center gap-5">
        <ButtonPanel state={buttonState} onClick={handleButtonClick} />
        <TapModeInput tapMode={tapMode} onToggle={handleTapToggle} />
      </div>
    </div>
  );
}
