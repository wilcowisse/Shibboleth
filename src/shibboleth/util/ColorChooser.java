package shibboleth.util;

import java.awt.Color;

import shibboleth.model.SimpleRepo;
import shibboleth.model.SimpleUser;

public class ColorChooser {
	
	public Color getColor(SimpleUser u){
		return new Color(84, 235, 122);
	}
	
	public Color getColor(SimpleRepo r, boolean hasContributionInfo){
		if(hasContributionInfo){
			return new Color(84,192,235);
		}
		else{
			return new Color(164, 215, 235);
		}
	}
}
