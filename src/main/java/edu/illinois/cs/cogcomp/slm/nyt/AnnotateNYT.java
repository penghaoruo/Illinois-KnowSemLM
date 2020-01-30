package edu.illinois.cs.cogcomp.slm.nyt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.nlp.util.SimpleCachingPipeline;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.util.IOManager;
import edu.illinois.cs.cogcomp.slm.util.MyCuratorClient;
import edu.illinois.cs.cogcomp.slm.util.TextProcessor;

public class AnnotateNYT {
	
	static NYTCorpusDocumentParser parser = new NYTCorpusDocumentParser();
	
	public static void annotate(String file, TextProcessor client) throws IOException {
			NYTCorpusDocument nytDoc = parser.parseNYTCorpusDocumentFromFile(new File(file), false);
			if (nytDoc == null || nytDoc.body == null) {
				return ;
			}
			
			String cleanText = StringCleanup.normalizeToAscii(nytDoc.body);
			
			TextAnnotation ta = null;
			ArrayList<TextAnnotation> srltas = new ArrayList<TextAnnotation>();
			try {
				ta = client.annotator.createBasicTextAnnotation("nyt", "", cleanText);
				client.annotator.addView(ta, ViewNames.POS);
				client.annotator.addView(ta, ViewNames.NER_CONLL);
				client.annotator.addView(ta, ViewNames.LEMMA);
				client.annotator.addView(ta, ViewNames.SHALLOW_PARSE);
				for (int i = 0; i < ta.getNumberOfSentences(); i++) {
					TextAnnotation ta_tmp = client.annotator.createBasicTextAnnotation("nyt", "", ta.getSentence(i).toString());
					client.annotator.addView(ta_tmp, ViewNames.SRL_VERB);
					srltas.add(ta_tmp);
				}
				//MyCuratorClient.client.addView(ta, ViewNames.COREF);
			} catch (Exception e) {
				System.out.println(e);
				return ;
			}
			
			AnnotatedNYTDocNEW doc = new AnnotatedNYTDocNEW(nytDoc, ta, srltas);
			doc.save();
			System.out.println("Success!");
	}

        public static void annotate(String file, SimpleCachingPipeline client) throws IOException {
                        String content = IOManager.readContentNoTrim(file);
                        String cleanText = StringCleanup.normalizeToAscii(content);

                        TextAnnotation ta = null;
                        try {
                                ta = client.createBasicTextAnnotation("ace04", "", cleanText);
                                MyCuratorClient.client.addView(ta, ViewNames.SRL_VERB);
                                } catch (Exception e) {
                                    System.out.println(e);
                                    return ;
                                } 
                                System.out.println("Success!");
                        }

	public static void annotateAll() throws Exception {
		MyCuratorClient.init();
                //TextProcessor client = TextProcessor.getInstance();
		ArrayList<String> files = IOManager.readLines("ace04.txt");
		//ArrayList<String> files_exist = IOManager.readLines("data/anotation_filelist_ser.txt");
		String path = "/shared/experiments/hpeng7/results/ACE2004/Golden/Rawtext/";
                for (int i = 0; i < files.size(); i++) {
			System.out.println("Doc Num:" + i);
			String filename = path + files.get(i);
			//String f = files.get(i).substring(0, files.get(i).length() - 4) + ".ser";
			//if (!files_exist.contains(f)) {
				annotate(filename, MyCuratorClient.client);
			//}
		}	
	}
}
