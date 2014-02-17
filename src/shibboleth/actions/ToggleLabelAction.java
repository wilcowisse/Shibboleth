package shibboleth.actions;

import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.openide.util.Lookup;

/**
 * Toggle labels in graph.
 * 
 * Syntax: <tt>labels</tt>.
 * 
 * @author Wilco Wisse
 *
 */
public class ToggleLabelAction extends ShibbolethAction {
	
	public static boolean isShowing = false;
		
	@Override
	public void execute(String[] args){
		if(args.length == 0){
			isShowing = !isShowing;
			PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
			PreviewModel previewModel = previewController.getModel();
			previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, new Boolean(isShowing));
			listener.graphChanged("Toggled labels", false);
		}
		else {
			listener.messagePushed("Wrong syntax");
		}
	}

	@Override
	public String getCommand() {
		return "labels";
	}
}