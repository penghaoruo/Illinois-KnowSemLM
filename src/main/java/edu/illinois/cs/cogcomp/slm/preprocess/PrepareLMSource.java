package edu.illinois.cs.cogcomp.slm.preprocess;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.slm.util.IOManager;

public class PrepareLMSource {
	public static void prepareDoc(String fin, String fout) throws IOException {
		ArrayList<String> lines = IOManager.readLines(fin);
		ArrayList<String> lexicons = loadLexicons();
		BufferedWriter bw = IOManager.openWriter(fout);
		
		double docNum = 0;
		double chainNum = 0;
		double frameNum = 0;
		boolean flag_doc = false;
		for (String line : lines) {
			if (line.startsWith("Doc:") || line.length() == 0) {
				if (line.startsWith("Doc:")) {
					if (flag_doc) {
						docNum += 1;
						bw.write("." + "\n");
						flag_doc = false;
					}
					if (docNum % 10000 == 0) {
						System.out.println(docNum);
					}
				}
				continue;
			}
			if (line.contains("\t")) {
				line = line.substring(line.indexOf("\t")+1, line.length());
			}
			
			boolean flag_line = false;
			String[] strs = line.split(",");
			for (int i = 0; i < strs.length - 1; i++) {
				String token = strs[i];
				if (lexicons.contains(token)) {
					bw.write(token + " ");
					frameNum += 1;
					flag_line = true;
					flag_doc = true;
				}
			}
			if (flag_line) {
				chainNum += 1;
				bw.write("period" + " ");
			}
		}
		bw.close();
		
		System.out.println("Doc:\t" + docNum);
		System.out.println("Chain:\t" + chainNum);
		System.out.println("Frame:\t" + frameNum);
		System.out.println("AVG Frame Per Doc:\t" + (frameNum / docNum));
		System.out.println("AVG Frame Per Chain:\t" + (frameNum / chainNum));
	}

	private static ArrayList<String> loadLexicons() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(IOManager.readLines("data/frame-signle-srl-group.txt"));
		list.addAll(IOManager.readLines("data/frame-compound-srl-group.txt"));
		return list;
	}
}
