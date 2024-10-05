package net.edl;

import java.util.Map;
import java.util.Map.Entry;

import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.OdfStylesDom;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.dom.element.style.StyleParagraphPropertiesElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTabStopElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTabStopsElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfParagraphProperties;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextHeading;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;

public class EBDocManagerODFTool {

	private Map<String, String> leftPart = null;
	private Map<String, String> rightPart = null;
	private String reference = null;
	private boolean isAT = false;

	
	String outputFileName;
	OdfTextDocument outputDocument;

	OdfContentDom contentDom;  // the document object model for content.xml
	OdfStylesDom stylesDom;   // the document object model for styles.xml

	// the office:automatic-styles element in content.xml
	// this is here for the sake of completeness; this program doesn't use it
	OdfOfficeAutomaticStyles contentAutoStyles;

	// the office:styles element in styles.xml
	OdfOfficeStyles stylesOfficeStyles;

	// the office:text element in the content.xml file
	OfficeTextElement officeText;
	
	
	
	public EBDocManagerODFTool(String ref, boolean isAT, Map<String, String> lp, Map<String, String> rp) 
	{
		reference = ref;
		leftPart  = lp;
		rightPart = rp;
		this.isAT = isAT;
	}
	
	public String generateDocument() throws Exception
	{
		if (reference == null || reference.isEmpty())
			throw new Exception("Invalid reference, null or empty");
		
		if (leftPart == null || rightPart == null)
			throw new Exception("Invalid parameter: left or right part are null");
		
		if (leftPart.isEmpty() || rightPart.isEmpty())
			throw new Exception("Invalid parameter: left or right part are empty");
		
		String suffix = "_NT";
		if (isAT)
			suffix = "_AT";

		String ref = reference.replaceAll("/", "_");
		ref = ref.replaceAll(":", "_");
		ref = ref.replaceAll(" ", "");

		outputFileName = "/tmp/document_" + ref + ".odt";

		//OdfDocument odt = OdfDocument.loadDocument("/home/data/EspritDeLiberte/EB/modeleEB.ott");
		
		setupOutputDocument();
		
		if (outputDocument != null)
        {
            //cleanOutDocument();

            addOfficeStyles();

            //processTitle(reference);
            
            processInputDocument();

            saveOutputDocument();
        }
		
		return outputFileName;
	}
	
	private void setupOutputDocument()
	{
	    try
	    {
	        outputDocument = OdfTextDocument.newTextDocument();

	        contentDom = outputDocument.getContentDom();
	        stylesDom = outputDocument.getStylesDom();
	        
	        contentAutoStyles = contentDom.getOrCreateAutomaticStyles();
	        stylesOfficeStyles = outputDocument.getOrCreateDocumentStyles();

	        officeText = outputDocument.getContentRoot();
	    }
	    catch (Exception e)
	    {
	        System.err.println("Unable to create output file.");
	        System.err.println(e.getMessage());
	        outputDocument = null;
	    }
	}
	
	void setFontWeight(OdfStyleBase style, String value)
	{
	    style.setProperty(StyleTextPropertiesElement.FontWeight, value);
	    style.setProperty(StyleTextPropertiesElement.FontWeightAsian, value);
	    style.setProperty(StyleTextPropertiesElement.FontWeightComplex, value);
	}

	void setFontStyle(OdfStyleBase style, String value)
	{
	    style.setProperty(StyleTextPropertiesElement.FontStyle, value);
	    style.setProperty(StyleTextPropertiesElement.FontStyleAsian, value);
	    style.setProperty(StyleTextPropertiesElement.FontStyleComplex, value);
	}

	void setFontSize(OdfStyleBase style, String value)
	{
	    style.setProperty(StyleTextPropertiesElement.FontSize, value);
	    style.setProperty(StyleTextPropertiesElement.FontSizeAsian, value);
	    style.setProperty(StyleTextPropertiesElement.FontSizeComplex, value);
	}
	
	void addOfficeStyles()
	{
		OdfDefaultStyle defaultStyle;
		OdfStyle style;
		StyleParagraphPropertiesElement pProperties;

		StyleTabStopsElement tabStops;
		StyleTabStopElement tabStop;

		// Set default font size to 10 point
		defaultStyle = stylesOfficeStyles.getDefaultStyle(OdfStyleFamily.Paragraph);
		//style.setProperty(OdfTextProperties.FontSize, "10pt");

		setFontSize(defaultStyle, "14pt");

		style = stylesOfficeStyles.newStyle("Synopsis_20_Para", OdfStyleFamily.Paragraph);
		style.setStyleDisplayNameAttribute("Synopsis Para");
		style.setProperty(OdfParagraphProperties.Border,  "0.035cm solid #000000");
		style.setProperty(OdfParagraphProperties.Padding, "0.25cm");
		style.setProperty(OdfParagraphProperties.MarginLeft, "1cm");
		style.setProperty(OdfParagraphProperties.MarginRight, "1cm");
		style.setProperty(OdfParagraphProperties.TextIndent, "0.25cm");

		// Paragraph with tab stop at 7.5cm with a
		// leader of "."  This is used for the
		// cast list.
		style = stylesOfficeStyles.newStyle("Cast_20_Para", OdfStyleFamily.Paragraph);
		style.setStyleDisplayNameAttribute("Cast Para");
		style.setStyleFamilyAttribute(OdfStyleFamily.Paragraph.toString());

		// build hierarchy from "inside out"
		tabStop = new StyleTabStopElement(stylesDom);
		tabStop.setStylePositionAttribute("7.5cm");
		tabStop.setStyleLeaderStyleAttribute("dotted");
		tabStop.setStyleLeaderTextAttribute(".");
		tabStop.setStyleTypeAttribute("right");

		tabStops = new StyleTabStopsElement(stylesDom);
		tabStops.appendChild(tabStop);

		pProperties = new StyleParagraphPropertiesElement(stylesDom);
		pProperties.appendChild(tabStops);

		style.appendChild(pProperties);
	}
	
	private void processInputDocument() 
	{
		OdfTextParagraph paragraph;
		
		processTitle(reference);
		
		for (Entry<String, String> e: leftPart.entrySet())
		{
			String numVerse = e.getKey();
			String text = e.getValue();
			
			paragraph = new OdfTextParagraph(contentDom, "Synopsis_20_Para");
	        paragraph.addContent(text);
	        		
	        officeText.appendChild(paragraph);		
		}
	}

	private void processTitle(String title)
	{
	    OdfTextHeading heading;
	    
	    heading = new OdfTextHeading(contentDom);
	    heading.addStyledContent("Movie_20_Heading", title + " ");

	    officeText.appendChild(heading);
	}
	
	private void saveOutputDocument()
	{
	    try
	    {
	        outputDocument.save(outputFileName);
	    }
	    catch (Exception e)
	    {
	        System.err.println("Unable to save document.");
	        System.err.println(e.getMessage());
	    }
	}
	
}
