// @preserve jQuery.floatThead 1.2.11 - http://mkoryak.github.io/floatThead/ - Copyright (c) 2012 - 2014 Misha Koryak
// @license MIT

/* @author Misha Koryak
 * @projectDescription lock a table header in place while scrolling - without breaking styles or events bound to the header
 *
 * Dependencies:
 * jquery 1.9.0 + [required] OR jquery 1.7.0 + jquery UI core
 *
 * http://mkoryak.github.io/floatThead/
 *
 * Tested on FF13+, Chrome 21+, IE8, IE9, IE10, IE11
 *
 */
(function( $ ) {
  /**
   * provides a default config object. You can modify this after including this script if you want to change the init defaults
   * @type {Object}
   */
  $.floatThead = $.floatThead || {};
  $.floatThead.defaults = {
    cellTag: null, // DEPRECATED - use headerCellSelector instead
    headerCellSelector: 'tr:visible:first>*:visible', //thead cells are this.
    zIndex: 1001, //zindex of the floating thead (actually a container div)
    debounceResizeMs: 10,
    useAbsolutePositioning: true, //if set to NULL - defaults: has scrollContainer=true, doesn't have scrollContainer=false
    scrollingTop: 0, //String or function($table) - offset from top of window where the header should not pass above
    scrollingBottom: 0, //String or function($table) - offset from the bottom of the table where the header should stop scrolling
    scrollContainer: function($table){
      return $([]); //if the table has horizontal scroll bars then this is the container that has overflow:auto and causes those scroll bars
    },
    getSizingRow: function($table, $cols, $fthCells){ // this is only called when using IE,
      // override it if the first row of the table is going to contain colgroups (any cell spans greater then one col)
      // it should return a jquery object containing a wrapped set of table cells comprising a row that contains no col spans and is visible
      return $table.find('tbody tr:visible:first>*:visible');
    },
    floatTableClass: 'floatThead-table',
    floatWrapperClass: 'floatThead-wrapper',
    floatContainerClass: 'floatThead-container',
    copyTableClass: true, //copy 'class' attribute from table into the floated table so that the styles match.
    enableAria: false, //will copy header text from the floated header back into the table for screen readers. Might cause the css styling to be off. beware!
    autoReflow: false, //(undocumeted) - use MutationObserver api to reflow automatically when internal table DOM changes
    debug: false //print possible issues (that don't prevent script loading) to console, if console exists.
  };

  var util = window._;

  var canObserveMutations = typeof MutationObserver !== 'undefined';


  //browser stuff
  var ieVersion = function(){for(var a=3,b=document.createElement("b"),c=b.all||[];a = 1+a,b.innerHTML="<!--[if gt IE "+ a +"]><i><![endif]-->",c[0];);return 4<a?a:document.documentMode}();
  var isFF = /Gecko\//.test(navigator.userAgent);
  var isWebkit = /WebKit\//.test(navigator.userAgent);

  //safari 7 (and perhaps others) reports table width to be parent container's width if max-width is set on table. see: https://github.com/mkoryak/floatThead/issues/108
  var isTableWidthBug = function(){
    if(isWebkit) {
      var $test = $('<div style="width:0px"><table style="max-width:100%"><tr><th><div style="min-width:100px;">X</div></th></tr></table></div>');
      $("body").append($test);
      var ret = ($test.find("table").width() == 0);
      $test.remove();
      return ret;
    }
    return false;
  };

  var createElements = !isFF && !ieVersion; //FF can read width from <col> elements, but webkit cannot

  var $window = $(window);

  /**
   * @param debounceMs
   * @param cb
   */
  function windowResize(debounceMs, eventName, cb){
    if(ieVersion == 8){ //ie8 is crap: https://github.com/mkoryak/floatThead/issues/65
      var winWidth = $window.width();
      var debouncedCb = util.debounce(function(){
        var winWidthNew = $window.width();
        if(winWidth != winWidthNew){
          winWidth = winWidthNew;
          cb();
        }
      }, debounceMs);
      $window.on(eventName, debouncedCb);
    } else {
      $window.on(eventName, util.debounce(cb, debounceMs));
    }
  }


  function debug(str){
    window.console && window.console && window.console.log && window.console.log("jQuery.floatThead: " + str);
  }

  /**
   * try to calculate the scrollbar width for your browser/os
   * @return {Number}
   */
  function scrollbarWidth() {
    var $div = $( //borrowed from anti-scroll
        '<div style="width:50px;height:50px;overflow-y:scroll;'
        + 'position:absolute;top:-200px;left:-200px;"><div style="height:100px;width:100%">'
        + '</div>'
    );
    $('body').append($div);
    var w1 = $div.innerWidth();
    var w2 = $('div', $div).innerWidth();
    $div.remove();
    return w1 - w2;
  }
  /**
   * Check if a given table has been datatableized (http://datatables.net)
   * @param $table
   * @return {Boolean}
   */
  function isDatatable($table){
    if($table.dataTableSettings){
      for(var i = 0; i < $table.dataTableSettings.length; i++){
        var table = $table.dataTableSettings[i].nTable;
        if($table[0] == table){
          return true;
        }
      }
    }
    return false;
  }

  function tableWidth($table, $fthCells, isOuter){
    // see: https://github.com/mkoryak/floatThead/issues/108
    var fn = isOuter ? "outerWidth": "width";
    if(isTableWidthBug && $table.css("max-width")){
      var w = 0;
      if(isOuter) {
        w += parseInt($table.css("borderLeft"), 10);
        w += parseInt($table.css("borderRight"), 10);
      }
      for(var i=0; i < $fthCells.length; i++){
        w += $fthCells.get(i).offsetWidth;
      }
      return w;
    } else {
      return $table[fn]();
    }
  }
  $.fn.floatThead = function(map){
    map = map || {};
    if(!util){ //may have been included after the script? lets try to grab it again.
      util = window._ || $.floatThead._;
      if(!util){
        throw new Error("jquery.floatThead-slim.js requires underscore. You should use the non-lite version since you do not have underscore.");
      }
    }

    if(ieVersion < 8){
      return this; //no more crappy browser support.
    }

    if(createElements){ //make sure this is done only once no matter how many times you call the plugin fn
        //because chrome cant read <col> width, these elements are used for sizing the table. Need to create new elements because they must be unstyled by user's css.
        document.createElement('fthtr'); //tr
        document.createElement('fthtd'); //td
        document.createElement('fthfoot'); //tfoot
    }
    var mObs = null; //mutation observer lives in here if we can use it / make it

    if(util.isFunction(isTableWidthBug)) {
      isTableWidthBug = isTableWidthBug();
    }

    if(util.isString(map)){
      var command = map;
      var ret = this;
      this.filter('table').each(function(){
        var obj = $(this).data('floatThead-attached');
        if(obj && util.isFunction(obj[command])){
          var r = obj[command]();
          if(typeof r !== 'undefined'){
            ret = r;
          }
        }
      });
      return ret;
    }
    var opts = $.extend({}, $.floatThead.defaults || {}, map);

    $.each(map, function(key, val){
      if((!(key in $.floatThead.defaults)) && opts.debug){
        debug("Used ["+key+"] key to init plugin, but that param is not an option for the plugin. Valid options are: "+ (util.keys($.floatThead.defaults)).join(', '));
      }
    });
    if(opts.debug){
      var v = $.fn.jquery.split(".");
      if(parseInt(v[0], 10) == 1 && parseInt(v[1], 10) <= 7){
        debug("jQuery version "+$.fn.jquery+" detected! This plugin supports 1.8 or better, or 1.7.x with jQuery UI 1.8.24 -> http://jqueryui.com/resources/download/jquery-ui-1.8.24.zip")
      }
    }

    this.filter(':not(.'+opts.floatTableClass+')').each(function(){
      var floatTheadId = util.uniqueId();
      var $table = $(this);
      if($table.data('floatThead-attached')){
        return true; //continue the each loop
      }
      if(!$table.is('table')){
        throw new Error('jQuery.floatThead must be run on a table element. ex: $("table").floatThead();');
      }
      canObserveMutations = opts.autoReflow && canObserveMutations; //option defaults to false!
      var $header = $table.find('thead:first');
      var $tbody = $table.find('tbody:first');
      if($header.length == 0){
        throw new Error('jQuery.floatThead must be run on a table that contains a <thead> element');
      }
      var headerFloated = false;
      var scrollingTop, scrollingBottom;
      var scrollbarOffset = {vertical: 0, horizontal: 0};
      var scWidth = scrollbarWidth();
      var lastColumnCount = 0; //used by columnNum()
      var $scrollContainer = opts.scrollContainer($table) || $([]); //guard against returned nulls

      var useAbsolutePositioning = opts.useAbsolutePositioning;
      if(useAbsolutePositioning == null){ //defaults: locked=true, !locked=false
        useAbsolutePositioning = opts.scrollContainer($table).length;
      }
      if(!useAbsolutePositioning){
        headerFloated = true; //#127
      }
      var $caption = $table.find("caption");
      var haveCaption = $caption.length == 1;
      if(haveCaption){
        var captionAlignTop = ($caption.css("caption-side") || $caption.attr("align") || "top") === "top";
      }

      var $fthGrp = $('<fthfoot style="display:table-footer-group;border-spacing:0;height:0;border-collapse:collapse;"/>');

      var locked = $scrollContainer.length > 0;
      var wrappedContainer = false; //used with absolute positioning enabled. did we need to wrap the scrollContainer/table with a relative div?
      var $wrapper = $([]); //used when absolute positioning enabled - wraps the table and the float container
      var absoluteToFixedOnScroll = ieVersion <= 9 && !locked && useAbsolutePositioning; //on ie using absolute positioning doesnt look good with window scrolling, so we change positon to fixed on scroll, and then change it back to absolute when done.
      var $floatTable = $("<table/>");
      var $floatColGroup = $("<colgroup/>");
      var $tableColGroup = $table.find('colgroup:first');
      var existingColGroup = true;
      if($tableColGroup.length == 0){
        $tableColGroup = $("<colgroup/>");
        existingColGroup = false;
      }
      var $fthRow = $('<fthrow style="display:table-row;border-spacing:0;height:0;border-collapse:collapse"/>'); //created unstyled elements
      var $floatContainer = $('<div style="overflow: hidden;" aria-hidden="true" class="floatThead-floatContainer"></div>');
      var floatTableHidden = false; //this happens when the table is hidden and we do magic when making it visible
      var $newHeader = $("<thead/>");
      var $sizerRow = $('<tr class="size-row"/>');
      var $sizerCells = $([]);
      var $tableCells = $([]); //used for sizing - either $sizerCells or $tableColGroup cols. $tableColGroup cols are only created in chrome for borderCollapse:collapse because of a chrome bug.
      var $headerCells = $([]);
      var $fthCells = $([]); //created elements

      $newHeader.append($sizerRow);
      $table.prepend($tableColGroup);
      if(createElements){
        $fthGrp.append($fthRow);
        $table.append($fthGrp);
      }

      $floatTable.append($floatColGroup);
      $floatContainer.append($floatTable);
      if(opts.copyTableClass){
        $floatTable.attr('class', $table.attr('class'));
      }
      $floatTable.attr({ //copy over some deprecated table attributes that people still like to use. Good thing poeple dont use colgroups...
        'cellpadding': $table.attr('cellpadding'),
        'cellspacing': $table.attr('cellspacing'),
        'border': $table.attr('border')
      });
      var tableDisplayCss = $table.css('display');
      $floatTable.css({
        'borderCollapse': $table.css('borderCollapse'),
        'border': $table.css('border'),
        'display': tableDisplayCss
      });
      if(tableDisplayCss == 'none'){
        floatTableHidden = true;
      }

      $floatTable.addClass(opts.floatTableClass).css('margin', 0); //must have no margins or you wont be able to click on things under floating table

      if(useAbsolutePositioning){
        var makeRelative = function($container, alwaysWrap){
          var positionCss = $container.css('position');
          var relativeToScrollContainer = (positionCss == "relative" || positionCss == "absolute");
          if(!relativeToScrollContainer || alwaysWrap){
            var css = {"paddingLeft": $container.css('paddingLeft'), "paddingRight": $container.css('paddingRight')};
            $floatContainer.css(css);
            $container = $container.wrap("<div class='"+opts.floatWrapperClass+"' style='position: relative; clear:both;'></div>").parent();
            wrappedContainer = true;
          }
          return $container;
        };
        if(locked){
          $wrapper = makeRelative($scrollContainer, true);
          $wrapper.append($floatContainer);
        } else {
          $wrapper = makeRelative($table);
          $table.after($floatContainer);
        }
      } else {
        $table.after($floatContainer);
      }


      $floatContainer.css({
        position: useAbsolutePositioning ? 'absolute' : 'fixed',
        marginTop: 0,
        top:  useAbsolutePositioning ? 0 : 'auto',
        zIndex: opts.zIndex
      });
      $floatContainer.addClass(opts.floatContainerClass);
      updateScrollingOffsets();

      var layoutFixed = {'table-layout': 'fixed'};
      var layoutAuto = {'table-layout': $table.css('tableLayout') || 'auto'};
      var originalTableWidth = $table[0].style.width || ""; //setting this to auto is bad: #70
      var originalTableMinWidth = $table.css('minWidth') || "";

      function eventName(name){
        return name+'.fth-'+floatTheadId+'.floatTHead'
      }

      function setHeaderHeight(){
        var headerHeight = 0;
        $header.find("tr:visible").each(function(){
          headerHeight += $(this).outerHeight(true);
        });
        $sizerRow.outerHeight(headerHeight);
        $sizerCells.outerHeight(headerHeight);
      }


      function setFloatWidth(){
        var tw = tableWidth($table, $fthCells, true);
        var width = $scrollContainer.width() || tw;
        var floatContainerWidth = $scrollContainer.css("overflow-y") != 'hidden' ? width - scrollbarOffset.vertical : width;
        $floatContainer.width(floatContainerWidth);
        if(locked){
          var percent = 100 * tw / (floatContainerWidth);
          $floatTable.css('width', percent+'%');
        } else {
          $floatTable.outerWidth(tw);
        }
      }

      function updateScrollingOffsets(){
        scrollingTop = (util.isFunction(opts.scrollingTop) ? opts.scrollingTop($table) : opts.scrollingTop) || 0;
        scrollingBottom = (util.isFunction(opts.scrollingBottom) ? opts.scrollingBottom($table) : opts.scrollingBottom) || 0;
      }

      /**
       * get the number of columns and also rebuild resizer rows if the count is different then the last count
       */
      function columnNum(){
        var count, $headerColumns;
        if(existingColGroup){
          count = $tableColGroup.find('col').length;
        } else {
          var selector;
          if(opts.cellTag == null && opts.headerCellSelector){ //TODO: once cellTag option is removed, remove this conditional
            selector = opts.headerCellSelector;
          } else {
            selector = 'tr:first>'+opts.cellTag;
          }
          if(util.isNumber(selector)){
            //its actually a row count. (undocumented, might be removed!)
            return selector;
          }
          $headerColumns = $header.find(selector);
          count = 0;
          $headerColumns.each(function(){
            count += parseInt(($(this).attr('colspan') || 1), 10);
          });
        }
        if(count != lastColumnCount){
          lastColumnCount = count;
          var cells = [], cols = [], psuedo = [], content;
          for(var x = 0; x < count; x++){
            if (opts.enableAria && (content = $headerColumns.eq(x).text()) ) {
              cells.push('<th scope="col" class="floatThead-col">' + content + '</th>');
            } else {
              cells.push('<th class="floatThead-col"/>');
            }
            cols.push('<col/>');
            psuedo.push("<fthtd style='display:table-cell;height:0;width:auto;'/>");
          }

          cols = cols.join('');
          cells = cells.join('');

          if(createElements){
            psuedo = psuedo.join('');
            $fthRow.html(psuedo);
            $fthCells = $fthRow.find('fthtd');
          }

          $sizerRow.html(cells);
          $sizerCells = $sizerRow.find("th");
          if(!existingColGroup){
            $tableColGroup.html(cols);
          }
          $tableCells = $tableColGroup.find('col');
          $floatColGroup.html(cols);
          $headerCells = $floatColGroup.find("col");

        }
        return count;
      }

      function refloat(){ //make the thing float
        if(!headerFloated){
          headerFloated = true;
          if(useAbsolutePositioning){ //#53, #56
            var tw = tableWidth($table, $fthCells);
            var wrapperWidth = $wrapper.width();
            if(tw > wrapperWidth){
              $table.css('minWidth', tw);
            }
          }
          $table.css(layoutFixed);
          $floatTable.css(layoutFixed);
          $floatTable.append($header); //append because colgroup must go first in chrome
          $tbody.before($newHeader);
          setHeaderHeight();
        }
      }
      function unfloat(){ //put the header back into the table
        if(headerFloated){
          headerFloated = false;
          if(useAbsolutePositioning){ //#53, #56
            $table.width(originalTableWidth);
          }
          $newHeader.detach();
          $table.prepend($header);
          $table.css(layoutAuto);
          $floatTable.css(layoutAuto);
          $table.css('minWidth', originalTableMinWidth); //this looks weird, but its not a bug. Think about it!!
          $table.css('minWidth', tableWidth($table, $fthCells)); //#121
        }
      }
      function changePositioning(isAbsolute){
        if(useAbsolutePositioning != isAbsolute){
          useAbsolutePositioning = isAbsolute;
          $floatContainer.css({
            position: useAbsolutePositioning ? 'absolute' : 'fixed'
          });
        }
      }
      function getSizingRow($table, $cols, $fthCells, ieVersion){
        if(createElements){
          return $fthCells;
        } else if(ieVersion) {
          return opts.getSizingRow($table, $cols, $fthCells);
        } else {
          return $cols;
        }
      }

      /**
       * returns a function that updates the floating header's cell widths.
       * @return {Function}
       */
      function reflow(){
        var i;
        var numCols = columnNum(); //if the tables columns change dynamically since last time (datatables) we need to rebuild the sizer rows and get new count

        return function(){
          $tableCells = $tableColGroup.find('col');
          var $rowCells = getSizingRow($table, $tableCells, $fthCells, ieVersion);

          if($rowCells.length == numCols && numCols > 0){
            if(!existingColGroup){
              for(i=0; i < numCols; i++){
                $tableCells.eq(i).css('width', '');
              }
            }
            unfloat();
            var widths = [];
            for(i=0; i < numCols; i++){
              widths[i] = $rowCells.get(i).offsetWidth;
            }
            for(i=0; i < numCols; i++){
              $headerCells.eq(i).width(widths[i]);
              $tableCells.eq(i).width(widths[i]);
            }
            refloat();
          } else {
            $floatTable.append($header);
            $table.css(layoutAuto);
            $floatTable.css(layoutAuto);
            setHeaderHeight();
          }
        };
      }

      function floatContainerBorderWidth(side){
        var border = $scrollContainer.css("border-"+side+"-width");
        var w = 0;
        if (border && ~border.indexOf('px')) {
          w = parseInt(border, 10);
        }
        return w;
      }
      /**
       * first performs initial calculations that we expect to not change when the table, window, or scrolling container are scrolled.
       * returns a function that calculates the floating container's top and left coords. takes into account if we are using page scrolling or inner scrolling
       * @return {Function}
       */
      function calculateFloatContainerPosFn(){
        var scrollingContainerTop = $scrollContainer.scrollTop();

        //this floatEnd calc was moved out of the returned function because we assume the table height doesnt change (otherwise we must reinit by calling calculateFloatContainerPosFn)
        var floatEnd;
        var tableContainerGap = 0;
        var captionHeight = haveCaption ? $caption.outerHeight(true) : 0;
        var captionScrollOffset = captionAlignTop ? captionHeight : -captionHeight;

        var floatContainerHeight = $floatContainer.height();
        var tableOffset = $table.offset();
        var tableLeftGap = 0; //can be caused by border on container (only in locked mode)
        if(locked){
          var containerOffset = $scrollContainer.offset();
          tableContainerGap = tableOffset.top - containerOffset.top + scrollingContainerTop;
          if(haveCaption && captionAlignTop){
            tableContainerGap += captionHeight;
          }
          tableContainerGap -= floatContainerBorderWidth('top');
          tableLeftGap = floatContainerBorderWidth('left');
        } else {
          floatEnd = tableOffset.top - scrollingTop - floatContainerHeight + scrollingBottom + scrollbarOffset.horizontal;
        }
        var windowTop = $window.scrollTop();
        var windowLeft = $window.scrollLeft();
        var scrollContainerLeft =  $scrollContainer.scrollLeft();
        scrollingContainerTop = $scrollContainer.scrollTop();



        return function(eventType){
          var isTableHidden = $table[0].offsetWidth <= 0 && $table[0].offsetHeight <= 0;
          if(!isTableHidden && floatTableHidden) {
            floatTableHidden = false;
            setTimeout(function(){
              $table.trigger("reflow");
            }, 1);
            return null;
          }
          if(isTableHidden){ //its hidden
            floatTableHidden = true;
            if(!useAbsolutePositioning){
              return null;
            }
          }

          if(eventType == 'windowScroll'){
            windowTop = $window.scrollTop();
            windowLeft = $window.scrollLeft();
          } else if(eventType == 'containerScroll'){
            scrollingContainerTop = $scrollContainer.scrollTop();
            scrollContainerLeft =  $scrollContainer.scrollLeft();
          } else if(eventType != 'init') {
            windowTop = $window.scrollTop();
            windowLeft = $window.scrollLeft();
            scrollingContainerTop = $scrollContainer.scrollTop();
            scrollContainerLeft =  $scrollContainer.scrollLeft();
          }
          if(isWebkit && (windowTop < 0 || windowLeft < 0)){ //chrome overscroll effect at the top of the page - breaks fixed positioned floated headers
            return;
          }

          if(absoluteToFixedOnScroll){
            if(eventType == 'windowScrollDone'){
              changePositioning(true); //change to absolute
            } else {
              changePositioning(false); //change to fixed
            }
          } else if(eventType == 'windowScrollDone'){
            return null; //event is fired when they stop scrolling. ignore it if not 'absoluteToFixedOnScroll'
          }

          tableOffset = $table.offset();
          if(haveCaption && captionAlignTop){
            tableOffset.top += captionHeight;
          }
          var top, left;
          var tableHeight = $table.outerHeight();

          if(locked && useAbsolutePositioning){ //inner scrolling, absolute positioning
            if (tableContainerGap >= scrollingContainerTop) {
              var gap = tableContainerGap - scrollingContainerTop;
              top = gap > 0 ? gap : 0;
            } else {
              top = wrappedContainer ? 0 : scrollingContainerTop;
              //headers stop at the top of the viewport
            }
            left = tableLeftGap;
          } else if(!locked && useAbsolutePositioning) { //window scrolling, absolute positioning
            if(windowTop > floatEnd + tableHeight + captionScrollOffset){
              top = tableHeight - floatContainerHeight + captionScrollOffset; //scrolled past table
            } else if (tableOffset.top > windowTop + scrollingTop) {
              top = 0; //scrolling to table
              unfloat();
            } else {
              top = scrollingTop + windowTop - tableOffset.top + tableContainerGap + (captionAlignTop ? captionHeight : 0);
              refloat(); //scrolling within table. header floated
            }
            left =  0;
          } else if(locked && !useAbsolutePositioning){ //inner scrolling, fixed positioning
            if (tableContainerGap > scrollingContainerTop || scrollingContainerTop - tableContainerGap > tableHeight) {
              top = tableOffset.top - windowTop;
              unfloat();
            } else {
              top = tableOffset.top + scrollingContainerTop  - windowTop - tableContainerGap;
              refloat();
              //headers stop at the top of the viewport
            }
            left = tableOffset.left + scrollContainerLeft - windowLeft;
          } else if(!locked && !useAbsolutePositioning) { //window scrolling, fixed positioning
            if(windowTop > floatEnd + tableHeight + captionScrollOffset){
              top = tableHeight + scrollingTop - windowTop + floatEnd + captionScrollOffset;
              //scrolled past the bottom of the table
            } else if (tableOffset.top > windowTop + scrollingTop) {
              top = tableOffset.top - windowTop;
              refloat();
              //scrolled past the top of the table
            } else {
              //scrolling within the table
              top = scrollingTop;
            }
            left = tableOffset.left - windowLeft;
          }
          return {top: top, left: left};
        };
      }
      /**
       * returns a function that caches old floating container position and only updates css when the position changes
       * @return {Function}
       */
      function repositionFloatContainerFn(){
        var oldTop = null;
        var oldLeft = null;
        var oldScrollLeft = null;
        return function(pos, setWidth, setHeight){
          if(pos != null && (oldTop != pos.top || oldLeft != pos.left)){
            $floatContainer.css({
              top: pos.top,
              left: pos.left
            });
            oldTop = pos.top;
            oldLeft = pos.left;
          }
          if(setWidth){
            setFloatWidth();
          }
          if(setHeight){
            setHeaderHeight();
          }
          var scrollLeft = $scrollContainer.scrollLeft();
          if(!useAbsolutePositioning || oldScrollLeft != scrollLeft){
            $floatContainer.scrollLeft(scrollLeft);
            oldScrollLeft = scrollLeft;
          }
        }
      }

      /**
       * checks if THIS table has scrollbars, and finds their widths
       */
      function calculateScrollBarSize(){ //this should happen after the floating table has been positioned
        if($scrollContainer.length){
          if($scrollContainer.data().perfectScrollbar){
            scrollbarOffset = {horizontal:0, vertical:0};
          } else {
            var sw = $scrollContainer.width(), sh = $scrollContainer.height(), th = $table.height(), tw = tableWidth($table, $fthCells);
            var offseth = sw < tw ? scWidth : 0;
            var offsetv = sh < th ? scWidth : 0;
            scrollbarOffset.horizontal = sw - offsetv < tw ? scWidth : 0;
            scrollbarOffset.vertical = sh - offseth < th ? scWidth : 0;
          }
        }
      }
      //finish up. create all calculation functions and bind them to events
      calculateScrollBarSize();

      var flow;

      var ensureReflow = function(){
        flow = reflow();
        flow();
      };

      ensureReflow();

      var calculateFloatContainerPos = calculateFloatContainerPosFn();
      var repositionFloatContainer = repositionFloatContainerFn();

      repositionFloatContainer(calculateFloatContainerPos('init'), true); //this must come after reflow because reflow changes scrollLeft back to 0 when it rips out the thead

      var windowScrollDoneEvent = util.debounce(function(){
        repositionFloatContainer(calculateFloatContainerPos('windowScrollDone'), false);
      }, 300);

      var windowScrollEvent = function(){
        repositionFloatContainer(calculateFloatContainerPos('windowScroll'), false);
        windowScrollDoneEvent();
      };
      var containerScrollEvent = function(){
        repositionFloatContainer(calculateFloatContainerPos('containerScroll'), false);
      };


      var windowResizeEvent = function(){
        updateScrollingOffsets();
        calculateScrollBarSize();
        ensureReflow();
        calculateFloatContainerPos = calculateFloatContainerPosFn();
        repositionFloatContainer = repositionFloatContainerFn();
        repositionFloatContainer(calculateFloatContainerPos('resize'), true, true);
      };
      var reflowEvent = util.debounce(function(){
        calculateScrollBarSize();
        updateScrollingOffsets();
        ensureReflow();
        calculateFloatContainerPos = calculateFloatContainerPosFn();
        repositionFloatContainer(calculateFloatContainerPos('reflow'), true);
      }, 1);
      if(locked){ //internal scrolling
        if(useAbsolutePositioning){
          $scrollContainer.on(eventName('scroll'), containerScrollEvent);
        } else {
          $scrollContainer.on(eventName('scroll'), containerScrollEvent);
          $window.on(eventName('scroll'), windowScrollEvent);
        }
      } else { //window scrolling
        $window.on(eventName('scroll'), windowScrollEvent);
      }

      $window.on(eventName('load'), reflowEvent); //for tables with images

      windowResize(opts.debounceResizeMs, eventName('resize'), windowResizeEvent);
      $table.on('reflow', reflowEvent);
      if(isDatatable($table)){
        $table
          .on('filter', reflowEvent)
          .on('sort',   reflowEvent)
          .on('page',   reflowEvent);
      }


      if (canObserveMutations) {
        var mutationElement = $scrollContainer.length ? $scrollContainer[0] : $table[0];
        mObs = new MutationObserver(function(e){
          var wasThead = function(nodes){
            return nodes && nodes[0] && nodes[0].nodeName == "THEAD";
          };
          for(var i=0; i < e.length; i++){
            if(!(wasThead(e[i].addedNodes) || wasThead(e[i].removedNodes))){
              reflowEvent();
              break;
            }
          }
        });
        mObs.observe(mutationElement, {
            childList: true,
            subtree: true
        });
      }

      //attach some useful functions to the table.
      $table.data('floatThead-attached', {
        destroy: function(){
          var ns = '.fth-'+floatTheadId;
          unfloat();
          $table.css(layoutAuto);
          $tableColGroup.remove();
          createElements && $fthGrp.remove();
          if($newHeader.parent().length){ //only if its in the dom
            $newHeader.replaceWith($header);
          }
          if(canObserveMutations){
            mObs.disconnect();
            mObs = null;
          }
          $table.off('reflow');
          $scrollContainer.off(ns);
          if (wrappedContainer) {
            if ($scrollContainer.length) {
              $scrollContainer.unwrap();
            }
            else {
              $table.unwrap();
            }
          }
          $table.css('minWidth', originalTableMinWidth);
          $floatContainer.remove();
          $table.data('floatThead-attached', false);
          $window.off(ns);
        },
        reflow: function(){
          reflowEvent();
        },
        setHeaderHeight: function(){
          setHeaderHeight();
        },
        getFloatContainer: function(){
          return $floatContainer;
        },
        getRowGroups: function(){
          if(headerFloated){
            return $floatContainer.find("thead").add($table.find("tbody,tfoot"));
          } else {
            return $table.find("thead,tbody,tfoot");
          }
        }
      });
    });
    return this;
  };
})(jQuery);
/* jQuery.floatThead.utils - http://mkoryak.github.io/floatThead/ - Copyright (c) 2012 - 2014 Misha Koryak
 * License: MIT
 *
 * This file is required if you do not use underscore in your project and you want to use floatThead.
 * It contains functions from underscore that the plugin uses.
 *
 * YOU DON'T NEED TO INCLUDE THIS IF YOU ALREADY INCLUDE UNDERSCORE!
 *
 */

(function($){

  $.floatThead = $.floatThead || {};

  $.floatThead._  = window._ || (function(){
    var that = {};
    var hasOwnProperty = Object.prototype.hasOwnProperty, isThings = ['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp'];
    that.has = function(obj, key) {
      return hasOwnProperty.call(obj, key);
    };
    that.keys = function(obj) {
      if (obj !== Object(obj)) throw new TypeError('Invalid object');
      var keys = [];
      for (var key in obj) if (that.has(obj, key)) keys.push(key);
      return keys;
    };
    var idCounter = 0;
    that.uniqueId = function(prefix) {
      var id = ++idCounter + '';
      return prefix ? prefix + id : id;
    };
    $.each(isThings, function(){
      var name = this;
      that['is' + name] = function(obj) {
        return Object.prototype.toString.call(obj) == '[object ' + name + ']';
      };
    });
    that.debounce = function(func, wait, immediate) {
      var timeout, args, context, timestamp, result;
      return function() {
        context = this;
        args = arguments;
        timestamp = new Date();
        var later = function() {
          var last = (new Date()) - timestamp;
          if (last < wait) {
            timeout = setTimeout(later, wait - last);
          } else {
            timeout = null;
            if (!immediate) result = func.apply(context, args);
          }
        };
        var callNow = immediate && !timeout;
        if (!timeout) {
          timeout = setTimeout(later, wait);
        }
        if (callNow) result = func.apply(context, args);
        return result;
      };
    };
    return that;
  })();
})(jQuery);

