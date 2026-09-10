const test = require('node:test');
const assert = require('node:assert/strict');
const createFilter = require('../../src/main/webapp/_res/js/terminal-output.js');
const markers = ['\x1b[1m\x1b[7m%\x1b[27m\x1b[1m\x1b[0m', '\x1b[1m\x1b[7m%\x1b[27m\x1b[0m'];
test('removes every zsh marker at every possible frame boundary', () => {
    for (const marker of markers) {
        for (let i = 0; i <= marker.length; i++) {
            const filter = createFilter();
            assert.equal(filter('hello\r\n' + marker.slice(0, i)) + filter(marker.slice(i) + '\r\nprompt % '), 'hello\r\n\r\nprompt % ');
        }
        const filter = createFilter();
        assert.equal([...marker].map(c => filter(c)).join(''), '');
        assert.equal(filter(marker + 'one' + marker + 'two'), 'onetwo');
    }
});
test('preserves literal percentages, prompts, ANSI colors, and ordinary reverse video', () => {
    const text = '%\r\n100% complete\r\nuser@host ~ % \x1b[31mred\x1b[0m \x1b[7m%\x1b[0m';
    const filter = createFilter();
    assert.equal([...text].map(c => filter(c)).join(''), text);
});
test('multiplexed sessions keep independent partial escape sequences', () => {
    const a = createFilter(), b = createFilter();
    assert.equal(a(markers[0].slice(0, 8)), '');
    assert.equal(b('second session 50%'), 'second session 50%');
    assert.equal(a(markers[0].slice(8) + 'first session'), 'first session');
});
