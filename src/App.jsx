import { useState, useEffect } from 'react';
import { ExclamationTriangleIcon } from '@heroicons/react/24/outline';
import { Transition } from '@headlessui/react';
import { PuzzlePage } from './interface/PuzzlePage.jsx';
import { NewPuzzlePage } from './interface/NewPuzzlePage.jsx';
import { NavBar } from './interface/NavBar.jsx';
import { Board, Solver } from './sudoku/sudoku.js';


export default function App() {
  const [puzzle, setPuzzle] = useState();
  const [error, setError] = useState(false)
  const [showSolution, setShowSolution] = useState(false);

  useEffect(() => {
    const handlePopState = (event) => {
      const query = new URLSearchParams(window.location.search);
      const data = query.get('p');
      if (data) {
        try {
          let board = Board.import(data);
          let solver = new Solver(board);
          let solved = solver.solve_and_evaluate(true);
          if (solved) {
            resetPuzzle(board);
            return;
          }
        } catch (e) {
          console.log(e);
        }
        setError(true);
        setTimeout(() => setError(false), 3000);
      }
      resetPuzzle(null);
    };
    window.addEventListener('popstate', handlePopState);
    handlePopState();
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const resetPuzzle = (puzzle) => {
    setShowSolution(false);
    setPuzzle(puzzle);
  };

  const handleCreatePuzzle = (puzzle) => {
    let url = `?p=${puzzle.export()}`;
    window.history.pushState({}, '', url);
    resetPuzzle(puzzle);
  };

  const handleMenuClick = (action) => {
    switch (action) {
      case 'create':
        window.history.pushState({}, '', '?');
        resetPuzzle(null);
        break;
      case 'restart':
        resetPuzzle(Board.copy(puzzle));
        break;
      case 'solve':
        setShowSolution(true);
        break;
    }
  };

  return (
    <div className="flex flex-col h-dvh items-stretch">
      <NavBar hasPuzzle={!!puzzle} onMenuClick={handleMenuClick} />
      <div className="flex-1 relative">
        {!puzzle && (
          <Transition show={error}>
            <div className="absolute top-2 right-2 p-2 px-4 bg-red-200 text-red-800 text-sm flex gap-2 items-center justify-center transition data-closed:opacity-0 duration-500">
              <ExclamationTriangleIcon className="size-4" />
              <span>The puzzle URL is invalid</span>
            </div>
          </Transition>
        )}
        {puzzle ? (
          <PuzzlePage puzzle={puzzle} showSolution={showSolution} />
        ) : (
          <NewPuzzlePage onCreatePuzzle={handleCreatePuzzle} />
        )}
      </div>
    </div>
  );
}
