import { useState } from 'react';
import { PuzzlePage } from './interface/PuzzlePage.jsx';
import { Creator } from './sudoku/sudoku.js';

export default function App() {
  const puzzle = Creator.create();

  return (
    <PuzzlePage puzzle={puzzle} />
  );
}
