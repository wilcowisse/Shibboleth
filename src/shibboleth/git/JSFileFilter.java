package shibboleth.git;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * Class to filter javascript file in a git repository.
 * @author Wilco Wisse
 *
 */
public class JSFileFilter extends TreeFilter {

	private String[] pathContainsBlacklist = {
			  "glow/"				// http://www.bbc.co.uk/glow/download/
			, "dojo" 				// http://dojotoolkit.org/download/
			, "Acc.DC.API"			// http://whatsock.com/
			, "midori"				// http://www.midorijs.com/
			, "jquery-1."			// http://jquery.com/download/
			, "jquery.js"			// idem
			, "mootools"			// http://mootools.net/download
			, "prototype"			// http://prototypejs.org/download/
			, "yui"					// http://yuilibrary.com/yui/quick-start/
			, "dhtmlx"				// http://dhtmlx.com/docs/download.shtml
			, "/ext"				// http://www.sencha.com/products/extjs/
			, "vc.fx"				// http://intelligentexpert.net/getting-started
			, "jquery-ui"			// http://jqueryui.com/
			, "q-3."				// http://qooxdoo.org/downloads
			, "q-2."				// http://qooxdoo.org/downloads
			, "/q.js"				// idem
			, "scriptaculous-js"	// http://script.aculo.us/downloads
			, "d3.js"				// http://d3js.org/
			, "d3."					// idem 
			, "jit.js"				// http://philogb.github.io/jit/
			, "kinetic"				// http://kineticjs.com/
			, "processing-"			// http://processingjs.org/download/
			, "raphael.js"			// http://raphaeljs.com/
			, "swfobject"			// http://code.google.com/p/swfobject/downloads/list		
			, "three.js"			// http://threejs.org/
			, "threejs"				// http://threejs.org/
			, "qunit.js"			// http://qunitjs.com/
			, "qunit-"				// http://qunitjs.com/
			, "angular.js" 			// http://www.angularjs.org
			, "backbone.js" 		// http://backbonejs.org
			, "chaplin.js"			// http://chaplinjs.org
			, "ember.js"			// http://emberjs.com/guides/getting-started/obtaining-emberjs-and-dependencies
			, "ember-data.js"		// idem
			, "handlebars"			// idem
			, "knockout"			// http://knockoutjs.com
			, "funcjs.js"			// http://funcjs.webege.com
			, "joose.js"			// http://code.google.com/p/joose-js/downloads/list
			, "php.js"				// http://jsphp.co/javascript/php/page/home
			, "MochiKit.js"			// http://mochi.github.io/mochikit/index.html
			, "socket.io.js"		// http://socket.io
			, "underscore.js"		// http://underscorejs.org
			, "mustache.js"			// http://mustache.github.io
			, "bootstrap"			// http://getbootstrap.com/
			, "foundation"			// http://foundation.zurb.com/develop/download.html
			, "boot.js"				// http://jasmine.github.io
			, "custom_equality.js"	// http://jasmine.github.io
			, "custom_matcher.js"	// http://jasmine.github.io
			, "introduction.js"		// http://jasmine.github.io
			, "jasmine"				// http://jasmine.github.io
			, "unit.js"				// http://unitjs.com
			, "jsunit"				// https://github.com/pivotal/jsunit
			, "modernizr"			// http://modernizr.com
			, "xui"					// http://xuijs.com/downloads
			, "zepto.js"			// http://zeptojs.com/#download
			, "processing-"			// http://processingjs.org/download/
			, "basket.js"			// http://addyosmani.github.io/basket.js/
			, "hogan.js"			// http://twitter.github.io/hogan.js/
			, "lightbox"			// http://lokeshdhakar.com/projects/lightbox2/
			, "phaser"				// https://github.com/photonstorm/phaser
			, "require.js"			// http://lab.hakim.se/reveal-js
			, "reveal.js"			// http://lab.hakim.se/reveal-js
			, "webix.js"			// http://webix.com/download-webix-gpl
			, "right.js"			// http://rightjs.org/download
			, "right-"				// http://rightjs.org/download
			, "bonsai"				// https://github.com/uxebu/bonsai/downloads
			, "fabric.js"			// http://fabricjs.com/
			, "Gruntfile.js"		// Grunt.js
			, "prettify.js"			// code.google.com/p/google-code-prettify/
			, "ace"					// http://ace.c9.io/
			, "test"				// no test suites
			, "slides"				// no slides
			
	};
	
	
	@Override
	public TreeFilter clone() {
		return this;
	}

	@Override
	public boolean include(TreeWalk walker) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if(walker.isSubtree()){ // this is a dir
			String folderName = walker.getNameString();
			if(folderName.contains("lib") 
			|| folderName.contains("include") 
			|| folderName.contains("party")
			|| folderName.contains("vendor"))
			{
				System.out.println(" Blacklisted: " + walker.getPathString());
				return false;
			}
			else{
				return true;
			}
		}
		else{
			String fileName = walker.getNameString();
			String pathString = walker.getPathString();
			
			if(!fileName.endsWith(".js") || fileName.endsWith("min.js")){
				return false;
			}
			else{
				boolean result =  true;
				for(String lib : pathContainsBlacklist){
					if(pathString.contains(lib)){
						System.out.println(" Blacklisted: " + pathString);
						result=false;
						break;
					}
				}
				return result;
			}
		}
	}

	@Override
	public boolean shouldBeRecursive() {
		return false;
	}

}
