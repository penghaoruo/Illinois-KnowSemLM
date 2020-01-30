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

public class AnnotatedNYTDocNEW implements Serializable {
	static final long serialVersionUID = -7588980348693010389L;
	public NYTCorpusDocument doc;
	public TextAnnotation ta;
	public ArrayList<TextAnnotation> srltas;
	
	public AnnotatedNYTDocNEW() {}

	public AnnotatedNYTDocNEW(NYTCorpusDocument nytDoc, TextAnnotation ta, ArrayList<TextAnnotation> srltas) {
		this.doc = nytDoc;
		this.ta = ta;
		this.srltas = srltas;
	}

	public void save() throws IOException {			
		String filename = Parameters.nyt_annotation_path + doc.getGuid() + ".ser";
		FileOutputStream fileOut = new FileOutputStream(filename);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
	}

	public static AnnotatedNYTDoc load(String filename) throws Exception {
		FileInputStream fileIn = new FileInputStream(filename);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		AnnotatedNYTDoc e = (AnnotatedNYTDoc) in.readObject();
		in.close();
		fileIn.close();
		return e;
	}
}
