import { useState, memo } from 'react';
import { Cell, into_bit } from '../sudoku/sudoku.js';


const GridCell = memo(({puzzle, pos, selected, highlight, state, onClick}) => {

  const [row, column] = puzzle.into_cell(pos).into_array();

  const rank = (rd = 0, cd = 0) => {
    const p = puzzle.into_position(new Cell(row + rd, column + cd));
    return puzzle.layout.cell[p].rank;
  };

  const bt = row === 0 || rank() !== rank(-1, 0) ? 'border-t-3' : 'border-t-1';
  const bl = column === 0 || rank() !== rank(0, -1) ? 'border-l-3' : 'border-l-1';
  const br = column === puzzle.dimension - 1 ? 'border-r-3' : '';
  const bb = row === puzzle.dimension - 1 ? 'border-b-3' : '';

  const color = state.error ? 'text-red-600' :
      state.editable ? 'text-blue-600' : 'text-black';

  const renderCandidates = () => {
    if (!state.candidates) return null;
    const candidates = [];
    for (let value = 1; value <= puzzle.dimension; value++) {
      const contains = state.candidates & into_bit(value);
      candidates.push(
        <div key={value} className={`${contains && highlight === value ? 'bg-blue-100' : ''} flex items-center justify-center`}>
          <p className="p-0 text-[8px] select-none">{contains ? value : ''}</p>
        </div>
      );
    }
    return (
      <div className="grid grid-cols-3 grid-rows-3 h-full w-full">
        {candidates}
      </div>
    );
  };

  return (
    <div
      className={`${bt} ${bl} ${br} ${bb} ${selected ? 'inset-ring-2 inset-ring-blue-300' : ''} ${highlight === state.value ? 'bg-blue-100' : ''} h-[40px] border-slate-400 cursor-pointer`}
      onClick={() => onClick(pos, state.value)}
    >
      {state.value ? (
        <div className="flex items-center justify-center h-full">
          <p className={`${color} text-2xl font-serif select-none`}>{state.value}</p>
        </div>
      ) : renderCandidates()}
    </div>
  );
});


export function Grid({puzzle, cursor, highlight, state, onClick}) {
  const cells = [];

  for (const [pos, cellState] of state.entries() ) {
    cells.push(
      <GridCell
        key={pos}
        pos={pos}
        puzzle={puzzle}
        selected={cursor === pos}
        highlight={highlight}
        state={cellState}
        onClick={onClick}
      />
    );
  }

  return (
    <div className="relative">
      {puzzle.type === 'diagonal' ? (
        <svg className="absolute top-0 left-0 w-full h-full stroke-slate-400 -z-1" width="360" height="360">
          <line x1="0" y1="0" x2="360" y2="360" strokeWidth="1" strokeDasharray="5,5" />
          <line x1="360" y1="0" x2="0" y2="360" strokeWidth="1" strokeDasharray="5,5" />
        </svg>
      ) : null}
      <div className="grid grid-cols-[repeat(9,40px)]">
        {cells}
      </div>
    </div>
  );
}
