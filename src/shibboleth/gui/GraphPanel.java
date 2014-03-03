package shibboleth.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;

import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.openide.util.Lookup;

import processing.core.PApplet;


public class GraphPanel extends JPanel{
	
	private static final long serialVersionUID = -7823760349649788995L;
	private ProcessingTarget target;
	private PreviewController previewController;
	private PreviewModel previewModel;
	
	public GraphPanel(){
		setLayout(new BorderLayout());
		initGui();
	}
	
	public void initGui(){
		previewController = Lookup.getDefault().lookup(PreviewController.class);
		
        previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new Font(Font.SANS_SERIF, Font.PLAIN, 4));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 60);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 5f);
        previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, new Color(48,10,36));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(1.0f));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(new Color(255, 255, 255)));
        previewModel.getProperties().putValue(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(new Color(48,10,36)));
        
        //previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_MAX_CHAR, new Integer(20));
        //previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_SHORTEN, Boolean.TRUE);
        
        //Processing target, get the PApplet
	    target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
	    previewController.render(target);
	    PApplet applet = target.getApplet();
	    applet.init();
	    
	    add(applet, BorderLayout.CENTER);
	    
	    refresh();
	    reset();

	}
	
	public void showLabels(boolean show){
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, new Boolean(show));
	}
	
	public void refresh(){
		target.refresh();
		previewController.render(target);
		previewController.refreshPreview();
        repaint();
	}
	
	public void reset(){
		target.resetZoom();
		refresh();
	}
	
	
	
}
