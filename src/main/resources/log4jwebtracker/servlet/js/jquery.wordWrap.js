// jQuery Word-wrap Plugin
//
// Version 1.0
//
// Cory S.N. LaViska
// A Beautiful Site (http://abeautifulsite.net/)
// 25 December 2009
//
// Visit http://abeautifulsite.net/2009/12/jquery-word-wrap-plugin/ for more information
//
// Usage: $('TEXTAREA').wordWrap([on|off])
//
// TERMS OF USE
// 
// This plugin is dual-licensed under the GNU General Public License and the MIT License and
// is copyright A Beautiful Site, LLC. 
//
(function() {
	
	jQuery.fn.wordWrap = function(action) {
		
		if( action != 'on' && action != 'off' ) action = 'on';
		
		jQuery(this).each( function() {
			
			var el = jQuery(this);
			
			switch( action ) {
				
				// Enables word-wrap on the selected elements
				case 'on':
					
					if( jQuery.browser.msie ) {
						el.attr('wrap', 'soft');
					} else {
						// We handle the value separately, because Firefox seems to clone the original value
						var text = el.val();
						el.clone(true).attr('wrap', 'on').val(text).insertAfter(el);
						el.remove();
					}
					
				break;
				
				// Disables word-wrap on the selected elements
				case 'off':
					
					if( jQuery.browser.msie ) {
						el.attr('wrap', 'off');
					} else {
						// We handle the value separately, because Firefox seems to clone the original value
						var text = el.val();
						el.clone(true).attr('wrap', 'off').val(text).insertAfter(el);
						el.remove();
					}
					
				break;
				
			}
			
		});
		
		return jQuery(this);
		
	}
  
})();