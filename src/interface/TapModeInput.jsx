import { useState } from 'react';
import { Field, Label, Switch } from '@headlessui/react';


export function TapModeInput({tapMode, onToggle}) {

  return (
    <Field className="flex flex-col gap-2">
      <div className="flex gap-2">
        <Switch
          checked={tapMode}
          onChange={onToggle}
          className="group inline-flex h-6 w-11 items-center rounded-full bg-gray-200 transition data-checked:bg-blue-600 cursor-pointer"
        >
          <span className="size-4 translate-x-1 rounded-full bg-white transition group-data-checked:translate-x-6" />
        </Switch>
        <Label className="cursor-pointer">Use tap mode</Label>
      </div>
      <p className="text-xs text-gray-600 inline-block">
        To insert a number with tap mode enabled, first click the number button and then tap the grid cell.
      </p>
    </Field>
  );
}
