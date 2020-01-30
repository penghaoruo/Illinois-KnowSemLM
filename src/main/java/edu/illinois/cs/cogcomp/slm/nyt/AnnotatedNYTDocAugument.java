package edu.illinois.cs.cogcomp.slm.nyt;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.slm.Parameters;

public class AnnotatedNYTDocAugument implements Serializable {
	static final long serialVersionUID = -7578980348693010389L;
	public TextAnnotation ta;
	public TextAnnotation corefta;
	public ArrayList<TextAnnotation> srltas;

	public AnnotatedNYTDocAugument(TextAnnotation ta, TextAnnotation corefta, ArrayList<TextAnnotation> srltas) {
		this.ta = ta;
		this.corefta = corefta;
		this.srltas = srltas;
	}
	
	public void save(String fname) throws IOException {			
		String filename = Parameters.nyt_annotation_path + fname + ".ta";
		FileOutputStream fileOut = new FileOutputStream(filename);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}

	public static AnnotatedNYTDocAugument load(String filename) throws Exception {
		FileInputStream fileIn = new FileInputStream(filename);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		AnnotatedNYTDocAugument e = (AnnotatedNYTDocAugument) in.readObject();
		in.close();
		fileIn.close();
		return e;
	}
}
