package edu.illinois.cs.cogcomp.slm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Parameters {
	public static String nyt_raw_path_1 = "/shared/corpora/corporaWeb/written/eng/NYT_annotated_corpus/data/accum1987-02/";
	public static String nyt_raw_path_2 = "/shared/corpora/corporaWeb/written/eng/NYT_annotated_corpus/data/accum2003-07/";
	public static String nyt_filelist_1 = "data/nyt_filelist_1.txt";
	public static String nyt_filelist_2 = "data/nyt_filelist_2.txt";
	
	public static String nyt_annotation_path = "/shared/preprocessed/resources/NYT/";
	public static String nyt_annotation_ta = "/shared/preprocessed/resources/NYT-TA/";
	
	public static String gw_filelist = "data/gw_filelist.txt";
	public static String gw_annotation_path = "/shared/preprocessed/resources/Gigaword-v5-unzip/";
	
	public static String srl_chain_file = "/shared/preprocessed/resources/srlChains.txt";
	public static String srl_chain_group_file = "/shared/preprocessed/resources/srlChains_group.txt";
	public static String srlcoref_chain_file = "/shared/preprocessed/resources/srlCorefChains.txt";
	public static String srlcoref_chain_group_file = "/shared/preprocessed/resources/srlCorefChains_group.txt";
	
	public static String penntreebank_path = "/shared/corpora/corporaWeb/treebanks/eng/pennTreebank/treebank-3/parsed/mrg/wsj/";
	public static String propbank_path = "/shared/corpora/corporaWeb/treebanks/eng/propbank_1/data/";
	public static String[] sections = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", 
		                               "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", 
		                               "20", "21", "22", "23", "24"};
	
	public static String frame_map_file = "data/vn-fn.xml";
	
	public static String coref_config_file = "config/AcePlainTextConfig";
	                                            
	public static void readParams(String configPath) {
		
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(configPath));
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.exit(0);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		//pathToGoldHeadLastWordPairCount = config.getProperty("pathToGoldHeadLastWordPairCount",pathToGoldHeadLastWordPairCount);
	}
}

