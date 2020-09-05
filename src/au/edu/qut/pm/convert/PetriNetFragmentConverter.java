package au.edu.qut.pm.convert;

import au.edu.qut.prom.helpers.PetrinetExportUtils;

public class PetriNetFragmentConverter {

	public static void main(String[] args) throws Exception{
		String petriNetFrag = args[0];
		System.out.println( PetrinetExportUtils.petriNetFragmentToDOT(petriNetFrag) );
	}

}
