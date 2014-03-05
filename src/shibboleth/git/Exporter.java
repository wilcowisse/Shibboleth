package shibboleth.git;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import shibboleth.model.UserChunk;
import shibboleth.util.BlameUtil;

/**
 * Export the code from a repo of a sorted list of chunks.
 * 
 * @author Wilco Wisse
 *
 */
public class Exporter {
	private File exportPath, clonePath;
	private List<String> blackList;
	
	/**
	 * Construct an exporter
	 * @param exportPath The directory to export to
	 * @param clonePath The directory where the cloned repos can be found.
	 * @param pathBlackList paths which must not be exported.
	 */
	public Exporter(File exportPath, File clonePath, List<String> blackList){
		this.exportPath = exportPath;
		this.clonePath = clonePath;
	}
	
	/**
	 * Copies the file fragments of one file to the export location.
	 * @param fileChunks All the chunks of one file.
	 * @throws IOException
	 */
	public void export(List<UserChunk> fileChunks, boolean fragmented) throws IOException{
		if(fileChunks.isEmpty())
			return;
		
		UserChunk first = fileChunks.get(0);
		
		if(blackList.contains(first.file.filePath))
			return;
		
		if(fileChunks.size() == 1) { // file completely written in one commit...
			System.out.println(" Blacklisted " + first.file.filePath + "(One commit)");
			return;
		}
		
		for(String suspect : suspectlist){
			if(first.file.filePath.contains(suspect) && fileChunks.size()<3)
				return;
		}
		
		List<List<UserChunk>> formattedChunks = BlameUtil.format(fileChunks);
		
		
		if(formattedChunks.size() > 0) { // can be 0 when a file is completely written by an unknown user...
			String source="clones" + "/" + 
					first.committer.repo.replace('/', '-') + "/" +
					first.file.filePath;
			
			String copyDest = "export" + "/" +
					first.committer.repo.replace('/', '-') + "-" +
					first.file.filePath.replace('/', '-');
			File sourceFile = new File(source);
			File copyDestFile = new File(copyDest);
			
			
			if(fragmented){
				copy(sourceFile, copyDestFile);
				int i = 0;
				for(List<UserChunk> formattedChunk : formattedChunks){
					write(formattedChunk, i);
					i++;
				}
				writeLog(formattedChunks, copyDestFile);
			}
			else if(formattedChunks.size() == 1 && BlameUtil.isContiguous(fileChunks)) {
				copy(sourceFile, copyDestFile);
			}
			
		}

	}
	
	private void write(List<UserChunk> chunks, int pos) throws IOException{
		if(chunks.size() < 1)
			return;
		
		UserChunk first = chunks.get(0);
		
		String source = 
				first.committer.repo.replace('/', '-') + "/" +
				first.file.filePath;
		
		String target =
				first.committer.repo.replace('/', '-') + "-" +
				first.file.filePath.replace('/', '-') + 
				"."+pos+"."+ first.getUser().login;
						
		File sourceFile = new File(clonePath, source);
		File targetFile = new File(exportPath, target);
		
		byte[] encoded = Files.readAllBytes(Paths.get(sourceFile.getPath()));
		String fileContents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		String[] fileLines = fileContents.split("\\r?\\n");
		String[] slice = Arrays.copyOfRange(fileLines, chunks.get(0).start, chunks.get(chunks.size()-1).end+1);
		
		copy(slice, targetFile);

	}

	private void copy(String[] lines, File dest) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(dest, false));
        for(int i = 0; i<lines.length-1; i++){
        	String line = lines[i] == null ? "" : lines[i];
        	bw.write(line);
            bw.newLine();
        }
        if(lines.length>0){
        	String line = lines[lines.length-1] == null ? "" : lines[lines.length-1];
        	bw.write(line);
        }
        bw.close();

	}
	
	/**
	 * Copy file
	 * @param source The source uri
	 * @param dest The target uri
	 * @throws IOException
	 */
	public void copy(File source, File dest) throws IOException{
		Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	/**
	 * Writes log file with authorship info
	 * @param formattedChunks
	 * @param copyDestFile
	 * @throws IOException
	 */
	public void writeLog(List<List<UserChunk>> formattedChunks, File copyDestFile) throws IOException{
		File indexFile = new File("export", "similarity.log");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile, true));
	    
		for(List<UserChunk> chunks : formattedChunks){
			 int start = chunks.get(0).start;
			 int end = chunks.get(chunks.size()-1).end;
			 String user = chunks.get(0).getUser().login;
			 bw.write(copyDestFile.getName() + "\t" + user + "\t" + start + "\t" + end);
	         bw.newLine();
		}
	    bw.close();
	}
	
	
	private String[] suspectlist = {
			  "glow/"				// http://www.bbc.co.uk/glow/download/
			, "dojo" 				// http://dojotoolkit.org/download/
			, "Acc.DC.API"			// http://whatsock.com/
			, "midori"				// http://www.midorijs.com/
			, "jquery-1."			// http://jquery.com/download/
			, "jquery.js"			// idem
			, "jquery.mobile"		// http://jquerymobile.com/
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
			, "codemirror"			// http://codemirror.net/
	};
	

}
