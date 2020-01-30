package edu.illinois.cs.cogcomp.slm.preprocess;

import java.util.ArrayList;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.slm.Parameters;
import edu.illinois.cs.cogcomp.slm.util.IOManager;

public class FrameMapping {
	class VerbNet {
		String verb;
		String sense;
		
		@Override
		public int hashCode() {
		    int hash = 3;
		    hash = 53 * hash + (this.verb != null ? this.verb.hashCode() : 0);
		    hash = 53 * hash + this.sense.hashCode();
		    return hash;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
		        return false;
		    }
		    if (getClass() != obj.getClass()) {
		        return false;
		    }
		    final VerbNet vb = (VerbNet) obj;
			if (this.verb.equals(vb.verb) && this.sense.equals(vb.sense)) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	private HashMap<VerbNet, String> map = new HashMap<VerbNet, String>();
	
	public FrameMapping() {
		ArrayList<String> lines = IOManager.readLines(Parameters.frame_map_file);
		for (String line : lines) {
			if (line.startsWith("<vncls class=")) {
				VerbNet vn = new VerbNet();
				vn.sense = getField(line, "class");
				vn.verb = getField(line, "vnmember");
				String frame = getField(line, "fnframe");
				map.put(vn, frame);
			}
		}
	}

	private String getField(String line, String str) {
		int p = line.indexOf(str);
		int a = line.indexOf("\'", p);
		int b = line.indexOf("\'", a+1);
		return line.substring(a+1, b);
	}
	
	public String getFrame(String verb, String sense) {
		VerbNet vn = new VerbNet();
		vn.sense = sense;
		vn.verb = verb;
		return map.get(vn);
	}
}
