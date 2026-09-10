/* Preserve terminal output while removing zsh's styled PROMPT_EOL_MARK.
 * WebSocket frames can split any escape sequence, so keep partial markers per session.
 */
(function (root) {
    'use strict';
    var markers = [
        '\x1b[1m\x1b[7m%\x1b[27m\x1b[1m\x1b[0m',
        '\x1b[1m\x1b[7m%\x1b[27m\x1b[0m'
    ];
    function createOutputFilter() {
        var pending = '';
        return function (chunk) {
            var input = pending + chunk;
            pending = '';
            markers.forEach(function (marker) { input = input.split(marker).join(''); });
            // Hold only an exact prefix of a known marker, never ordinary text.
            var keep = 0;
            markers.forEach(function (marker) {
                for (var size = 1; size < marker.length && size <= input.length; size++) {
                    if (input.endsWith(marker.slice(0, size))) keep = Math.max(keep, size);
                }
            });
            if (keep) { pending = input.slice(-keep); input = input.slice(0, -keep); }
            return input;
        };
    }
    if (typeof module !== 'undefined' && module.exports) module.exports = createOutputFilter;
    else root.createBastillionOutputFilter = createOutputFilter;
}(typeof window !== 'undefined' ? window : globalThis));
