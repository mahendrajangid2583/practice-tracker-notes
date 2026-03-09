import { Node, mergeAttributes } from '@tiptap/core';

export default Node.create({
    name: 'imageCaption',

    group: 'block',

    content: 'inline*',

    draggable: false,

    defining: true,

    parseHTML() {
        return [
            { tag: 'p.image-caption' },
            { tag: 'div.image-caption' }, // Backwards compat
            { tag: 'figcaption' }         // Backwards compat
        ];
    },

    renderHTML({ HTMLAttributes }) {
        return ['p', mergeAttributes(HTMLAttributes, { class: 'image-caption text-center text-sm text-neutral-500 mt-2 mb-8 leading-relaxed italic' }), 0];
    },
});
