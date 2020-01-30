package edu.illinois.cs.cogcomp.slm.nyt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.BIODecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.ExtendHeadsDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.decoders.MentionDecoder;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocFromTextLoader;
import edu.illinois.cs.cogcomp.lbj.coref.io.loaders.DocLoader;
import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.illinois.cs.cogcomp.lbj.coref.ir.solutions.ChainSolution;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MTypePredictor;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MDExtendHeads;
import edu.illinois.cs.cogcomp.lbj.coref.learned.MentionDetectorMyBIOHead;
import edu.illinois.cs.cogcomp.lbj.coref.main.ConfigSystem;
import edu.illinois.cs.cogcomp.lbj.coref.util.MyCuratorClient;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.util.IOManager;
import edu.illinois.cs.cogcomp.slm.util.MyFunc;

public class AugumentCoref {
	
	public static void annotateAll() {
		MyCuratorClient.init();
		ConfigSystem.parseProps(Parameters.coref_config_file);

		MentionDecoder mdDec = new ExtendHeadsDecoder(new MDExtendHeads(), new BIODecoder(new MentionDetectorMyBIOHead()));
		MTypePredictor mTyper = new MTypePredictor();
		DocLoader loader = new DocFromTextLoader(mdDec, mTyper);
		
		ArrayList<String> files = IOManager.readLines("data/anotation_filelist_ser.txt");
		ArrayList<String> files_exist = IOManager.readLines("data/anotation_filelist_ta.txt");
		for (int i = 983608; i < 1000000; i++) {
			try {
				System.out.println("Doc Num:" + i);
				String f = files.get(i).substring(0, files.get(i).length() - 4) + ".ta";
				if (files_exist.contains(f)) {
					System.out.println("Skipped!!!");
					continue;
				}
				AnnotatedNYTDocNEW d = null;
				try {
					d = MyFunc.load(Parameters.nyt_annotation_path + files.get(i));
				} catch (Exception e) {
					continue;
				}
				if (d == null) {
					continue;
				}
		
				TextAnnotation coref_ta = null;
				try {
					String cleanText = StringCleanup.normalizeToAscii(d.doc.getBody());
					Doc doc = loader.loadDoc(cleanText);
					List<Doc> docs = new ArrayList<Doc>();
					docs.add(doc);
					ChainSolution<Mention> sol = ConfigSystem.TestCoref(docs, Parameters.coref_config_file);
					coref_ta = docs.get(0).getTextAnnotation();
				} catch (Exception e) {
					System.gc();
					continue;
				}
				if (coref_ta == null) {
					System.gc();
					continue;
				}
				
				AnnotatedNYTDocAugument myTA = new AnnotatedNYTDocAugument(d.ta, coref_ta, d.srltas);
				MyFunc.save(myTA, files.get(i).substring(0, files.get(i).length() - 4));
				System.gc();
			} catch (Exception e) {
				System.gc();
				continue;
			}
			System.out.println("Succeed!!!");
		}	
	}
}
