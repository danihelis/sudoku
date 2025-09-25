import { useState, useEffect, useRef } from 'react';
import { Creator, Board, Solver, Cell, into_bit, choice_array } from '../sudoku/sudoku.js';
import { ChevronLeftIcon, ChevronRightIcon, PlayIcon } from '@heroicons/react/24/outline';
import { PlayIcon as PlaySIcon } from '@heroicons/react/24/solid';
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
    <Button className={`${className} rounded-full p-2 text-white`} {...props} />
  );
}


function Selector({values, selected, setSelected}) {

  const change = (delta) => {
    let index = values.indexOf(selected);
    index = (index + delta + values.length) % values.length;
    setSelected(values[index]);
  };

  return (
    <div className="flex items-center justify-center gap-6 w-full">
      <RoundButton key="<" onClick={() => change(-1)}>
        <ChevronLeftIcon className="size-6" />
      </RoundButton>
      <span className="uppercase w-25 text-center text-lg">{selected}</span>
      <RoundButton key=">" onClick={() => change(1)}>
        <ChevronRightIcon className="size-6" />
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
    <div className="flex-1 flex flex-col items-center justify-evenly h-full">
      <div className="flex items-center">
        <p className="uppercase text-blue-800 text-2xl font-semibold">New Puzzle</p>
      </div>
      <div className="flex flex-col gap-10 items-center justify-center">
        <Selector key="difficulties" values={difficulties} selected={difficulty} setSelected={setDifficulty} />
        <Selector key="types" values={types} selected={type} setSelected={setType} />
        <img src={urls[type]} width="100" />
      </div>
      <div className="flex items-center">
        <Button className="p-3 px-6 rounded flex gap-2 text-black items-center" onClick={create}>
          <PlayIcon className="size-6" />
          <span className="text-xl uppercase font-light">Play</span>
        </Button>
      </div>
    </div>
  );
}
