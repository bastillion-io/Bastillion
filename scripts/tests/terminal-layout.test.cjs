const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const source = fs.readFileSync('src/main/webapp/admin/secure_shell.html', 'utf8');
const start = source.indexOf('            function resetSize() {');
const end = source.indexOf('            var windowResizeTimer;', start);

test('terminal height stays constant as sessions are added and the viewport shrinks', () => {
    for (const count of [1, 4, 8]) {
        for (const viewportHeight of [500, 900]) {
            const heights = [];
            const resized = [];
            const cards = Array.from({length: count}, (_, id) => ({id: 'run_cmd_' + id}));
            const output = {closest: () => ({css() {}}), css() {}, height: h => heights.push(h)};
            const $ = selector => {
                if (selector === '.run_cmd') return {length: count, each: fn => cards.forEach(card => fn.call(card))};
                if (typeof selector === 'object') return {attr: () => selector.id};
                return output;
            };
            vm.runInNewContext(source.slice(start, end) + '\nresetSize();', {
                $, document: {querySelector: () => ({})}, window: {innerHeight: viewportHeight},
                resize: element => resized.push(element)
            });
            assert.deepEqual(heights, Array(count).fill(320));
            assert.equal(resized.length, count, 'each terminal must still update its remote size');
        }
    }
});
