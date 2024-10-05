package net.edl;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class Main {

	private static CommandLine commandLine = null; 
	private static String reference;


	public static void main(String[] args) throws Exception 
	{
		readArgs(args);
		
		BiblicalDataManager bibleManager = new BiblicalDataManager();
		bibleManager.getBiblicalTexts(reference);
		
		Map<String, String> leftText = bibleManager.getLeftText();
		if (leftText.isEmpty())
			throw new Exception("Left part is empty");
		
		Map<String, String> rightText = bibleManager.getRightText();
		if (rightText.isEmpty())
			throw new Exception("Right part is empty");
		
		boolean isAT = bibleManager.isAT();
		
		//EBDocManagerPOI docManager = new EBDocManagerPOI(reference, isAT, leftText, rightText);
		//EBDocManagerODFTool docManager = new EBDocManagerODFTool(reference, isAT, leftText, rightText);
		EBDocManagerJDoc docManager = new EBDocManagerJDoc(reference, isAT, leftText, rightText);
		String generatedDoc = docManager.generateDocument();
		
		System.out.println("End of program");
		System.out.println("Reference: " + reference);
		System.out.println("Doc saved in file:" + generatedDoc);
	}
	
	private static void readArgs(String[] args) throws ParseException
	{
		Option option_reference  = Option.builder("r").required(true).longOpt("reference").hasArg().build();
		Option option_refStart   = Option.builder("s").required(false).longOpt("refStart").build();
		Option option_refEnd     = Option.builder("e").required(false).longOpt("endRef").build();
		Option option_allChapter = Option.builder("a").required(false).longOpt("allChapter").build();
		Option option_version    = Option.builder("v").required(false).longOpt("version").hasArg().build();

		Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        
        options.addOption(option_reference);
        options.addOption(option_refStart);
        options.addOption(option_refEnd);
        options.addOption(option_allChapter);
        options.addOption(option_version);
                
        try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("Unable to read entry args: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
        
        if (commandLine.hasOption(option_reference))
        	reference = commandLine.getOptionValue(option_reference);
                
        System.out.println("End of reading args");
	}

}
