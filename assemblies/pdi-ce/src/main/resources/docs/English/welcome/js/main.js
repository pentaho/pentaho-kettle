/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

// Define an utility block for wecomePageUtils

var wpg = {

	gotoSteelwheelsSamples: function(args){

		// See if we have mantle
		if(window.top.mantle_addHandler == undefined) return;

		// Switch to browser perspective
		window.top.mantle_setPerspective('browser.perspective');

		// Wait for it to load, and switch to SW
		setTimeout(function(){
				window.top.mantle_fireEvent('GenericEvent', {
						'eventSubType': 'OpenFolderEvent',
						'stringParam': "/public/Steel Wheels"
					});
			},2000);
	}

}


$(document).ready(function() {

	// Pedro's stuff



	
    var stylePage = function(){


		///////////////////////////////////////
		//
		// highlightTag
		//
		///////////////////////////////////////

		var highlightTag = function(clazz, tag, hasHr, beforeOrAfter){

			var h1 = $(".content-area." + clazz + "  > " + tag).wrap("<div class='row'><div class='span12'></div></div>");

			// ruleH1: Append a rule to the ones that have 
			if(hasHr){
				if(beforeOrAfter == "before"){
					$(".content-area." + clazz + "  " + tag).prepend("<hr class='first'/>");
				}
				else{
					$(".content-area." + clazz + "  " + tag).append("<hr class='first'/>");
				}
			}

		}
		
		///////////////////////////////////////
		//
		// columnizeTag
		//
		///////////////////////////////////////

		var columnizeTag = function(clazz, tag, groupQuantity, isDeep, hideRuler){

			var higherRankedTags = {
				h2: 'h1',
				h3: 'h1,h2',
				h4: 'h1,h2,h3',
				h5: 'h1,h2,h3,h4',
				h6: 'h1,h2,h3,h4,h5'
			};
			var stopTag = (higherRankedTags[tag]) || "";

			var levelFn = (isDeep) ? 'find' : 'children',
					groups = [],
					currentGroup, 
					newGroup = true;
			$(".content-area." + clazz)[levelFn](tag).each(function(i,k){
				var $tag = $(this);
				newGroup = newGroup  || !($tag.parent().is( lastParent ) ) 
							|| (currentGroup.length == groupQuantity);
				if (newGroup){
					currentGroup = $();
					groups.push( currentGroup );
					lastParent = $tag.parent();
					newGroup = false;

				}
				var section = $tag.add($tag.nextUntil( tag + (stopTag ? "," + stopTag : "") ));
				newGroup = section.last().next().is(stopTag);
				section.wrapAll("<div class='contentSpan span4'></div>");
				currentGroup.push( section.parent().get(0) );
				
			});
			
			// Wrap each groupQuantity contentSpans in a row inside each content-area
			var wrapWithRow = function($sel){
				// console.log("Wrapping " + $sel.length + " objects");
				$sel.wrapAll("<div class='contentRow row'></div>");

				if(!hideRuler){
					$sel.parent("div.contentRow").before("<hr/>" );
				}
				else{
				
					// Even if we want to hide the rules, force one if theres
					// a paragraph before
					if( $sel.parent("div.contentRow").prev().get(0).nodeName.toLowerCase() == "p"){
						$sel.parent("div.contentRow").before("<hr/>" );
					}

				}
			};

			$(groups).each( function(groupIdx, currentGroup){
				wrapWithRow( $(currentGroup) );
			});

		}


		///////////////////////////////////////
		//
		// hierarchizeTag
		//
		///////////////////////////////////////

		var hierarchizeTag = function(clazz, tag){

			function hierarchize ($items, level){
				var parentClass = 'level' + (level-1),
						childClass = 'level' + level;

				if ( $items.length > 0 ){		
					$items.each(function(i,k){
						if ( (level == 0) || $(this).parents('.' + parentClass).length > 0 ){
							$(this).removeClass(parentClass).addClass(childClass);
						}
					});
					hierarchize ( $items.filter(function (){
						return $(this).hasClass(childClass);
					}), level+1 );
				}
			}
			
			hierarchize( $('.content-area.'+ clazz + ' ' + tag), 0);
		}

		///////////////////////////////////////
		//
		// buttonizeTag
		//
		///////////////////////////////////////

		var buttonizeTag = function(clazz, tag){

			var $tags = $(".content-area." + clazz + "  > " + tag);
			var buttonIds = [];
            
            // First thing - gather together the content and build the structure
            
            var buttonId;
            var $buttonTarget;
            for( var i = 0; i<$tags.length ; i++){
            
                var $t = $tags.eq(i);
                if ( $t.text().indexOf("STARTBUTTONIZE") == 0){
                    
                    buttonId = $t.text().substr(15);
                    buttonIds.push(buttonId);
                    $buttonTarget = $t.wrapAll("<div class='row'><div class='span12'><div class='buttonizeButtons buttonizeButtons"+ buttonId +"'></div></div></div>").parent();
                    $t.remove();
                    //console.log("found button group with id " + buttonId );
                    continue;
                }
                else if ($t.text().indexOf("ENDBUTTONIZE") == 0){
                    $t.remove();
					$buttonTarget.addClass("buttonizeButtons" + $buttonTarget.find("div.buttonizeButton").length);
                }
                else{
            
                    // Warp content in a div
                    $t.nextUntil(tag).wrapAll("<div class='buttonizeContent buttonizeContent"+ buttonId +"'></div>");
					//
                    // Create button in $buttonTarget and remove it
                    $buttonTarget.append("<div class='buttonizeButton'>"+ $t.text() +"</div>");
                    $t.remove();
                    
                }
            }
            
			// Now... handle the clicks!
			var handleClicks = function(buttonId){
			
				var button = $(this);
				var idx = button.prevAll(".buttonizeButton").length;
				// console.log("You have clicked on group " + buttonId + ", index " + idx );

				// Remove class selected from everywhere, add to this
				button.parent().children(".buttonizeButton").removeClass("selected");
				button.addClass("selected");

				// Show content-area
				var $contentArea = $(".buttonizeContent" + buttonId).eq(idx);
				$(".buttonizeContent" + buttonId).removeClass("selected visible");
				$contentArea.addClass("selected");
				setTimeout(function(){
						$contentArea.addClass("visible");
				},50);
			
			}

			$.each(buttonIds,function(i,buttonId){
				
				$(".buttonizeButtons"+buttonId).on('click','.buttonizeButton',function(){
					handleClicks.call(this,buttonId);
				});

				// Do a first click
				$(".buttonizeButtons"+buttonId + " > .buttonizeButton:first-child").click();


			});


		
		}

		///////////////////////////////////////
		//
		// groupByLetter
		//
		///////////////////////////////////////

		var groupListByLetter = function(clazz, tag, groupQuantity, headerTag){

			// Since the last range of letters always has less entries, we'll
			// put use groupQuantity + 1 and merge the last two

			var contents = [], headers = [];
			var $list = $(".content-area." + clazz + "  " + tag),
			$ph = $list.parent();
			$list.detach();

			// Get the headers;
			var numGroups = Math.floor(26/groupQuantity),
			endRemainder = 26 - numGroups * groupQuantity;
			for(var i = 0; i< numGroups; i++){
				var startCode = 65 + i*groupQuantity ,
				endCode = 64 + (i+1)*groupQuantity + ( ( (i+1) == numGroups ) ? endRemainder : 0 );
				headers.push( String.fromCharCode(startCode) + " - " + String.fromCharCode(endCode) );
			}

			$list.children().each(function(i,k){
					var $tag = $(this).detach();

					var letterCode = $tag.text().toLowerCase().charCodeAt(0) - 97;
					var idx = Math.floor( letterCode / groupQuantity );
					idx = ( idx < 0 || idx >= numGroups ) ? ( numGroups - 1 ) : idx;

					if(! $.isArray(contents[idx])){
						contents[idx]=[$tag];
					}
					else{
						contents[idx].push($tag);
					}	
				});

			$(headers).each( function(i,headerText){
					var $header = $('<' + headerTag + '/>').text(headerText)
					.appendTo($ph),
					$list = $('<' + tag + '/>')
					.appendTo($ph);
					$(contents[i]).each(function(j,$contributor){
							$list.append($contributor);
						});
				});


		}
		

		///////////////////////////////////////
		//
		// functionizeLinks
		//
		///////////////////////////////////////

		var functionizeLinks = function(){

			// This is where we define custom applications for the links

			$("a").each(function(n){

					var $a = $(this);
					if(!$a.attr("href")) return;

					var found = $a.attr("href").match(/.*FUNCTION_(.*)/);
					if(found && found[1]){

						// Get function name and execute it
						var fargs = found[1].split("_");
						var f = wpg[fargs.splice(0,1)[0]];
						if(f){
							// replace the href with a click
							$a.removeAttr("href");
							$a.click(function(){
									f.call(fargs);                
								});

						}

					}

				});

		}



		///////////////////////////////////////
		//
		// ACTIONS
		//
		///////////////////////////////////////

		groupListByLetter("groupListByLetterUL2", "ul", 2, "h2");
		groupListByLetter("groupListByLetterUL3", "ul", 3, "h2");
		groupListByLetter("groupListByLetterUL4", "ul", 4, "h2");
		groupListByLetter("groupListByLetterUL5", "ul", 5, "h2");
		groupListByLetter("groupListByLetterUL6", "ul", 6, "h2");
		groupListByLetter("groupListByLetterUL7", "ul", 7, "h2");
		groupListByLetter("groupListByLetterUL8", "ul", 8, "h2");

		buttonizeTag("buttonizeH6", "h6");

		columnizeTag("columnizeH2", "h2", 3);
		columnizeTag("columnizeUL", "ul", 3);
		columnizeTag("columnizeH3", "h3", 3, true, true);


		highlightTag("highlightH1", "h1", false);
		highlightTag("highlightH1WithHR", "h1", true);
		highlightTag("highlightH1WithHRBefore", "h1", true,"before");
		highlightTag("highlightH2", "h2", false);
		highlightTag("highlightH2WithHR", "h2", true);
		highlightTag("highlightH2WithHRBefore", "h2", true,"before");


		functionizeLinks();

	}

	stylePage();


	// NavigationActions
	
	// Do we need to change the way clicks work?
	
	var targetizeLinks = function(){

		$("a").each(function(){
				var $a=$(this);
				var href = $a.attr("href");

				if(href){
					$a.attr('target','_blank');

					if(href.indexOf(".pdf")>0){
						$a.addClass("pdfLink");
					}
				}

			});	

	};

	targetizeLinks();
	

	var handleClicks = function(button, content, clazz){
	
		var button = $(this);
		var idx = button.prevAll(".header-navigation-item").length;
		// console.log("You have clicked index " + idx );

		// Remove class selected from everywhere, add to this
		button.parent().children(".header-navigation-item").removeClass("selected");
		button.addClass("selected");

		// Show relevant header-container
		var headerContainer = $(".header-container").eq(idx);
		headerContainer.parent().children(".header-container").removeClass("selected");
		headerContainer.addClass("selected");
		
		// Show content-area
		var contentArea = $(".content-area").eq(idx);
		contentArea.parent().children(".content-area").removeClass("selected visible");
		contentArea.addClass("selected");
		setTimeout(function(){
				contentArea.addClass("visible");
		},50);
	
	}
	$(".header-navigation").on('click','> div',handleClicks);
	
	$('.header-navigation-item').first().click();

	
});
