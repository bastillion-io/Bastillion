/**
 * Copyright 2017 Harsh Yadav - harshyadav2829@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This jquery plugin augments the xterm terminal provided by term.js to support
 * modern scroll functionality by implementing an overlay scrollbar.
 */
 
(function($) {
    $.fn.terminalScroll = function(terminal) {
            var me = $(this).find('.terminal');
            var height =  parseInt(me.height());
            var displacement = 5;
            var start, stop;
            var netScrollLine = 0;
            var width = parseInt(me.width());
            var isDraggging = false;
            var isOnScrollHandler = false;
            
            /*
            *scrollBarContainer{scrollHandler, scrollBar}
            */

            var scrollBarContainer = $('<div></div>').css({
                height: height,
                position: 'absolute',
                right: '0px',
                top: '0px',
                width: '30px',
				visibility: 'hidden'
            });

            var scrollBar = $('<div></div>').css({
                background: '#C3C3C3',
                height: '100%',
                'min-height' : '8px',
                position: 'absolute',
                right: '0px',
                top: '0px',
                width: '8px',
                'border-radius': '4px'
            });

            var scrollHandler = $('<div></div>').css({
                height: '50px',
                width: '20px',
                position: 'absolute',
                left: '0px',
                top: '0px',
                'border-radius': '10px',
                background: '#C3C3C3',
                display: 'none',
				cursor: 'default'
            });
            
            var scrollUpButton = $('<div id="up"><span style="position:relative; top: 3px; left: 7px;">&#9650;</span></div>').css({
                height: '20px',
                width: '100%',
                color: '#000',
            });

            var scrollMiddleCircle = $('<div></div>').css({
                height: '10px',
                width: '10px',
                margin: '0px 5px',
                background: '#000',
                'border-radius': '10px',
            });
            
            var scrollDownButton = $('<div id="down"><span style="position: relative; top: 2px; left: 7px;">&#9660;</span></div>').css({
                height: '20px',
                width: '100%',
                color: '#000',
            });

            //set position of terminal* div to relative
            me.css({position: 'relative'});

            //assemble scrollbar components 
            scrollHandler.append(scrollUpButton);
            scrollHandler.append(scrollMiddleCircle);
            scrollHandler.append(scrollDownButton);
            scrollBarContainer.append(scrollHandler);
            scrollBarContainer.append(scrollBar);
            
            //attach scrollbar to terminal*
            me.append(scrollBarContainer);
            
            //Handle display of scrollHandler based on mouse events
            scrollHandler.mouseenter(function(){
                isOnScrollHandler = true;
            });
            scrollHandler.mouseleave(function(){
                isOnScrollHandler = false;
            });
            
            scrollBarContainer.mouseenter(function(e){
                scrollHandler.show();
            });
            scrollBarContainer.mouseleave(function(e){
                scrollHandler.hide();
            });
            
            scrollBarContainer.mousemove(function(e){
                if (isDraggging == false && isOnScrollHandler == false ) {
                    var containerTop = parseInt(scrollBarContainer.offset().top);
                    var mousePointerY = e.pageY;  //mouse pointer y
                    var scrollHandlerHeight = parseInt(scrollHandler.height());
                    
                    if (mousePointerY + scrollHandlerHeight > containerTop + parseInt(me.height())) {
                        scrollHandler.css('top', parseInt(me.height()) - scrollHandlerHeight);
                    } else {
                        scrollHandler.css('top', mousePointerY - containerTop);
                    }
                }
            });
            
            $(document).on('click', '#up', function() {
                scrollUp();
            });

            $(document).on('click', '#down', function() {
                scrollDown();
            });
            
            var scrollUp = function() {
                var top = parseInt(scrollBar.css('top'));

                if (terminal.ydisp > 0) {
                    /*
                    *if pixels are more than number of lines to scroll then
                    *scroll one line and set the top of scroll accordingly
                    */
                    if (top > terminal.ydisp) {
                        scrollBar.css('top', parseInt(top - (top / terminal.ydisp)));
                        terminal.scrollDisp(-1);
                    }
                    /*
                    *pixel are less than number of lines to scroll then
                    *reduce top of scrollbar by one pixel and scroll
                    *terminal's line accordingly
                    */
                    else {
                        scrollBar.css('top', top - 1);
                        terminal.scrollDisp(-parseInt(terminal.ydisp / top));
                    }
                }
            }
            
            var scrollDown = function() {
                var top = parseInt(scrollBar.css('top'));
                var scrollArea = parseInt(me.height()) - top - parseInt(scrollBar.height());
                var lastLine = terminal.ydisp + terminal.rows;
                var scrollLines = terminal.lines.length - lastLine;
                
                if (lastLine < terminal.lines.length) {
                    /*
                    *if pixels are more than number of lines to scroll then
                    *scroll one line and set the top of scroll accordingly
                    */
                    if (scrollArea > scrollLines) {
                        scrollBar.css('top', parseInt(top + (scrollArea / scrollLines)));
                        terminal.scrollDisp(1);
                    }
                    /*
                    *pixel are less than number of lines to scroll then
                    *reduce top of scrollbar by one pixel and scroll
                    *terminal's line accordingly
                    */
                    else {
                        scrollBar.css('top', top + 1);
                        terminal.scrollDisp(parseInt(scrollLines / scrollArea));
                    }
                }
            }
            
            var dragScroll = function(str, stp) {
                var movement = str - stp;
                var top = parseInt(scrollBar.css('top'));
                //scroll up
                if (movement > 0 && terminal.ydisp > 0) {
                    /*
                    *if pixels are more than number of lines to scroll then
                    *scroll one line and set the top of scrollbar accordingly
                    */
                    if (top > terminal.ydisp) {
                        var pixelsPerLine = parseInt(top / terminal.ydisp);
                        var linesToScroll = parseInt(movement / pixelsPerLine);
                        if (linesToScroll > 0) {
                            terminal.scrollDisp(-linesToScroll);
                            scrollBar.css('top', top - pixelsPerLine * linesToScroll);
                        }
                        else {
                            terminal.scrollDisp(-1);
                            scrollBar.css('top', top - pixelsPerLine);
                        }
                    }
                    /*
                    *pixel are less than number of lines to scroll then
                    *reduce top of scrollbar by one pixel and scroll
                    *terminal's line accordingly
                    */
                    else {
                        var linesPerPixel = parseInt(terminal.ydisp / top);
                        var linesToScroll = linesPerPixel * movement;
                        
                        if (linesToScroll > 0) {
                            terminal.scrollDisp(-linesToScroll);
                            scrollBar.css('top', top - parseInt(linesToScroll / linesPerPixel));
                        }
                    }
                    //limiting case
                    if (terminal.ydisp == 0) {
                        scrollBar.css('top', '0px');
                    }
                }
                //scroll down
                else if (movement < 0 && (terminal.ydisp + terminal.rows) < terminal.lines.length) {
                    movement = -movement;
                    var scrollArea = parseInt(me.height()) - parseInt(scrollBar.css('top')) - parseInt(scrollBar.height());
                    var lines = terminal.lines.length - terminal.ydisp - terminal.rows;
                    /*
                    *if pixels are more than number of lines to scroll then
                    *scroll one line and set the top of scrollbar accordingly
                    */
                    if (scrollArea > lines) {
                        var pixelsPerLine = parseInt(scrollArea / lines);
                        var linesToScroll = parseInt(movement / pixelsPerLine);
                        
                        if (linesToScroll > 0) {
                            terminal.scrollDisp(linesToScroll);
                            scrollBar.css('top', top + linesToScroll * pixelsPerLine);
                        } else {
                            terminal.scrollDisp(1);
                            scrollBar.css('top', top + pixelsPerLine);
                        }
                    } 
                    /*
                    *pixel are less than number of lines to scroll then
                    *reduce top of scrollbar by one pixel and scroll
                    *terminal's line accordingly
                    */
                    else {
                        var linesPerPixel = parseInt(lines / scrollArea);
                        var linesToScroll = linesPerPixel * movement;
                        
                        if (linesToScroll > 0) {
                            terminal.scrollDisp(linesToScroll);
                            scrollBar.css('top', top + parseInt(linesToScroll / linesPerPixel));
                        }
                    }
                    if (terminal.ydisp + terminal.rows == terminal.lines.length) {
                        scrollBar.css('top', parseInt(me.height()) - parseInt(scrollBar.height()));
                    }
                }
            }
            
            $(scrollHandler).draggable({
                axis: 'y',
                containment: 'parent',
                start: function(e, ui) {
                    start = ui.position.top;
                    isDraggging = true;
                },
                drag: function(e, ui) {
                    stop = ui.position.top;
                    dragScroll(parseInt(start), parseInt(stop));
                    start = stop;
                    scrollHandler.show();
                },
                stop: function() {
                    isDraggging = false;
                }
            });
        
            /*
            *Bind mousewheel(IE, chrome and safari)/DOMMouseScroll(FE) event so that scrollbar position change
            *corresponding to the lines scrolled in terminal
            */
            
            me.bind('mousewheel DOMMouseScroll', function(event) {
                var top = parseInt(scrollBar.css('top'));
                var line = terminal.ydisp;
                if (event.originalEvent.wheelDelta > 0 || event.originalEvent.detail < 0) {
                    //set scrollBar top
                    if (line > 5) {
                        scrollBar.css('top', top - (top / line) * 5);
					} else {
                        scrollBar.css('top', '0px');
					}
                }
                else {
                    if (terminal.lines.length - line - terminal.rows > 5) {
                        scrollBar.css('top', top + ((parseInt(me.height()) - top - scrollBar.height()) / (terminal.lines.length - line - terminal.rows))*5);
					} else {
                        scrollBar.css('top', parseInt(me.height()) - scrollBar.height());
					}
                }
            });
                        
            /*
            *This object is returned to the caller as on write event
            *in terminal the length and the position of scrollbar should change.
            *This is done using this closure. Here the setScrollBar function can be
            *called from the caller where scrollbar is initialized.
            */
            return {
                setScrollBar : function() {
                    if (terminal.rows < terminal.lines.length) {
                        scrollBarContainer.css({'visibility': 'visible'});
                    }
                    var height = parseInt(me.height());
                    scrollBarContainer.height(height);
                    var effectiveHeight = parseInt(Math.max(scrollBar.css('min-height').replace('px',''), terminal.rows * height / terminal.lines.length));
                    scrollBar.height(effectiveHeight);
                    scrollBar.css({top: height - effectiveHeight});
                }
            };
    }
}(jQuery));