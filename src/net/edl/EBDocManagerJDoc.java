package net.edl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jdom.Element;
import org.jopendocument.dom.ODDocument;
import org.jopendocument.dom.ODPackage;
import org.jopendocument.dom.ODXMLDocument;
import org.jopendocument.dom.OOUtils;
import org.jopendocument.dom.spreadsheet.Table;


public class EBDocManagerJDoc 
{
	private Map<String, String> leftPart = null;
	private Map<String, String> rightPart = null;
	private String reference = null;
	private String outputFileName = null;
	private boolean isAT = false;

	public EBDocManagerJDoc(String ref, boolean isAT, Map<String, String> lp, Map<String, String> rp) 
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
				
		String ref = reference.replaceAll("/", "_");
		ref = ref.replaceAll(":", "_");
		ref = ref.replaceAll(" ", "");
		
		String styleName = "left_text";

		outputFileName = "/tmp/document_" + ref + ".odt";
		
		String fname = "/home/data/EspritDeLiberte/EB/modeleEB";
		String suffix = "_NT";
		if (isAT)
			suffix = "_AT";
			
		fname += suffix + ".odt";
		styleName += suffix;
		
		System.out.println(styleName);
				
        File templateFile = new File(fname);
        File outFile = new File(outputFileName);
        
        if (outFile.exists())
        	outFile.delete();
        
        final ODDocument single = new ODPackage(templateFile).getODDocument();
        final ODXMLDocument content = single.getPackage().getContent();

        final Element elementTable = content.getDescendantByName("table:table", "TexteEB");
        final Table<ODDocument> table = new Table<ODDocument>(single, elementTable);
        
        table.duplicateFirstRows(1, leftPart.size() -1);
        
        int numRow = 0;
        for (Entry<String, String> e: leftPart.entrySet())
        {
        	String key = e.getKey();
        	String leftText = e.getValue();
        	String rightText = rightPart.get(key);
        	
        	table.setValueAt(leftText, 0, numRow);
        	table.setValueAt(rightText, 2, numRow);
        	
        	if (numRow < leftPart.size() -1)
        	{
            	if (key.contains("_"))
            		key = key.split("_")[1];
            	
            	table.setValueAt(key, 1, numRow);
        	}
        	        	
        	numRow++;
        }
              
        // Save to file.
        single.saveAs(outFile);
        
        /*
         *  On ajoute "a la main" la référence dans l'entete du doc
         *  Dans LO, Créer un champ d'utilisateur nommé comme on veut, de type texte
         *  et avec comme valeur __reference__ et l'insérer dans le doc.
         *  
         *  Le code ci-dessous remplacera le mot clé par sa vrai valeur
         */
        
       	Map<String, String> vars = new HashMap<String, String>();
		vars.put("__reference__", reference);
		
		modifyTextFileInZip(outputFileName, vars);
        
        // Open the document with LibreOffice !
        OOUtils.open(outFile);

        return outputFileName;
	}
	
	private void modifyTextFileInZip(String zipPath, Map<String, String> vars) throws IOException 
	{
	    Path zipFilePath = Paths.get(zipPath);
	    try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader)null)) 
	    {
	        Path source = fs.getPath("/content.xml");
	        Path temp   = fs.getPath("/___abc___.txt");
	        if (Files.exists(temp)) 
	        {
	            throw new IOException("temp file exists, generate another name");
	        }
	        
	        Files.move(source, temp);
	        streamCopy(temp, source, vars);
	        Files.delete(temp);
	    }
	}

	private void streamCopy(Path src, Path dst, Map<String, String> vars) throws IOException 
	{
	    try (BufferedReader br = new BufferedReader(new InputStreamReader (Files.newInputStream(src)));
	         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(dst)))) 
	    {
	        String line;
	        while ((line = br.readLine()) != null) 
	        {
	        	for (Entry<String, String> e: vars.entrySet())
	        	{
	        		line = line.replace(e.getKey(), e.getValue());
	        	}
	        		        	
	            bw.write(line);
	            bw.newLine();
	        }
	    }
	}
}
