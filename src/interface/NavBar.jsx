import { useState } from 'react';
import { PlusIcon, ArrowPathIcon, WrenchIcon, BeakerIcon, Cog6ToothIcon, EyeIcon } from '@heroicons/react/24/outline';
import { Menu, MenuButton, MenuItem, MenuItems } from '@headlessui/react';
import gridUrl from '../assets/grid.svg';


export function NavBar({hasPuzzle, onMenuClick}) {
  const menu = [
    ['New puzzle', 'create', <PlusIcon className="size-4" />],
    ['Restart puzzle', 'restart', <ArrowPathIcon className="size-4" />],
    ['Show solution', 'solve', <EyeIcon className="size-4" />],
    // ['Configure', 'configure', <WrenchIcon className="size-4" />],
  ];

  return (
    <div className="bg-black text-white w-full p-2 px-4 flex items-center sticky top-0 h-15">
      <img src={gridUrl} className="stroke-white" width="25" height="25" />
      <span className="font-bold ml-2 uppercase">Sudoku</span>
      <span className="flex-1" />
      {true && (
        <div className="flow-auto text-right">
          <Menu>
            <MenuButton className="cursor-pointer bg-gray-600 rounded-md p-2 px-3">
              ☰
            </MenuButton>
            <MenuItems transition anchor="bottom end" className="bg-gray-600 p-2 rounded-sm flex flex-col mt-1 transition duration-100 ease-out data-closed:scale-95 data-closed:opacity-0">
              {menu.map(([label, action, icon]) => (
                <MenuItem key={label}>
                  <button
                    className="text-left text-white cursor-pointer rounded-sm hover:bg-gray-400 hover:text-black p-1 px-2 flex items-center gap-2"
                    onClick={() => onMenuClick(action)}
                  >
                    {icon}
                    <span>{label}</span>
                  </button>
                </MenuItem>
              ))}
            </MenuItems>
          </Menu>
        </div>
      )}
    </div>
  );
}
