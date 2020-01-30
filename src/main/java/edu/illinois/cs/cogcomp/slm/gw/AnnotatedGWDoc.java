package edu.illinois.cs.cogcomp.slm.gw;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotatedNYTDoc;

public class AnnotatedGWDoc implements Serializable {
		static final long serialVersionUID = -7578980448693010389L;
		public String content;
		public String id;
		public TextAnnotation ta;
		
		public AnnotatedGWDoc() {}

		public AnnotatedGWDoc(String doc, String id, TextAnnotation ta) {
			this.content = doc;
			this.id = id;
			this.ta = ta;
		}

		public void save() throws IOException {			
			String filename = Parameters.gw_annotation_path + id + ".ser";
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
