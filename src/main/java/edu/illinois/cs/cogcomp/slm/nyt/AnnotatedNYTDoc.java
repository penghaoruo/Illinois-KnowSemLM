package edu.illinois.cs.cogcomp.slm.nyt;

import java.io.*;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.slm.Parameters;

public class AnnotatedNYTDoc implements Serializable {
		static final long serialVersionUID = -7588980448693010389L;
		public NYTCorpusDocument doc;
		public TextAnnotation ta;
		
		public AnnotatedNYTDoc() {}

		public AnnotatedNYTDoc(NYTCorpusDocument nytDoc, TextAnnotation ta) {
			this.doc = nytDoc;
			this.ta = ta;
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
