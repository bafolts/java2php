package translator;

import java.util.ArrayList;

public class Constructor extends Method {

	public Constructor(String line) {
		setContents(line);

		goToNext("(");

		String sParameters = getAndSetNext(")").trim();
		if(sParameters.length()>0) {
			String[] aParameters = sParameters.split(",");
			for(int i=0;i<aParameters.length;i++) {
				addParameter(new Parameter(aParameters[i]));
			}
		}

		setBlockBody(new BlockBody(currentString()));

	}
	
}