package edu.illinois.cs.cogcomp.slm.preprocess;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDocAugument;
import edu.illinois.cs.cogcomp.slm.util.IOManager;

public class SRLCorefChain {
	public static void generate(String filename, BufferedWriter bw) throws IOException {
		AnnotatedNYTDocAugument doc = null;
		try {
			doc = AnnotatedNYTDocAugument.load(filename);
		} catch (Exception e) {
			return ;
		}
		if (doc == null) {
			return ;
		}
		
		TextAnnotation ta = doc.corefta;
		if (ta == null || !ta.hasView("PRED_COREF_VIEW")) {
			return ;
		}
		View coref = null;
		try {
			coref = ta.getView("PRED_COREF_VIEW");
		}
		catch(Exception e) {
			return ;
		}
		if (coref == null) {
			return ;
		}
		if (coref.getConstituents().size() <= 1) {
			return ;
		}
		ArrayList<TextAnnotation> srltas = doc.srltas;
		if (srltas == null) {
			return ;
		}
		if (ta.getNumberOfSentences() != srltas.size()) {
			return ;
		}
		
		String id = filename.substring(filename.lastIndexOf("/") + 1, filename.length() - 3);
		bw.write("Doc: " + id + "\n");
		
		// sort mentions
		ArrayList<Constituent> mentions = (ArrayList<Constituent>) coref.getConstituents();
		for (int i = 0; i < mentions.size() - 1; i++) {
			for (int j = i + 1; j < mentions.size(); j++) {
				boolean flag = false;
				if (Integer.parseInt(mentions.get(i).getLabel()) > Integer.parseInt(mentions.get(j).getLabel())) {
					flag = true;
				}
				if ((Integer.parseInt(mentions.get(i).getLabel()) == Integer.parseInt(mentions.get(j).getLabel())) &&
					(mentions.get(i).getStartSpan() > mentions.get(j).getStartSpan())) {
					flag = true;
				}
				if (flag) {
					Constituent m = mentions.get(i);
					mentions.set(i, mentions.get(j));
					mentions.set(j, m);
				}
			}
		}
		
		ArrayList<Constituent> corefChain = new ArrayList<Constituent>();
		corefChain.add(mentions.get(0));
		String clusterId = mentions.get(0).getLabel();
		for (int i = 1; i < mentions.size(); i++) {
			Constituent m = mentions.get(i);
			if (m.getLabel().equals(clusterId)) {
				corefChain.add(m);
			}
			else {
				handleCluster(corefChain, bw, srltas);
				corefChain.clear();
				corefChain.add(m);
				clusterId = m.getLabel();
			}
		}
		handleCluster(corefChain, bw, srltas);
	}

	private static void handleCluster(ArrayList<Constituent> corefChain, BufferedWriter bw, ArrayList<TextAnnotation> srltas) throws IOException {
		if (corefChain.size() <= 1) {
			return ;
		}
		
		ArrayList<String> strs = new ArrayList<String>();
		String type = corefChain.get(0).getAttribute("EntityType");
		Constituent prev = null;
		for (Constituent m : corefChain) {
			int senId = m.getSentenceId();
			TextAnnotation ta_srl = srltas.get(senId);
			if (ta_srl == null || !ta_srl.hasView(ViewNames.SRL_VERB)) {
				return ;
			}
			View srl = null;
			try {
				srl = ta_srl.getView(ViewNames.SRL_VERB);
			}
			catch(Exception e) {
				return ;
			}
			if (srl == null) {
				return ;
			}
			
			Constituent arg = getSRLConstituentsCovering(srl, m);
			if (arg == null) {
				continue;
			}
			
			Constituent predicate = getPredicate(arg);
			String argLabel = getArgLabel(arg);
			
			if (predicate == null || argLabel.equals("")) {
				continue;
			}
			if (isIgnoreVerb(predicate)) {
				continue;
			}
			if (prev == null) {
				prev = predicate;
			}
			else {
				if (predicate.getStartSpan() == prev.getStartSpan() && predicate.getEndSpan() == prev.getEndSpan()) {
					continue;
				}
			}
			String str = augmentVerbCompound(predicate, srl) + "!" + argLabel;
			//System.out.println(m.getSurfaceForm().replaceAll("\n", " ") + "\t" + m.getLabel() + "\t" + str);
			strs.add(str);
		}
		if (strs.size() >= 2) {
			bw.write(type + "\t");
			for (String str : strs) {
				bw.write(str + ",");
			}
			bw.write("\n");
		}
	}

	private static Constituent getSRLConstituentsCovering(View srl, Constituent m) {
		String head = m.getTextAnnotation().getToken(Integer.parseInt(m.getAttribute("MentionHeadStart")));
		for (Constituent c : srl.getConstituents()) {
			if (c.getSurfaceForm().equals(head)) {
				return c;
			}
		}
		return null;
	}

	private static String augmentVerbCompound(Constituent predicate, View srl) {
		String str = augmentVerb(predicate);
		Constituent c = predicate;
		while (true) {
			boolean flag = false;
			List<Constituent> cc = srl.getConstituentsCoveringSpan(c.getEndSpan(), c.getEndSpan() + 2);
			for (Constituent p : cc) {
				if (p.getLabel().equals("Predicate")) {
					if (!isIgnoreVerb(p)) {
						str = str + "#" + augmentVerb(p);
						c = p;
					}
				}
			}
			if (!flag) {
				break;
			}
		}
		return str;
	}

	private static String getArgLabel(Constituent arg) {
		String label = "";
		while (!arg.getLabel().equals("Predicate")) {
			label = arg.getLabel();
			arg = arg.getIncomingRelations().get(0).getSource();
		}
		return label;
	}

	private static Constituent getPredicate(Constituent arg) {
		while (!arg.getLabel().equals("Predicate")) {
			arg = arg.getIncomingRelations().get(0).getSource();
		}
		return arg;
	}

	private static boolean isIgnoreVerb(Constituent c) {
		String token = c.getAttribute("predicate");
		if (token.equals("be") || token.equals("do") || token.equals("have") || token.equals("can") || token.equals("may") || token.equals("dare") || token.equals("must")
				|| token.equals("ought") || token.equals("shall") || token.equals("will") || token.equals("may")) {
			return true;
		}
		return false;
	}
	
	private static String augmentVerb(Constituent c) {
		String res = regularize(c.getAttribute("predicate")) + "." + c.getAttribute("SenseNumber");
		List<Relation> rels = c.getOutgoingRelations();
		for (Relation r : rels) {
			String label = r.getTarget().getLabel();
			if (label.equals("AM-NEG")) {
				res = res + "(not)";
			}
			if (label.equals("C-V")) {
				res = res + "[" + r.getTarget().getTokenizedSurfaceForm() + "]";
			}
		}
		return res;
	}
	
	private static String regularize(String str) {
		if (str.startsWith("\'")) {
			str = str.substring(1, str.length());
		}
		return str;
	}

	public static void generateAll() throws Exception {
		ArrayList<String> lines = IOManager.readLines("data/anotation_filelist_ta.txt");
		ArrayList<String> processed_docs = IOManager.readLines("data/processedDoc.txt");
		BufferedWriter bw = IOManager.openWriter("data/srlCorefChains_1.txt");
		for (int i = 0; i < lines.size(); i++) {
			System.out.println(i);
			if (processed_docs.contains(lines.get(i).substring(0, lines.get(i).length() - 3))) {
				continue;
			}
			String fname = Parameters.nyt_annotation_ta + lines.get(i);
			generate(fname, bw);
		}
		bw.close();
	}
}