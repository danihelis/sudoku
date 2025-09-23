import { useState, memo } from 'react';
import { Field, Label, Switch } from '@headlessui/react';
import { PencilIcon, XMarkIcon, ArrowUturnLeftIcon, ArrowUturnRightIcon } from '@heroicons/react/24/outline';


const Button = memo(({value, children, className, toggled, onClick, disabled}) => {

  const colors = disabled ? 'bg-gray-200 text-gray-400'
    : toggled ? 'bg-blue-600 xhover:bg-blue-500 active:bg-blue-700 text-white'
    : 'bg-blue-300 xhover:bg-blue-200 active:bg-blue-400 text-black';

  return (
    <button
      className={`${className} ${colors} ${disabled || 'cursor-pointer'} rounded-lg px-2 py-2 text-2xl flex items-center justify-center`}
      onClick={() => onClick(value)}
      disabled={disabled}
    >
      {children}
    </button>
  );
});


const NumberButton = memo(({number, pencilMode, ...props}) => {
  const column = (number - 1) % 3;
  const row = Math.floor((number - 1) / 3);
  let positioning = `
    ${row === 0 ? 'items-start' : row == 1 ? 'items-center' : 'items-end'}
    ${column === 0 ? 'justify-start' : column === 1 ?
        'justify-center' : 'justify-end'}`;

  return (
    <Button className="h-12" value={number} {...props}>
      {pencilMode ? (
        <div className={`w-full h-full flex ${positioning}`}>
          <span className="text-sm">{number}</span>
        </div>
      ) : <span className="font-light">{number}</span>}
    </Button>
  );
});


export function ButtonPanel({state, onClick}) {

  const numericKeys = Object.keys(state)
    .filter(k => /\d+/.test(k))
    .map(k => parseInt(k));
  numericKeys.sort((a, b) => a - b);

  const numericButtons = [];
  for (const number of numericKeys) {
    numericButtons.push(
      <NumberButton
        key={number}
        number={number}
        pencilMode={state.pencil.toggled}
        disabled={state[number].disabled}
        toggled={state[number].toggled}
        onClick={onClick}
      />
    );
  }

  return (
    <div className="grid grid-cols-7 gap-1 w-full">
      {numericButtons.slice(0, 5)}
      <Button key="pencil" value="pencil" className="row-span-2" toggled={state.pencil.toggled} onClick={onClick}>
        <span><PencilIcon className="size-6" /></span>
      </Button>
      <Button key="undo" value="undo" disabled={state.undo.disabled} onClick={onClick}>
        <ArrowUturnLeftIcon className="size-5" />
      </Button>
      {numericButtons.slice(5)}
      <Button key="erase" value="erase" toggled={state.erase.toggled} onClick={onClick}>
        <XMarkIcon className="size-6 text-center" />
      </Button>
      <Button key="redo" value="redo" disabled={state.redo.disabled} onClick={onClick}>
        <ArrowUturnRightIcon className="size-5" />
      </Button>
    </div>
  );
}
