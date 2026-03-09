import React, { useState, useEffect } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Mention from '@tiptap/extension-mention';
import Link from '@tiptap/extension-link';
import Underline from '@tiptap/extension-underline';
import Placeholder from '@tiptap/extension-placeholder';
import { Color } from '@tiptap/extension-color';
import { TextStyle } from '@tiptap/extension-text-style';
import { ReactRenderer } from '@tiptap/react';
import tippy from 'tippy.js';
import 'tippy.js/dist/tippy.css';
import { X, Save, Check, StickyNote, Bold, Italic, Underline as UnderlineIcon, Strikethrough, List, ListOrdered, Link as LinkIcon, Palette, Image as ImageIcon } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { searchOmni } from '../services/api';
import MentionList from './Editor/MentionList';
import Image from '@tiptap/extension-image';
import clsx from 'clsx';

const NoteModal = ({ isOpen, onClose, onSave, initialValue = '' }) => {
  const [status, setStatus] = useState('idle'); // 'idle', 'saving', 'saved'
  const [, forceUpdate] = useState(0); // Force re-render for TipTap state updates

  const editor = useEditor({
    extensions: [
      StarterKit,
      Image.configure({
        inline: false,
        allowBase64: true,
        HTMLAttributes: {
          class: 'max-w-full h-auto rounded-lg',
        },
      }),
      Underline,
      TextStyle,
      Color,
      Link.configure({
        openOnClick: false,
        HTMLAttributes: {
          class: 'text-amber-400 underline decoration-amber-500/30 cursor-pointer hover:text-amber-300',
          target: '_blank',
        },
      }),
      Placeholder.configure({
        placeholder: 'Write your tactical brief...',
        emptyEditorClass: 'is-editor-empty before:content-[attr(data-placeholder)] before:text-neutral-600 before:float-left before:pointer-events-none h-0',
      }),
      Mention.configure({
        HTMLAttributes: {
          class: 'mention text-amber-400 font-bold bg-amber-900/20 px-1 rounded decoration-clone cursor-pointer hover:underline',
        },
        renderHTML({ node, htmlAttributes }) {
          return [
            'a',
            {
                ...htmlAttributes,
                href: node.attrs.link,
                target: '_blank',
                rel: 'noopener noreferrer',
            },
            `@${node.attrs.label ?? node.attrs.id}`,
          ]
        },
        renderText({ node }) {
            return `@${node.attrs.label ?? node.attrs.id}`
        },
        command: ({ editor, range, props }) => {
            const nodeAfter = editor.view.state.selection.$to.nodeAfter
            const override = nodeAfter?.text?.startsWith(' ') ? { to: range.to + 1 } : {}
      
            editor
              .chain()
              .focus()
              .insertContentAt(
                { ...range, ...override },
                [
                  {
                    type: 'mention',
                    attrs: props,
                  },
                  {
                    type: 'text',
                    text: ' ',
                  },
                ],
              )
              .run()
      
            window.getSelection()?.collapseToEnd()
        },
        suggestion: {
          items: async ({ query }) => {
            try {
                return await searchOmni(query);
            } catch (error) {
                console.error("Mention API Error:", error);
                return [];
            }
          },
          render: () => {
            let component;
            let popup;

            return {
              onStart: props => {
                component = new ReactRenderer(MentionList, {
                  props,
                  editor: props.editor,
                });

                if (!props.clientRect) {
                  return;
                }

                popup = tippy('body', {
                  getReferenceClientRect: props.clientRect,
                  appendTo: () => document.body,
                  content: component.element,
                  showOnCreate: true,
                  interactive: true,
                  trigger: 'manual',
                  placement: 'bottom-start',
                });
              },
              onUpdate(props) {
                component.updateProps(props);

                if (!props.clientRect) {
                  return;
                }

                popup[0].setProps({
                  getReferenceClientRect: props.clientRect,
                });
              },
              onKeyDown(props) {
                if (props.event.key === 'Escape') {
                  popup[0].hide();
                  return true;
                }
                return component.ref?.onKeyDown(props);
              },
              onExit() {
                popup[0].destroy();
                component.destroy();
              },
            };
          },
        },
    }).extend({
        addAttributes() {
            return {
                id: {
                    default: null,
                    parseHTML: element => element.getAttribute('data-id'),
                    renderHTML: attributes => {
                        if (!attributes.id) {
                            return {}
                        }
                        return {
                            'data-id': attributes.id,
                        }
                    },
                },
                label: {
                    default: null,
                    parseHTML: element => element.getAttribute('data-label'),
                    renderHTML: attributes => {
                        if (!attributes.label) {
                            return {}
                        }
                        return {
                            'data-label': attributes.label,
                        }
                    },
                },
                link: {
                    default: null,
                    parseHTML: element => element.getAttribute('href'),
                    renderHTML: attributes => {
                        if (!attributes.link) {
                            return {}
                        }
                        return {
                            'href': attributes.link,
                        }
                    },
                },
                type: {
                    default: null,
                    parseHTML: element => element.getAttribute('data-type'),
                    renderHTML: attributes => {
                        if (!attributes.type) {
                            return {}
                        }
                        return {
                            'data-type': attributes.type,
                        }
                    },
                },
            }
        }
    }),
    ],
    content: initialValue,
    editorProps: {
        attributes: {
            class: 'max-w-none focus:outline-none min-h-[300px] text-slate-300 p-4 bg-neutral-950 rounded-b-lg'
        }
    },
    onTransaction: () => {
        forceUpdate(n => n + 1);
    },
  });

  // Custom Key Handler for Tab
  useEffect(() => {
    if (!editor) return;

    const handleKeyDown = (e) => {
      if (e.key === 'Tab') {
        e.preventDefault();
        editor.commands.insertContent('    '); // Insert 4 spaces
      }
    };

    const dom = editor.view.dom;
    dom.addEventListener('keydown', handleKeyDown);

    return () => {
      dom.removeEventListener('keydown', handleKeyDown);
    };
  }, [editor]);

  // Update content when initialValue changes
  useEffect(() => {
    if (editor && isOpen) {
        editor.commands.setContent(initialValue || '');
        setStatus('idle');
    }
  }, [initialValue, isOpen, editor]);

  const handleSave = async () => {
    if (!editor) return;
    setStatus('saving');
    const html = editor.getHTML();
    await onSave(html);
    setStatus('saved');
    setTimeout(() => onClose(), 800);
  };

  const setLink = () => {
    const previousUrl = editor.getAttributes('link').href;
    const url = window.prompt('URL', previousUrl);

    // cancelled
    if (url === null) {
      return;
    }

    // empty
    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
      return;
    }

    // update
    editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
  };

  const addImage = () => {
    const url = window.prompt('Enter image URL:');
    if (url) {
      editor.chain().focus().setImage({ src: url }).run();
    }
  };

  const ToolbarButton = ({ onClick, isActive, icon: Icon, label }) => (
    <button
      onClick={onClick}
      onMouseDown={(e) => e.preventDefault()}
      title={label}
      className={clsx(
        "p-2 rounded transition-all duration-200",
        isActive 
          ? "bg-amber-500 text-neutral-950 shadow-[0_0_10px_rgba(245,158,11,0.3)]" 
          : "text-neutral-400 hover:bg-neutral-800 hover:text-neutral-200"
      )}
    >
      <Icon size={18} strokeWidth={isActive ? 2.5 : 2} />
    </button>
  );

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
          <motion.div 
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={onClose} className="absolute inset-0 bg-black/80 backdrop-blur-sm" 
          />
          
          <motion.div 
            initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }}
            className="relative w-full max-w-2xl bg-neutral-950 border border-neutral-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]"
          >
            {/* Header */}
            <div className="flex justify-between items-center p-4 border-b border-neutral-800 bg-neutral-900/50">
              <h3 className="text-xl font-serif text-neutral-100 flex items-center gap-2">
                <StickyNote className="text-amber-500" size={24} /> Tactical Notes
              </h3>
              <button onClick={onClose} className="p-2 hover:bg-neutral-800 rounded-full text-neutral-400 hover:text-white transition-colors">
                <X size={20} />
              </button>
            </div>

            {/* Editor Toolbar */}
            <div className="bg-neutral-900 border-b border-neutral-800 p-2 flex gap-1 overflow-x-auto items-center">
                {editor && (
                    <>
                        <ToolbarButton 
                            onClick={() => editor.chain().focus().toggleBold().run()} 
                            isActive={editor.isActive('bold')} 
                            icon={Bold} 
                            label="Bold" 
                        />
                        <ToolbarButton 
                            onClick={() => editor.chain().focus().toggleItalic().run()} 
                            isActive={editor.isActive('italic')} 
                            icon={Italic} 
                            label="Italic" 
                        />
                        <ToolbarButton 
                            onClick={() => editor.chain().focus().toggleUnderline().run()} 
                            isActive={editor.isActive('underline')} 
                            icon={UnderlineIcon} 
                            label="Underline" 
                        />
                        <ToolbarButton 
                            onClick={() => editor.chain().focus().toggleStrike().run()} 
                            isActive={editor.isActive('strike')} 
                            icon={Strikethrough} 
                            label="Strike" 
                        />
                        
                        <div className="w-px h-6 bg-neutral-700 mx-2" />
                        
                        <ToolbarButton 
                            onClick={() => editor.chain().focus().toggleBulletList().run()} 
                            isActive={editor.isActive('bulletList')} 
                            icon={List} 
                            label="Bullet List" 
                        />
                        <ToolbarButton 
                            onClick={() => editor.chain().focus().toggleOrderedList().run()} 
                            isActive={editor.isActive('orderedList')} 
                            icon={ListOrdered} 
                            label="Ordered List" 
                        />

                        <div className="w-px h-6 bg-neutral-700 mx-2" />

                        <ToolbarButton 
                            onClick={setLink} 
                            isActive={editor.isActive('link')} 
                            icon={LinkIcon} 
                            label="Link" 
                        />
                        <ToolbarButton 
                            onClick={addImage} 
                            isActive={false} 
                            icon={ImageIcon} 
                            label="Add Image" 
                        />

                        <div className="w-px h-6 bg-neutral-700 mx-2" />
                        
                        {/* Color Picker */}
                        <div className="flex items-center gap-1">
                            <ToolbarButton 
                                onClick={() => editor.chain().focus().setColor('#f59e0b').run()} // Amber-500
                                isActive={editor.isActive('textStyle', { color: '#f59e0b' })}
                                icon={() => <div className="w-4 h-4 rounded-full bg-amber-500 border border-neutral-600" />}
                                label="Amber Text"
                            />
                             <ToolbarButton 
                                onClick={() => editor.chain().focus().setColor('#ef4444').run()} // Red-500
                                isActive={editor.isActive('textStyle', { color: '#ef4444' })}
                                icon={() => <div className="w-4 h-4 rounded-full bg-red-500 border border-neutral-600" />}
                                label="Red Text"
                            />
                             <ToolbarButton 
                                onClick={() => editor.chain().focus().setColor('#10b981').run()} // Emerald-500
                                isActive={editor.isActive('textStyle', { color: '#10b981' })}
                                icon={() => <div className="w-4 h-4 rounded-full bg-emerald-500 border border-neutral-600" />}
                                label="Green Text"
                            />
                            <ToolbarButton 
                                onClick={() => editor.chain().focus().unsetColor().run()} 
                                isActive={false}
                                icon={() => <div className="w-4 h-4 rounded-full bg-slate-300 border border-neutral-600 relative overflow-hidden">
                                    <div className="absolute inset-0 bg-red-500 rotate-45 w-[1px] h-[150%] left-1/2 -ml-[0.5px] -mt-1" />
                                </div>}
                                label="Reset Color"
                            />
                        </div>
                    </>
                )}
            </div>

            {/* Editor Content */}
            <div className="flex-1 overflow-y-auto bg-neutral-950 custom-scrollbar relative">
              <EditorContent editor={editor} />
            </div>

            {/* Footer */}
            <div className="p-4 border-t border-neutral-800 bg-neutral-900/50 flex justify-end gap-3">
              <button 
                onClick={onClose} 
                className="px-4 py-2 text-neutral-400 hover:text-white font-medium transition-colors"
                disabled={status === 'saving'}
              >
                Cancel
              </button>
              <button 
                onClick={handleSave}
                disabled={status === 'saving' || status === 'saved'}
                className={`flex items-center gap-2 px-6 py-2 font-bold rounded-lg shadow-lg transition-all active:scale-95 ${
                    status === 'saved' 
                    ? 'bg-emerald-600 text-white shadow-emerald-900/20' 
                    : 'bg-gradient-to-r from-amber-600 to-amber-500 hover:from-amber-500 hover:to-amber-400 text-white shadow-amber-900/20'
                }`}
              >
                {status === 'saving' ? (
                    <>
                        <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                        Saving...
                    </>
                ) : status === 'saved' ? (
                    <>
                        <Check size={18} />
                        Saved
                    </>
                ) : (
                    <>
                        <Save size={18} />
                        Save Notes
                    </>
                )}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default NoteModal;
