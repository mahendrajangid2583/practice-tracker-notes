import React, { forwardRef, useEffect, useImperativeHandle, useState } from 'react';
import { Code, Folder, FileText, HelpCircle } from 'lucide-react';
import clsx from 'clsx';

const MentionList = forwardRef((props, ref) => {
  const [selectedIndex, setSelectedIndex] = useState(0);

  const selectItem = (index) => {
    const item = props.items[index];
    if (item) {
      props.command({ 
        id: item.id, 
        label: item.value, 
        link: item.link, 
        type: item.type,
        subtype: item.subtype 
      });
    }
  };

  const upHandler = () => {
    setSelectedIndex((selectedIndex + props.items.length - 1) % props.items.length);
  };

  const downHandler = () => {
    setSelectedIndex((selectedIndex + 1) % props.items.length);
  };

  const enterHandler = () => {
    selectItem(selectedIndex);
  };

  useEffect(() => setSelectedIndex(0), [props.items]);

  useImperativeHandle(ref, () => ({
    onKeyDown: ({ event }) => {
      if (event.key === 'ArrowUp') {
        upHandler();
        return true;
      }
      if (event.key === 'ArrowDown') {
        downHandler();
        return true;
      }
      if (event.key === 'Enter') {
        enterHandler();
        return true;
      }
      return false;
    },
  }));

  const getIconConfig = (item) => {
    if (item.type === 'COLLECTION') {
        return { icon: Folder, colorClass: "bg-blue-500/10 text-blue-400" };
    }
    if (item.subtype === 'QUESTION') {
        return { icon: HelpCircle, colorClass: "bg-purple-500/10 text-purple-400" };
    }
    return { icon: FileText, colorClass: "bg-amber-500/10 text-amber-400" };
  };

  return (
    <div className="bg-neutral-900 border border-amber-500/30 rounded-lg shadow-xl overflow-hidden min-w-[200px] flex flex-col p-1 z-[99999]">
      {props.items.length ? (
        props.items.map((item, index) => {
            const { icon: Icon, colorClass } = getIconConfig(item);
            return (
          <button
            className={clsx(
              'px-3 py-2 text-left text-sm flex items-center gap-3 rounded-md transition-colors',
              index === selectedIndex ? 'bg-amber-900/30 text-amber-400 border border-amber-500/20' : 'text-slate-300 hover:bg-neutral-800'
            )}
            key={index}
            onClick={() => selectItem(index)}
          >
            <div className={clsx("p-1 rounded", colorClass)}>
                <Icon size={14} />
            </div>
            <span className="truncate">{item.value}</span>
          </button>
            );
        })
      ) : (
        <div className="px-3 py-2 text-slate-500 text-sm italic">No results</div>
      )}
    </div>
  );
});

export default MentionList;


