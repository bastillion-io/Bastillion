/*

 Copyright (c) 2009 Dimas Begunoff, http://www.farinspace.com

 Licensed under the MIT license
 http://en.wikipedia.org/wiki/MIT_License

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.

 */

;(function($){

	$.fn.tableScroll = function(options)
	{
		var Me = this;
		var timeoutHandle;

		this.scrollbarWidth = 0;

		this.FormatTable = function()
		{
			var flush = settings.flush;

			var originalClass = $(this).attr("class");

			var tb = $(this).addClass('tablescroll_body');

			var wrapper = $('<div class="tablescroll_wrapper"></div>').insertBefore(tb).append(tb);

			// check for a predefined container
			if (!wrapper.parent('div').hasClass(settings.containerClass))
			{
				$('<div></div>').addClass(settings.containerClass).insertBefore(wrapper).append(wrapper);
			}

			var width = settings.width ? settings.width : tb.outerWidth();

			wrapper.css
			({
				'width': width+'px',
				'height': settings.height+'px',
				'overflow': 'auto'
			});

			tb.css('width',width+'px');

			// with border difference
			var wrapper_width = wrapper.outerWidth();
			var diff = wrapper_width-width;

			// assume table will scroll
			wrapper.css({width:((width-diff)+Me.scrollbarWidth)+'px'});
			tb.css('width',(width-diff)+'px');

			if (tb.outerHeight() <= settings.height)
			{
				wrapper.css({height:'auto',width:(width-diff)+'px'});
				flush = false;
			}

			// using wrap does not put wrapper in the DOM right 
			// away making it unavailable for use during runtime
			// tb.wrap(wrapper);

			// possible speed enhancements
			var has_thead = $('thead',tb).length ? true : false ;
			var has_tfoot = $('tfoot',tb).length ? true : false ;
			var thead_tr_first = $('thead tr:first',tb);
			var tbody_tr_first = $('tbody tr:first',tb);
			var tfoot_tr_first = $('tfoot tr:first',tb);

			// Record cell widths
			// Shoud colspan > 1, evenly distribute width among grouped cells
			var aWidths = [];
			$('th, td',thead_tr_first).each(function(i)
			{
				var jCell = $(this);
				var colspan = jCell.attr('colspan') || 1;
				var colspanIterator = colspan;
				var avgWidth = jCell.width()/colspan;
				while (colspanIterator) {
					aWidths.push(avgWidth);
					colspanIterator--;
				}

			});

			// Explicitly set width of header cells
			function SetCellWidths(_selector, _base, _aCellWidths) {
				var cellIdx = 0;
				var w = 0;
				$(_selector, _base).each(function(i) {
					var jCell = $(this);
					var colspan = jCell.attr('colspan') || 1;
					var w = 0;
					while (colspan) {
						w += _aCellWidths[cellIdx];
						cellIdx++;
						colspan--;
					}
					jCell.width(w);
				});
			}

			SetCellWidths("th, td", thead_tr_first, aWidths);
			SetCellWidths("th, td", tbody_tr_first, aWidths);
			if (has_tfoot) SetCellWidths("th, td", tfoot_tr_first, aWidths);

			if (has_thead)
			{
				var thead = $('thead',tb);
				var tbh = $('<table class="tablescroll_head '+originalClass+'" cellspacing="0"></table>')
					.insertBefore(wrapper)
					.css({'margin-bottom':'0'})
					.prepend(thead.clone());
				tb.css({'table-layout':'fixed'});
				thead.hide();
			}

			if (has_tfoot)
			{
				var tfoot = $('tfoot',tb);
				var tbf = $('<table class="tablescroll_foot '+originalClass+'" cellspacing="0"></table>')
					.insertAfter(wrapper)
					.css({'margin-top':'0'})
					.prepend(tfoot.clone());
				tfoot.hide();
			}

			if (tbh != undefined)
			{
				tbh.css('width',width+'px');

				if (flush)
				{
					$('tr:first', tbh).append($('<th></th>').css({
						'width': Me.scrollbarWidth + 'px'
						, 'padding': 0
					}));
					tbh.css('width',wrapper.outerWidth() + 'px');
				}
			}

			if (tbf != undefined)
			{
				tbf.css('width',width+'px');

				if (flush)
				{
					$('tr:first', tbf).append($('<td></td>').css({
						'width': Me.scrollbarWidth + 'px'
						, 'padding': 0
					}));
					tbf.css('width',wrapper.outerWidth() + 'px');
				}
			}
		};

		this.UndoFormatTable = function() {
			var jThis = $(this);
			var container = jThis.parents("div.tablescroll");
			if (container.length > 0)
			{
				container.before(this);
				container.empty();
				jThis.removeAttr("style");
				jThis.find("thead, tfoot").show();
			}
			return;
		}

		// http://jdsharp.us/jQuery/minute/calculate-scrollbar-width.php
		this.GetScrollbarWidth = function() {
			if (Me.scrollbarWidth) return Me.scrollbarWidth;
			var div = $('<div style="width:50px;height:50px;overflow:hidden;position:absolute;top:-200px;left:-200px;"><div style="height:100px;"></div></div>');
			$('body').append(div);
			var w1 = $('div', div).innerWidth();
			div.css('overflow-y', 'auto');
			var w2 = $('div', div).innerWidth();
			$(div).remove();
			Me.scrollbarWidth = (w1 - w2);
		}

		// -----------------------------------------------------------------------------
		if (options == 'undo') {
			Me.UndoFormatTable();
		}

		var settings = $.extend({}, $.fn.tableScroll.defaults, options);

		// Bail out if there's no vertical overflow
		//if ($(this).height() <= settings.height)
		//{
		//  return this;
		//}

		// Calculate scrollbar width and save for later
		Me.GetScrollbarWidth();

		// Apply formatting to each table
		this.each(Me.FormatTable);

		// Setup window.resize event handler
		$(window).unbind("resize.tableScroll").bind("resize.tableScroll", function() {
			clearTimeout(Me.timeoutHandle);
			Me.timeoutHandle = setTimeout(function() {
				Me.each(Me.UndoFormatTable);
				Me.each(Me.FormatTable);
			}, 300);
		});

		return this;
	};

	// public
	$.fn.tableScroll.defaults =
	{
		flush: true, // makes the last thead and tbody column flush with the scrollbar
		width: null, // width of the table (head, body and foot), null defaults to the tables natural width
		height: 100, // height of the scrollable area
		containerClass: 'tablescroll' // the plugin wraps the table in a div with this css class
	};

})(jQuery);
