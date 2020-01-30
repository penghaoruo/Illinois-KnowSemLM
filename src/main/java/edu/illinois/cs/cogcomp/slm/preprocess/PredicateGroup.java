package edu.illinois.cs.cogcomp.slm.preprocess;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.util.IOManager;

public class PredicateGroup {
	
	public static int count = 0;
	
	public static void generate(String fname) throws IOException {
		ArrayList<String> lines = IOManager.readLines(fname);
		FramesManager fm = FramesManager.getPropbankInstance();
		FrameMapping fmap = new FrameMapping();
		
		String outFile = null;
		if (fname.equals(Parameters.srl_chain_file)) {
			outFile = Parameters.srl_chain_group_file;
		}
		if (fname.equals(Parameters.srlcoref_chain_file)) {
			outFile = Parameters.srlcoref_chain_group_file;
		}
		
		int docNum = 0;
		BufferedWriter bw = IOManager.openWriter(outFile);
		for (String line : lines) {
			if (line.startsWith("Doc:") || line.length() == 0) {
				bw.write(line + "\n");
				if (line.startsWith("Doc:")) {
					docNum += 1;
				}
				if (docNum % 10000 == 0) {
					System.out.println(docNum);
				}
				continue;
			}
			if (line.contains("\t")) {
				bw.write(line.substring(0, line.indexOf("\t")+1));
				line = line.substring(line.indexOf("\t")+1, line.length());
			}
			String[] strs = line.split(",");
			ArrayList<String> frames = new ArrayList<String>();
			for (String str : strs) {
				// last one
				if (str.length() == 0) {
					continue;
				}
				// connective
				if (!str.contains(".")) {
					frames.add(str);
					continue;
				}
				String[] predicates = str.split("#");
				String res = "";
				for (String predicate : predicates) {
					if (predicate.contains("!")) {
						String arglabel = predicate.substring(predicate.indexOf("!"), predicate.length());
						predicate = predicate.substring(0, predicate.indexOf("!"));
						res = res + getFrame(predicate, fm, fmap) + arglabel + "#";
					} 
					else {
						res = res + getFrame(predicate, fm, fmap) + "#";
					}
				}
				res = res.substring(0, res.length()-1);
				frames.add(res);
			}
			for (int i = 0; i < frames.size(); i++) {
				bw.write(frames.get(i) + ",");
			}
			bw.write("\n");
		}
		bw.close();
		System.out.println("Substitute Num : " + count);
	}

	private static String removeAppendix(String str) {
		int p = str.indexOf("(");
		if (p != -1) {
			str = str.substring(0, p);
		}
		p = str.indexOf("[");
		if (p != -1) {
			str = str.substring(0, p);
		}
		return str;
	}
	
	private static String getFrame(String str, FramesManager fm, FrameMapping fmap) {
		int p = str.indexOf('.');
		if (p == -1) {
			return str;
		}
		String predicate = str.substring(0, p);
		String sense = removeAppendix(str.substring(p+1, str.length()));
		if (fm.getFrame(predicate) == null) {
			return predicate + "." + sense;
		}
		String vncls = fm.getFrame(predicate).getVerbClass(sense);
		if (vncls == null || vncls.equals("UNKNOWN")) {
			return predicate + "." + sense;
		}
		String[] strs = vncls.split(" ");
		String frame = null;
		for (String s : strs) {
			frame = fmap.getFrame(predicate, s);
			//System.out.println(predicate + "\t" + s +"\t" + frame);
			if (frame != null && !frame.equals("NA") && !frame.equals("DS")) {
				count++;
				return frame;
			}
		}
		return predicate + "." + sense;
	}
}
