const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const source = fs.readFileSync('src/main/webapp/admin/secure_shell.html', 'utf8');
const start = source.indexOf('termMap[id].attachCustomKeyEventHandler(');
const end = source.indexOf('termMap[id].onData(', start);
function setup(selection) {
    const sent = [];
    let handler;
    vm.runInNewContext(source.slice(start, end), {
        id: 1,
        termMap: {1: {hasSelection: () => selection, attachCustomKeyEventHandler: fn => { handler = fn; }}},
        sendToActiveTerminals: value => sent.push(JSON.parse(JSON.stringify(value)))
    });
    return {handler, sent};
}
test('Ctrl+C without a selection sends one multiplex interrupt and suppresses duplicate xterm input', () => {
    const {handler, sent} = setup(false);
    let prevented = false;
    assert.equal(handler({type:'keydown', key:'c', ctrlKey:true, preventDefault:()=>{prevented=true;}}), false);
    assert.deepEqual(sent, [{keyCode:67}]);
    assert.equal(prevented, true);
});
test('copying selected output never interrupts remote commands', () => {
    for (const modifier of ['ctrlKey','metaKey']) {
        const {handler, sent} = setup(true);
        assert.equal(handler({type:'keydown',key:'c',[modifier]:true}), false);
        assert.deepEqual(sent, []);
    }
});
test('ordinary characters, navigation, and other control shortcuts stay with xterm', () => {
    const {handler, sent} = setup(false);
    for (const event of [{key:'a'}, {key:'ArrowUp'}, {key:'u',ctrlKey:true}, {key:'Enter'}, {key:'c',type:'keyup',ctrlKey:true}]) {
        assert.equal(handler({type:'keydown',...event}), true);
    }
    assert.deepEqual(sent, []);
});
