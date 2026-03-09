import { Node, mergeAttributes } from '@tiptap/core';
import { ReactNodeViewRenderer } from '@tiptap/react';
import ImageBlockView from '../ImageBlockView';

export default Node.create({
    name: 'imageBlock',

    group: 'block',

    draggable: true,

    // Leaf node, no content
    atom: true,

    addAttributes() {
        return {
            src: {
                default: null,
            },
            alt: {
                default: null,
            },
            align: {
                default: 'center',
            },
            width: {
                default: '100%',
            }
        };
    },

    parseHTML() {
        return [
            {
                tag: 'div[class="note-image"]',
                getAttrs: element => ({
                    src: element.querySelector('img')?.getAttribute('src'),
                    alt: element.querySelector('img')?.getAttribute('alt'),
                }),
            },
            {
                tag: 'img[src]',
                getAttrs: node => ({
                    src: node.getAttribute('src'),
                    alt: node.getAttribute('alt'),
                }),
            },
        ];
    },

    renderHTML({ HTMLAttributes }) {
        return [
            'div',
            mergeAttributes(HTMLAttributes, { class: 'note-image flex flex-col items-center mt-6 mb-2' }),
            ['img', mergeAttributes(HTMLAttributes, { class: 'visual-image rounded-xl border border-neutral-800/50 shadow-sm' })]
        ];
    },

    addCommands() {
        return {
            setImageBlock:
                (options) =>
                    ({ commands }) => {
                        return commands.insertContent([
                            {
                                type: 'imageBlock',
                                attrs: options,
                            },
                            {
                                type: 'imageCaption',
                            }
                        ]);
                    },
        };
    },

    addNodeView() {
        return ReactNodeViewRenderer(ImageBlockView);
    },
});
