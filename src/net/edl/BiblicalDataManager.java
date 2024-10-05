package net.edl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BiblicalDataManager {

	private static String reference;
	
	private static String atTranslationBook = "aleppo"; //"codex"; //"codex"; // Westminster Leningrad Codex
	private static String ntTranslationBook = "westcotthort"; // Westcott and Hort with NA27/UBS4 variants
	private static String frenchTranslation = "darby";
		
	private boolean isAT = false;
	private boolean isNT = false;
	
	private final static String wsQueryURL = "https://query.getbible.net/v2/";
	private final static String wsAPIURL   = "https://api.getbible.net/v2/";
	private String leftWS;
	private String rightWS;
	
	private Map<String, String> leftText = new HashMap<String, String>();
	private Map<String, String> rightText = new HashMap<String, String>();
	
	private static final String[] atBooks = 
	{	// Pentateuque
		"genese", "exode", "levitique", "nombres", "deuteronome", 
		
		// Livres historiques
		"josue", "juges", "ruth", "1 samuel", "2 samuel", "1 rois", "2 rois",
		"1 chroniques", "2 chroniques", "esdras", "nehemie", "esther", 
		
		// Livres poétiques
		"job", "psaumes", "proverbes", "ecclesiaste", "cantique des cantiques", 
		
		// Prophètes
		"esaie", "jeremie", "lamentations", "ezechiel", "daniel", 
		"osee", "joel", "amos", "abdias", "jonas", "michee", 
		"nahum", "habacuc", "sophonie", "aggee", "zacharie", "malachie"		
	};
	
	private static final String[] ntBooks = 
	{	// Evangiles
		"matthieu", "marc", "luc", "jean",
		"actes des apotres",
		
		// Épîtres de Paul
		"romains", "1 corinthiens", "2 corinthiens", "galates", "ephesiens",
		"philippiens", "colossiens", "1 thessaloniciens", "2 thessaloniciens",
		"1 timothée", "2 timothée", "tite", "philemon", "hebreux",
		
		// Autres épîtres
		"jacques", "1 pierre", "2 pierre", "1 jean", "2 jean", "3 jean", "jude",
		
		"apocalypse"
	};
	
	private static final String referenceExtractorExp = "(.*?)\\s(\\d{1,3})(?:[:/](\\d{1,2})(?:-(\\d{1,3})?)?)?";


	public void getBiblicalTexts(String ref) throws Exception 
	{
		reference = ref;
		
		buildReferenceInfoForWS();
		
		leftText = callWS(leftWS);
		rightText = callWS(rightWS);
	}
	
	public Map<String, String> getLeftText()
	{
		return leftText;
	}

	public Map<String, String> getRightText()
	{
		return rightText;
	}
	
	public boolean isAT()
	{
		return isAT;
	}

	private int getNumBook(String book)
	{
		int result = -1;
		int tmp = 0; // on va de 0 à 65 
		
		book = book.toLowerCase();
		if ("psaume".equals(book))
			book = "psaumes";
		
		isAT = false;
		isNT = false;
		
		for (String b: atBooks)
		{
			tmp ++;
			if (b.equals(book))
			{
				result = tmp;
				isAT = true;
				break;
			}
		}
		
		if (! isAT)
		{
			for (String b: ntBooks)
			{
				tmp ++;
				if (b.equals(book))
				{
					result = tmp;
					isNT = true;
					break;
				}
			}
		}
		
		return result;
	}

	private void buildReferenceInfoForWS() throws Exception 
	{
		String ref = reference;
		
		ref = ref.replaceAll("é", "e");
		ref = ref.replaceAll("è", "e");
		ref = ref.replaceAll("ë", "e");
		ref = ref.replaceAll("ê", "e");
		ref = ref.replaceAll("ï", "i");
		ref = ref.replaceAll("ô", "o");
				
		Pattern p = Pattern.compile(referenceExtractorExp);
		Matcher m = p.matcher(ref);

		System.out.println("nb groupe: " + m.groupCount() );
		
		if (! m.find())
			throw new Exception("Unable to match reference");
		 
		String book       = m.group(1);
		String chapter    = m.group(2);
		String startVerse = m.group(3);
		String endVerse   = m.group(4);
		
		if (book == null)
			throw new Exception("Error: extracted book is null in: " + reference);
		
		int bookNum = getNumBook(book);
		if (bookNum == -1)
			throw new Exception("Unable to find book number: " + book);
				
		String msg = isAT ? "AT" : "";
		if ("".equals(msg))
			msg = isNT ? "NT" : "";
		
		if ("".equals(msg))
			throw new Exception("Undefined testament");
		
		System.out.println( book + " (" + bookNum + " - " + msg + ") " + chapter + " " + startVerse + " " + endVerse);
		
		String tmpWS = wsQueryURL;
		if (startVerse == null)
			tmpWS = wsAPIURL;
		
		String tmpURL = tmpWS;
		
		if (isAT)
			tmpURL += atTranslationBook;
		else
			tmpURL += ntTranslationBook;
					
		leftWS = completeReference(chapter, startVerse, endVerse, bookNum, tmpURL);		
		System.out.println(leftWS);
		
		tmpURL = tmpWS + frenchTranslation;
		rightWS = completeReference(chapter, startVerse, endVerse, bookNum, tmpURL);		
		System.out.println(rightWS);		
	}

	private String completeReference(String chapter, String startVerse, String endVerse, int bookNum, String tmpWS) 
	{
		tmpWS += "/" + bookNum;
		
		if (startVerse != null)
		{		
			tmpWS += "%20" + chapter + ":" + startVerse + "-" + endVerse;
		}
		else
		{
			tmpWS += "/" + chapter + ".json";
		}
		
		return tmpWS;
	}
	
	private Map<String, String> callWS(String urlString) throws Exception
	{
		try 
		{
			Map<String, String> result = new LinkedHashMap<>();
			URL url = URI.create(urlString).toURL();
			
			// Opening a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Setting the request method to GET
            connection.setRequestMethod("GET");

            // Retrieving the response code
            int responseCode = connection.getResponseCode();

            // Processing the response
            if (responseCode == HttpURLConnection.HTTP_OK) 
            {
            	try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")))
            	{
            		String inputLine;
            		StringBuilder response = new StringBuilder();

            		while ((inputLine = in.readLine()) != null) 
            		{
            			response.append(inputLine);
            		}
            		
            		String respString = response.toString();
                    System.out.println("API Response: " + respString);
                    
                    int pos = respString.indexOf('"', 3);                    
                    String key = response.substring(2, pos); // on fabrique la string westcotthort_47_3
                    
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(response.toString());                    
                	JsonNode obj = rootNode.get(key);
                	
                	if (obj == null)
                		obj = rootNode; // on recupere tout le chapitre
                	
                	JsonNode translation = obj.get("translation");
                	System.out.println(translation.asText());
                	
                	JsonNode verses = obj.get("verses");
                	
                	for (JsonNode node : verses) 
                	{                    	
                		key = node.get("chapter").asText() + "_" + node.get("verse").asText();
                		String value = node.get("text").asText();
                		value = value.replaceAll("\\*", "");
                		
                		System.out.println(key + " " + value);
                		 
                		result.put(key, value.trim() );
                	}
                	
                	result.put("translation", translation.asText());
            	}            	
            } else {
                System.out.println("API Call Failed. Response Code: " + responseCode);
            }
            
    		return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }		
	}
}
