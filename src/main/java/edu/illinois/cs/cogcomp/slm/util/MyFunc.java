package edu.illinois.cs.cogcomp.slm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDoc;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDocAugument;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDocNEW;

public class MyFunc {
	
	public static void moveAllFiles(String path) throws IOException {
	    File directory = new File(path);
	    
	    File[] fList = directory.listFiles();
	    if (fList == null) {
	    	return ;
	    }
	    for (File file : fList) {
	        if (file.isFile()) {
	        	if (file.getName().endsWith("ta")) {
	        		file.renameTo(new File(Parameters.nyt_annotation_ta + file.getName()));
	        	}
	 
	        } else if (file.isDirectory()) {
	        	moveAllFiles(file.getAbsolutePath());
	        }
	    }
	}
	
	public static void printFileList(String path, String fname) throws IOException {
		int index = 0;
	    File directory = new File(path);
	    BufferedWriter bw = null;
	    bw = IOManager.openWriter(fname);
	    File[] fList = directory.listFiles();
	    for (File file : fList) {
	        bw.write(file.getName() + "\n");
	        index++;
	    }
	    bw.close();
	    System.out.println("Num of Files: " + index);
	}
	
	public static AnnotatedNYTDocNEW load(String filename) throws Exception {
		FileInputStream fileIn = new FileInputStream(filename);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		AnnotatedNYTDocNEW e = (AnnotatedNYTDocNEW) in.readObject();
		in.close();
		fileIn.close();
		return e;
	}
	
	public static Object loadObject(String filename) throws Exception {
		FileInputStream fileIn = new FileInputStream(filename);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Object e = in.readObject();
		in.close();
		fileIn.close();
		return e;
	}
	
	public static void save(AnnotatedNYTDocAugument doc, String fname) throws IOException {			
		String filename = Parameters.nyt_annotation_ta + fname + ".ta";
		FileOutputStream fileOut = new FileOutputStream(filename);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(doc);
		out.close();
		fileOut.close();
	}
	
	public static void computeStat(String fname) throws IOException {
		ArrayList<String> lines = IOManager.readLines(fname);
		HashMap<String, Integer> frames = new HashMap<String, Integer>();
		
		BufferedWriter bw = IOManager.openWriter("data/processedDoc.txt");
		
		double docNum = 0;
		double senNum = 0;
		double chainNum = 0;
		double frameNum = 0;
		for (String line : lines) {
			if (line.startsWith("Doc:") || line.length() == 0) {
				if (line.startsWith("Doc:")) {
					docNum += 1;
					bw.write(line.substring(5, line.length()) + "\n");
					if (docNum % 10000 == 0) {
						System.out.println(docNum);
					}
				}
				continue;
			}
			if (line.contains("\t")) {
				line = line.substring(line.indexOf("\t")+1, line.length());
			}
			String[] strs = line.split(",");
			if (strs.length > 0) {
				chainNum++;
			}
			frameNum += strs.length -1;
			for (int i = 0; i < strs.length - 1; i++) {
				if (!frames.containsKey(strs[i])) {
					frames.put(strs[i], 1);
				}
				else {
					int k = frames.get(strs[i]) + 1;
					frames.put(strs[i], k);
				}
			}
		}
		senNum = lines.size() - docNum;
		System.out.println("Doc:\t" + docNum);
		System.out.println("Frame:\t" + frameNum);
		System.out.println("Unique Frame:\t" + frames.size());
		System.out.println("AVG Sen Per Doc:\t" + (senNum / docNum));
		System.out.println("AVG Frame Per Doc:\t" + (frameNum / docNum));
		System.out.println("AVG Frame Per Chain:\t" + (frameNum / chainNum));
		bw.close();
		
		BufferedWriter bw_out = IOManager.openWriter("data/frameCounts-srlcoref.txt");
		frames = (HashMap<String, Integer>) sortByValue(frames);
		Iterator<Entry<String, Integer>> it = frames.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Integer> pairs = (Entry<String, Integer>)it.next();
			bw_out.write(pairs.getValue() + "\t" + pairs.getKey() + "\n");
		}
		bw_out.close();
		
		int threshold = 20;
		int res_size = 0;
		BufferedWriter bw1 = IOManager.openWriter("data/frame-signle-srlcoref.txt");
		BufferedWriter bw2 = IOManager.openWriter("data/frame-compound-srlcoref.txt");
		bw1.write("period" + "\n");
		SortedSet<String> keys = new TreeSet<String>(frames.keySet());
		for (String key : keys) { 
			if (frames.get(key) < threshold) {
				continue;
			}
			res_size += frames.get(key);
			if (!key.contains("#")) {
				bw1.write(key + "\n");
			}
			else {
				bw2.write(key + "\n");
			}
		}
		bw1.close();bw2.close();
		System.out.println("Filtered Frame:\t" + res_size);
	}
	
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map) {
    	List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    	Collections.sort( 
    		list, new Comparator<Map.Entry<K, V>>() {
    			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ) {
    				return (o1.getValue()).compareTo( o2.getValue() );
    			}
    		} 
    	);
    	Map<K, V> result = new LinkedHashMap<K, V>();
    	for (Map.Entry<K, V> entry : list)	{
    		result.put( entry.getKey(), entry.getValue() );
    	}
    	return result;
    }
    
    public static void annotateSRL() throws Exception {
    	ArrayList<String> lines = IOManager.readLines("srl-peng.txt");
    	BufferedWriter bw = IOManager.openWriter("srl-peng-frame_tmp_3.txt");
    	String standard_query = "mechanics";
    	
    	MyCuratorClient.init();
    	Integer index = 0;
    	for (String line : lines) {
    		System.out.println(index);
    		String[] strs = line.split("\t");
    		String query = strs[0];
    		String sen = strs[1];
    		sen = sen.replaceAll(query, standard_query);
    		sen = sen.substring(0, 1).toUpperCase() + sen.substring(1, sen.length());
    		
    		TextAnnotation ta = null;
    		try {
    			ta = MyCuratorClient.client.createAnnotatedTextAnnotation("srl-peng", index.toString(), sen);
    			MyCuratorClient.client.addView(ta, ViewNames.POS);
    			MyCuratorClient.client.addView(ta, ViewNames.SRL_VERB);
    			MyCuratorClient.client.addView(ta, ViewNames.LEMMA);
    		} catch (Exception e) {
    			System.out.println("Annotation Error!");
    			bw.write("\n");
    			continue;
    		}
    		
    		//String res = analyzeSRL(ta, standard_query);
    		String res = allPredicates(ta);
    		System.out.println(res);
    		bw.write(res + "\n");
    		
    		index++;
    	}
    	
    	bw.close();
    }
    
    private static String allPredicates(TextAnnotation ta) {
    	if (ta == null || !ta.hasView(ViewNames.SRL_VERB)) {
    		return "";
    	}
    	View srl = ta.getView(ViewNames.SRL_VERB);
    	if (srl == null) {
			return "";
		}
    	
    	String res = "";
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("Predicate")) {
				res = res + c.getAttribute("predicate") + "\t";
			}
		}
		return res;
    }

	private static String analyzeSRL(TextAnnotation ta, String query) {
		if (ta == null || !ta.hasView(ViewNames.SRL_VERB) || !ta.hasView(ViewNames.POS) || !ta.hasView(ViewNames.LEMMA)) {
			return "";
		}
		
		View tokens = ta.getView(ViewNames.TOKENS);
		View srl = ta.getView(ViewNames.SRL_VERB);
		View pos = ta.getView(ViewNames.POS);
		View lemma = ta.getView(ViewNames.LEMMA);
		
		if (srl == null || pos == null) {
			return "";
		}
		
		String res = "";
		for (Constituent token : tokens) {
			if (token.getSurfaceForm().toLowerCase().equals(query)) {
				String label = "";
				List<Constituent> list = srl.getConstituentsCovering(token);
				if (list == null || list.size() == 0) {
					continue;
				}
				Constituent c = list.get(0);
				if (c.getLabel().equals("Predicate") && c.getIncomingRelations() != null && c.getIncomingRelations().size() > 0) {
					c = c.getIncomingRelations().get(0).getSource();
				}
	
				while (!c.getLabel().equals("Predicate")) {
					label = c.getLabel();
					c = c.getIncomingRelations().get(0).getSource();
				}
				String pred = c.getAttribute("predicate");
				String le = "";
				if (isIgnoreVerb(pred)) {
					int k = tokens.getConstituentsCovering(c).get(0).getStartSpan();
					k++;
					while (k < pos.getConstituents().size()) {
						Constituent c_pos = pos.getConstituentsCoveringToken(k).get(0);
						if (c_pos.getLabel().toLowerCase().startsWith("vb")) {
							pred = c_pos.getSurfaceForm();
							le = lemma.getConstituentsCovering(c_pos).get(0).getLabel();
							break;
						}
						k++;
					}
				} else {
					pred = c.getSurfaceForm();
					le = c.getAttribute("predicate");
				}
				if (le.equals("")) {
					continue;
				}
				
				res = res + label + "#" + pred + "#" + le + "\t";
			}
		}
		return res.trim();
	}
	
	private static boolean isIgnoreVerb(String token) {
		if (token.equals("be") || token.equals("do") || token.equals("have") || token.equals("can") || token.equals("may") || token.equals("dare") || token.equals("must")
				|| token.equals("ought") || token.equals("shall") || token.equals("will") || token.equals("may")) {
			return true;
		}
		return false;
	}

	public static void sortFileByValue(String filename) throws IOException {
		String fname = filename.substring(0, filename.length() - 4) + "_sorted.txt";
		BufferedWriter bw = IOManager.openWriter(fname);
		
		HashMap<String, Double> pairs = new HashMap<String, Double>();
		ArrayList<String> lines = IOManager.readLines(filename);
		for (int i = 0; i < lines.size() / 9; i++) {
			String line = lines.get(i*9+1);
			String file = line.substring(0, line.length() - 1);
			
			line = lines.get(i*9+8);
			int p = line.lastIndexOf(":");
			int q = line.lastIndexOf("%");
			Double value = Double.parseDouble(line.substring(p + 1, q));
			
			pairs.put(file, value);
		}
		
		pairs = (HashMap<String, Double>) sortByValue(pairs);
		
		for (Entry<String, Double> entry :  pairs.entrySet()) {
			bw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
		}
		bw.close();
	}
}
