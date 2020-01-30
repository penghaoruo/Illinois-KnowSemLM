package edu.illinois.cs.cogcomp.slm.util;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.HeadFinderDependencyViewGenerator;
import edu.illinois.cs.cogcomp.nlp.pipeline.IllinoisPipelineFactory;
import edu.illinois.cs.cogcomp.srl.SRLProperties;
import edu.illinois.cs.cogcomp.srl.config.SrlConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextProcessor {
	private static TextProcessor instance;
    public final AnnotatorService annotator;
	public final static String[] requiredViews = { //ViewNames.POS,
			//ViewNames.NER_CONLL,ViewNames.LEMMA, ViewNames.SHALLOW_PARSE,
			ViewNames.SRL_VERB };

	/**
	* requires SRLProperties to have been instantiated already
	 */
    public TextProcessor( SRLProperties config ) throws Exception {
        String defaultParser = config.getDefaultParser();
        boolean useCurator = false;//config.useCurator();

        if (useCurator) {
			System.out.println("Using curator");
			annotator = CuratorFactory.buildCuratorClient();
		} else {
			System.out.println("Using pipeline");
			if (!defaultParser.equals("Stanford")) {
				System.out.println("Illinois Pipeline works only with the Stanford parser.\n"
						+ "Please change the 'DefaultParser' parameter in the configuration file.");
				System.exit(-1);
			}
			annotator = IllinoisPipelineFactory.buildPipeline();
		}
	}

    public static void initialize()
    {
        SRLProperties props = SRLProperties.getInstance();
        initialize(props);
    }

	public static void initialize(ResourceManager rm) {
        SRLProperties props = SRLProperties.getInstance(rm);
        initialize(props);
    }

    public static void initialize( SRLProperties props )
    {
		try {
			instance = new TextProcessor( props );
		} catch (Exception e) {
			System.out.println("Unable to initialize the text pre-processor");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static TextProcessor getInstance() {
		if (instance == null) {
			// Start a new TextPreProcessor with default values (no Curator, no
			// tokenization) and default config
			initialize( new SrlConfigurator().getDefaultConfig() );
		}
		return instance;
	}

	public TextAnnotation processText(String text) throws Exception {
		TextAnnotation ta;
		ta = annotator.createBasicTextAnnotation("", "", text);
		addViews(ta);
		return ta;
	}

	public void processText(TextAnnotation ta) throws Exception {
		addViews(ta);
	}

	private void addViews(TextAnnotation ta) throws AnnotatorException {
		for (String view : requiredViews) {
			if (!ta.hasView(view))
				annotator.addView(ta, view);
		}
		/*
		if (!ta.hasView(ViewNames.CLAUSES_STANFORD))
			ta.addView(ClauseViewGenerator.STANFORD);
		if (!ta.hasView(ViewNames.DEPENDENCY + ":" + ViewNames.PARSE_STANFORD))
			ta.addView(new HeadFinderDependencyViewGenerator(ViewNames.PARSE_STANFORD));
		*/
	}
}
