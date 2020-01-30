package edu.illinois.cs.cogcomp.slm.cloze;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.PropbankReader;
import edu.illinois.cs.cogcomp.slm.Parameters;

public class PropbankFrames {
	
	public static void getChains() {
		String viewName = "SRL_GOLD";
		PropbankReader reader = null;
		try {
			reader = new PropbankReader(Parameters.penntreebank_path, Parameters.propbank_path, Parameters.sections, viewName, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (reader.hasNext()) {
			TextAnnotation ta = reader.next();
			System.out.println(ta.getId());
			if (ta.hasView(viewName)) {
				System.out.println(ta.getView(viewName));
			}
		}
	}
}
