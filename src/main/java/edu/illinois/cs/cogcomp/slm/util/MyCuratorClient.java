package edu.illinois.cs.cogcomp.slm.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisChunkerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisLemmatizerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisNerHandler;
import edu.illinois.cs.cogcomp.annotation.handler.IllinoisPOSHandler;
import edu.illinois.cs.cogcomp.annotation.handler.StanfordDepHandler;
import edu.illinois.cs.cogcomp.annotation.handler.StanfordParseHandler;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorAnnotator;
import edu.illinois.cs.cogcomp.curator.CuratorClient;
import edu.illinois.cs.cogcomp.nlp.common.PipelineConfigurator;
import edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.IllinoisTokenizer;
import edu.illinois.cs.cogcomp.nlp.util.SimpleCachingPipeline;
import edu.illinois.cs.cogcomp.nlp.utility.CcgTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;

public class MyCuratorClient {
	private static TextAnnotationBuilder taBuilder = new CcgTextAnnotationBuilder(new IllinoisTokenizer());
	private static final String CONFIG_FILE = "config/caching-curator.properties";
	public static Map<String, Annotator> viewProviders = null; 
	public static ResourceManager rm;
	public static SimpleCachingPipeline client;
	public static IllinoisLemmatizer lemmatizer = new IllinoisLemmatizer();
	
	public static void init() throws Exception {
		try {
			rm = new ResourceManager(CONFIG_FILE);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		List<Annotator> annotators = new ArrayList<Annotator>();
		
		annotators.add(new IllinoisPOSHandler());
		annotators.add(new IllinoisChunkerHandler());
		
		ResourceManager defaultRM = new PipelineConfigurator().getConfig(rm);
		annotators.add(new IllinoisNerHandler(defaultRM, ViewNames.NER_CONLL));
		annotators.add(new IllinoisLemmatizerHandler(defaultRM));
		/*
		CuratorClient curatorClient = new CuratorClient(rm);
		String[] requiredViews = new String[]{ViewNames.SENTENCE, ViewNames.TOKENS, ViewNames.POS};
		annotators.add(new CuratorAnnotator(curatorClient, ViewNames.COREF, requiredViews));
		*/
		annotators.add(new SemanticRoleLabeler("Verb"));
		//annotators.add(new SemanticRoleLabeler("Nom"));
		
		Properties stanfordProps = new Properties();
		stanfordProps.put("annotators", "pos, parse");
		stanfordProps.put("parse.originalDependencies", true);
		stanfordProps.put("parse.maxlen", 60);
		stanfordProps.put("parse.maxtime", 1000); // per sentence? could be per document but no idea from stanford javadoc
		POSTaggerAnnotator posAnnotator = new POSTaggerAnnotator("pos", stanfordProps);
		ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);

		annotators.add(new StanfordDepHandler(posAnnotator, parseAnnotator));
		annotators.add(new StanfordParseHandler(posAnnotator, parseAnnotator));
		
		viewProviders = new HashMap<String, Annotator>(annotators.size());
		for (Annotator annotator : annotators) {
			viewProviders.put(annotator.getViewName(), annotator);
		}
		try {
			client = new SimpleCachingPipeline(taBuilder, viewProviders, rm);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (AnnotatorException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
}
