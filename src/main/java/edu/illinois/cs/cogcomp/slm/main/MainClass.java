package edu.illinois.cs.cogcomp.slm.main;

import java.util.Collections;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.cloze.PropbankFrames;
import edu.illinois.cs.cogcomp.slm.gw.AnnotateGW;
import edu.illinois.cs.cogcomp.slm.nyt.AnnotateNYT;
import edu.illinois.cs.cogcomp.slm.nyt.AugumentCoref;
import edu.illinois.cs.cogcomp.slm.preprocess.PredicateGroup;
import edu.illinois.cs.cogcomp.slm.preprocess.PrepareLMSource;
import edu.illinois.cs.cogcomp.slm.preprocess.SRLChain;
import edu.illinois.cs.cogcomp.slm.preprocess.SRLCorefChain;
import edu.illinois.cs.cogcomp.slm.util.MyCuratorClient;
import edu.illinois.cs.cogcomp.slm.util.MyFunc;
import edu.illinois.cs.cogcomp.slm.util.TextProcessor;

public class MainClass {
	public static void main(String[] args) throws Exception {
		AnnotateNYT.annotateAll();
		//AugumentCoref.annotateAll();
		//AnnotateGW.annotateAll();
		
		//SRLChain.generateAll();
		//PredicateGroup.generate(Parameters.srl_chain_file);
		
		//SRLCorefChain.generateAll();
		//PredicateGroup.generate(Parameters.srlcoref_chain_file);
		
		//PropbankFrames.getChains();
		//MyFunc.computeStat(Parameters.srl_chain_file);
		//MyFunc.computeStat(Parameters.srl_chain_group_file);
		//MyFunc.computeStat(Parameters.srlcoref_chain_file);
		//MyFunc.computeStat(Parameters.srlcoref_chain_group_file);
		
		//PrepareLMSource.prepareDoc(Parameters.srl_chain_file, "/shared/preprocessed/resources/SRL_DOC.txt");
		//PrepareLMSource.prepareDoc(Parameters.srl_chain_group_file, "/shared/preprocessed/resources/SRLGROUP_DOC.txt");
		
		//MyFunc.printFileList(Parameters.nyt_annotation_path, "data/anotation_filelist_ser.txt");
		//MyFunc.printFileList(Parameters.nyt_annotation_ta, "data/anotation_filelist_ta.txt");
		
		//MyFunc.annotateSRL();
		//MyFunc.sortFileByValue("o_ceaf.txt");
		//MyFunc.printFileList("/shared/experiments/hpeng7/results/ACE2004/Golden/Rawtext/", "ace04.txt");
	}
}
