package edu.illinois.cs.cogcomp.slm.preprocess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDoc;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDocNEW;
import edu.illinois.cs.cogcomp.slm.util.IOManager;
import edu.illinois.cs.cogcomp.slm.util.MyFunc;

public class SRLChain {
	private static ArrayList<String> connectives = null;
	
	private static void generateFromSRL(View srl, BufferedWriter bw) throws Exception {
		if (srl == null) {
			bw.append("\n");
			return ;
		}
		ArrayList<String> chain = new ArrayList<String>();
		ArrayList<Integer> start = new ArrayList<Integer>();
		ArrayList<Integer> end = new ArrayList<Integer>();
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("Predicate")) {
				// ignore
				if (isIgnoreVerb(c)) {
					continue;
				}
				// augment
				String predicate = augmentVerb(c);
				chain.add(predicate);
				start.add(c.getStartSpan());
				end.add(c.getEndSpan());
			}	
		}
		if (chain.size() == 0) {
			bw.append("\n");
			return ;
		}
		// check compound (gap <= 1)
		for (int j = 0; j < chain.size() - 1; j++) {
			if (start.get(j+1) - end.get(j) <= 1) {
				//System.out.println("Compound!!! " + chain.get(j) + "\t" + chain.get(j+1));
				String str = chain.get(j) + "#" + chain.get(j+1);
				chain.set(j, str);
				end.set(j, end.get(j+1));
				chain.remove(j+1);
				start.remove(j+1);
				end.remove(j+1);
				j--;
			}
		}
		// add discourse
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("AM-DIS") && isConnective(c.getSurfaceForm())) {
				//System.out.println(c.getSurfaceForm()+"\t"+c.getStartSpan()+"\t"+c.getEndSpan());
				//System.out.println(start);
				//System.out.println(end);
				int a = 0;
				int b = 0;
				for (int j = 0; j < chain.size(); j++) {
					if (j == 0) a = 0;
					else a = end.get(j-1);
					b = start.get(j);
					if (c.getStartSpan() >= a && c.getEndSpan() <= b) {
						chain.add(""); start.add(0); end.add(0);
						for (int k = chain.size() - 1; k >= j+1; k--) {
							chain.set(k, chain.get(k-1));
							start.set(k, start.get(k-1));
							end.set(k, end.get(k-1));
						}
						chain.set(j, "c:" + c.getSurfaceForm().replaceAll("\n", " ").toLowerCase());
						start.set(j, c.getStartSpan());
						end.set(j, c.getEndSpan());
						break;
					}
				}
				if (c.getStartSpan() >= end.get(chain.size()-1)) {
					chain.add("c:" + c.getSurfaceForm().replaceAll("\n", " ").toLowerCase());
					start.add(c.getStartSpan());
					end.add(c.getEndSpan());
				}
			}
		}
		for (int j = 0; j < chain.size(); j++) {
			bw.append(chain.get(j) + ",");
		}
		bw.append("\n");
	}

	private static void generateFormNYTDocNEW(AnnotatedNYTDocNEW doc, BufferedWriter bw) throws Exception {
		if (doc.srltas.size() == 0) {
			return ;
		}
		bw.append("Doc: " + doc.doc.getGuid() + "\n");
		for (int i = 0; i < doc.srltas.size(); i++) {
			View srl = doc.srltas.get(i).getView(ViewNames.SRL_VERB);
			generateFromSRL(srl, bw);
		}
	}
	
	private static void generateFormNYTDoc(AnnotatedNYTDoc doc, BufferedWriter bw) throws Exception {
		TextAnnotation ta = doc.ta;
		if (!ta.hasView(ViewNames.SRL_VERB)) {
			return ;
		}
		bw.append("Doc: " + doc.doc.getGuid() + "\n");
		for (int i = 0; i < ta.getNumberOfSentences(); i++) {
			View srl = null;
			try {
				srl = ta.getSentence(i).getView(ViewNames.SRL_VERB);
			}
			catch(Exception e) {
				bw.append("\n");
				continue;
			}
			generateFromSRL(srl, bw);
		}
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
				res = res + "[" + r.getTarget().getTokenizedSurfaceForm().replaceAll("\n", " ") + "]";
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

	private static boolean isConnective(String str) {
		if (connectives == null) {
			connectives = IOManager.readLines("data/functionwords/EnglishConjunctions.txt");
		}
		if (connectives.contains(str.toLowerCase())) {
			return true;
		}
		return false;
	}

	public static void generateAll() throws Exception {
		ArrayList<String> lines = IOManager.readLines("data/anotation_filelist_ser.txt");
		ArrayList<String> processed_docs = IOManager.readLines("data/processedDoc.txt");
		BufferedWriter bw = IOManager.openWriter(Parameters.srl_chain_file);
		for (int i = 0; i < lines.size(); i++) {
			System.out.print(i + "\t");
			if (processed_docs.contains(lines.get(i).substring(0, lines.get(i).length() - 4))) {
				System.out.println("processed!");
				continue;
			}
			String fname = Parameters.nyt_annotation_path + lines.get(i);
			
			Object obj = MyFunc.loadObject(fname);
			
			AnnotatedNYTDoc doc_1 = null;
			AnnotatedNYTDocNEW doc_2 = null;
			try {
				doc_2 = (AnnotatedNYTDocNEW) obj;
			} catch (Exception e) {
				doc_1 = (AnnotatedNYTDoc) obj;
			}
			if (doc_1 != null) {
				System.out.println("This is original doc.");
				generateFormNYTDoc(doc_1, bw);
			} else {
				if (doc_2 != null) {
					System.out.println();
					generateFormNYTDocNEW(doc_2, bw);
				}
			}
		}
		bw.close();
	}
}
