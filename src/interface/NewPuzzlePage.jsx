import { useState, useEffect, useRef } from 'react';
import { ChevronLeftIcon, ChevronRightIcon, PlayIcon } from '@heroicons/react/24/outline';
import { Creator, Board, Solver, Cell, into_bit, choice_array } from '../sudoku/sudoku.js';

import standardUrl from '../assets/standard.svg';
import diagonalUrl from '../assets/diagonal.svg';
import irregularUrl from '../assets/irregular.svg';

const types = ['standard', 'diagonal', 'irregular'];
const difficulties = ['easy', 'normal', 'hard'];
const urls = {
  standard: standardUrl,
  diagonal: diagonalUrl,
  irregular: irregularUrl,
};


function Button({children, onClick, className}) {
  const colors = 'bg-blue-300 xhover:bg-blue-200 active:bg-blue-400';
  return (
    <button className={`${colors} ${className}`} onClick={onClick}>
      {children}
    </button>
  );
}


function RoundButton({className, ...props}) {
  return (
    <Button className={`${className} p-2 `} {...props} />
  );
}


function Selector({values, selected, setSelected}) {
  const iconSize = "size-4";

  const change = (delta) => {
    let index = values.indexOf(selected);
    index = (index + delta + values.length) % values.length;
    setSelected(values[index]);
  };

  return (
    <div className="flex items-center justify-center gap-6 border-1 border-blue-300 rounded-xl p-1 w-full">
      <RoundButton key="<" className="rounded-l-lg" onClick={() => change(-1)}>
        <ChevronLeftIcon className={iconSize} />
      </RoundButton>
      <span className="flex-1 uppercase text-center text-slate-600">{selected}</span>
      <RoundButton key=">" className="rounded-r-lg" onClick={() => change(1)}>
        <ChevronRightIcon className={iconSize} />
      </RoundButton>
    </div>
  );
}


export function NewPuzzlePage({onCreatePuzzle}) {
  const [difficulty, setDifficulty] = useState(difficulties[1]);
  const [type, setType] = useState(types[0]);
  const [creating, setCreating] = useState(false);

  const create = () => {
    setTimeout(() => {
      let puzzle = null;
      while (!puzzle) {
        try {
          puzzle = Creator.create(type, difficulty);
        } catch {}
      }
      onCreatePuzzle(puzzle);
    }, 0);
    setCreating(true);
  };

  return creating ? (
    <div className="flex items-center justify-center h-full">
      <span className="text-gray-500">Creating puzzle...</span>
    </div>
  ) : (
    <div className="flex flex-col items-center justify-evenly h-full">
      <div className="text-4xl text-black uppercase font-bold">
        Sudoku
      </div>
      <img src={urls[type]} width="100" />
      <div className="flex flex-col gap-8 items-center justify-center w-2/3">
        <Selector key="types" values={types} selected={type} setSelected={setType} />
        <Selector key="difficulties" values={difficulties} selected={difficulty} setSelected={setDifficulty} />
      </div>
      <div className="flex items-center">
        <Button className="p-3 px-6 rounded-lg flex gap-2 text-black items-center" onClick={create}>
          <PlayIcon className="size-6" />
          <span className="text-xl uppercase font-light">Play</span>
        </Button>
      </div>
    </div>
  );
}
