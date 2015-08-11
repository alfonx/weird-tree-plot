package fileProcessing;

import graph.GraphNode;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

/** Export feature for svg vector graphics
 * At the moment, only circles will be written
 * @author justin
 *
 */
public class SVGGraph {

	public static void exportToSVG(	String filename, HashSet<GraphNode> nodes, boolean writeCaption,
									boolean writeCircles, boolean writeEdges) {
		
		writeCaption = true;
		writeEdges = false;
		double scale = 256.0;
		double strokeWidth = 0.01; //relative to scale
		boolean fillGradient = false;
		boolean stroke = true;
		double posxmin = Double.MAX_VALUE;
		double posxmax = Double.MIN_VALUE;
		double posymin = Double.MAX_VALUE;
		double posymax = Double.MIN_VALUE;
		for(GraphNode x : nodes) {
			if(x.getxPos() - x.getRadius() < posxmin) posxmin = x.getxPos() - x.getRadius();
			if(x.getxPos() + x.getRadius() > posxmax) posxmax = x.getxPos() + x.getRadius();
			if(x.getyPos() - x.getRadius() < posymin) posymin = x.getyPos() - x.getRadius();
			if(x.getyPos() + x.getRadius() > posymax) posymax = x.getyPos() + x.getRadius();
		}
		
		double width = posxmax - posxmin;
		double height = posymax - posymin;
		
		
		Locale.setDefault(Locale.ENGLISH);
		DecimalFormat df = new DecimalFormat("#.########");
		try{
			FileWriter writer = new FileWriter(filename);
			String append = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
					"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" "
					+ "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" + 
					"\n" + 
					"<svg xmlns=\"http://www.w3.org/2000/svg\"\n" + 
					"     xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
					+ "xmlns:ev=\"http://www.w3.org/2001/xml-events\"\n" + 
					"     version=\"1.1\" baseProfile=\"full\"\n" + 
					"     width=\"%widthpx\" height=\"%heightpx\"\n" + 
					"     viewBox=\"%cornerx %cornery %width %height\">\n" + 
					"\n";
			append = append.replaceAll("%width", df.format(width*scale));
			append = append.replaceAll("%height", df.format(height*scale));
			append = append.replaceAll("%cornerx", df.format(posxmin*scale));
			append = append.replaceAll("%cornery", df.format(posymin*scale));
			
			writer.append(append);
			
			for(GraphNode x : nodes) {
				if(writeCaption) {
					String insert = "<text x=\"%x\" y=\"%y\"\n" + 
							"      style=\"text-anchor: middle\">\n" + 
							"    %caption\n" + 
							"</text>\n";
					insert = insert.replaceAll("%x", df.format(x.getxPos()*scale));
					insert = insert.replaceAll("%y", df.format(x.getyPos()*scale));
					insert = insert.replaceAll("%caption",  
							StringEscapeUtils.escapeXml11(Matcher.quoteReplacement(x.getCaption())));
					writer.append(insert);
				}
				if(writeCircles) {
					GraphNode it = x;
					String insert = "\t<circle cx=\"%cx\" cy=\"%cy\" r=\"%r\" "
							+ "stroke=\"%Stroke\" stroke-width=\"%strokeWidthpx\" fill=\"%color\"/>\n";
					if(fillGradient) {
						int level = 255;
						while(it.getParent()!=null) {
							it = it.getParent();
							level -= 30;
						}
						if(level < 17) level = 17;
						insert = insert.replaceAll("%color", "#" + 
								Integer.toHexString(level) + "0000");
					} else {
						insert = insert.replaceAll("%color", "none");
					}
					insert = insert.replaceAll("%cx", df.format(x.getxPos()*scale));
					insert = insert.replaceAll("%cy", df.format(x.getyPos()*scale));
					insert = insert.replaceAll("%r", df.format(x.getRadius()*scale));
					insert = insert.replaceAll("%strokeWidth", df.format(strokeWidth*scale));
					if(stroke) insert = insert.replaceAll("%Stroke", "black");
					else insert = insert.replaceAll("%Stroke", "none");
					writer.append(insert);
				}
				try{
					GraphNode p = x.getParent();
					if(writeEdges && p!=null) {
						writer.append("\\draw [->] (" + 
								df.format(x.getxPos()) + "*\\SCALE," + 
								df.format(x.getyPos()) + "*\\SCALE) -- (" + 
								df.format(p.getxPos()) + "*\\SCALE," + 
								df.format(p.getyPos()) + "*\\SCALE);\n\n");
					}
				} catch(NullPointerException e) {}


			}


			writer.append(
					"\\n\" + \n" + 
					"					\"</svg>"
					);

			writer.flush();
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
