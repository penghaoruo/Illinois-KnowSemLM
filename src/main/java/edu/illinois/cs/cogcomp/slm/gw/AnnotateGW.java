package edu.illinois.cs.cogcomp.slm.gw;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.util.IOManager;
import edu.illinois.cs.cogcomp.slm.util.MyCuratorClient;

public class AnnotateGW {
	public static void annotate(String filename) throws IOException {
		int index = 0;
		BufferedReader bufReader = IOManager.openReader(filename);
		String line;
		String id;
		while ((line = bufReader.readLine()) != null) {
			// wait for doc
			if (!line.startsWith("<DOC ")) continue;
			id = getID(line);
			// ignore non-story
			if (line.indexOf("type=\"story") < 0)
				while ((line = bufReader.readLine()) != null)
					if (line.startsWith("</DOC>"))
						break;
			// skip to text
			while (!line.startsWith("<TEXT>"))
				if ((line = bufReader.readLine()) == null)
					return;
			if (line.startsWith("<TEXT>")) {
				StringBuilder content = new StringBuilder();
				boolean continuing = false;
				while ((line = bufReader.readLine()) != null) {
					if (line.startsWith("</TEXT>")) {
						if (content.length() == 0) break;
						String cleanText = StringCleanup.normalizeToAscii(content.toString().trim());
						index++;
						System.out.println("Doc Num:" + index + "\t" + id);
						
						boolean flag = true;
						TextAnnotation ta = null;
						try {
							ta = MyCuratorClient.client.createBasicTextAnnotation("gw", "", cleanText);
						} catch (AnnotatorException e1) {
							break ;
						}
						for (String viewName : MyCuratorClient.rm.getCommaSeparatedValues("viewsToAdd")) {
							try {
								MyCuratorClient.client.addView(ta, viewName);
							} catch (AnnotatorException e) {
								flag = false;
								break ;
							}
						}
						if (!flag) break;
						AnnotatedGWDoc doc = new AnnotatedGWDoc(cleanText, id, ta);
						doc.save();
						System.out.println("Success!");
						
			            break;
			        }
			        if (line.startsWith("<P>")) {
			        	continuing = false;
			            content.append("\n");
			        } 
			        else {
			        	if (!line.startsWith("</P>")) {
			        		if (continuing)
			        			content.append(' ');
			                else 
			                	continuing = true;
			                content.append(line.indexOf('&') >= 0 ? removeEscapes(line) : line);
			            }
			        }
				}
			}
		}
	}
			
	private static String getID(String str) {
		int a = str.indexOf("\"");
		int b = str.indexOf("\"", a + 1);
		return str.substring(a + 1, b);
	}

	private static String removeEscapes(String line) {
		return line.replaceAll("&(amp|AMP);","&");
	}
	
	public static void annotateAll() throws Exception {
		MyCuratorClient.init();
		ArrayList<String> files = IOManager.readLines(Parameters.gw_filelist);
		for (int i = 0; i < 1; i++) {
			String filename = files.get(i);
			File f = new File(Parameters.gw_annotation_path + filename + ".ser");
			if (!f.exists()) {
				annotate(filename);
			}
		}	
	}
}
