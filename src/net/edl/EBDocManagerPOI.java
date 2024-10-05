package net.edl;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

public class EBDocManagerPOI 
{
	private Map<String, String> leftPart = null;
	private Map<String, String> rightPart = null;
	private String reference = null;
	private String outputFileName = null;
	private boolean isAT = false;


	public EBDocManagerPOI(String ref, boolean isAT, Map<String, String> lp, Map<String, String> rp) 
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

		outputFileName = "/tmp/document_" + ref + ".docx";

		int headerFooterSize = 10;
		File outFile = new File(outputFileName);

		if (outFile.exists())
			outFile.delete();

		//Blank Document
		try (XWPFDocument document = new XWPFDocument();
				FileOutputStream out = new FileOutputStream(outFile))
		{
			CTDocument1 doc1 = document.getDocument();
			CTBody body = doc1.getBody();

			if (! body.isSetSectPr()) {
				body.addNewSectPr();
			}

			CTSectPr section = body.getSectPr();

			if (! section.isSetPgSz()) {
				section.addNewPgSz();
			}

			CTPageSz pageSize = section.getPgSz();
			pageSize.setW(BigInteger.valueOf(11900)); // 595 * 20
			pageSize.setH(BigInteger.valueOf(16840)); // 792 * 20

			XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
			XWPFParagraph headerParagraph = header.createParagraph(); 
			XWPFRun headerRun = headerParagraph.createRun();

			headerParagraph.setAlignment(ParagraphAlignment.CENTER);
			headerRun.setFontSize(headerFooterSize);
			headerRun.setFontFamily("Liberation Serif");

			headerRun.setText("Etude biblique: " + reference);


			XWPFFooter footer = document.createFooter(HeaderFooterType.DEFAULT);
			XWPFParagraph footerParagraph = footer.createParagraph(); 
			XWPFRun footerRun = footerParagraph.createRun();

			footerRun.setFontSize(headerFooterSize);
			footerRun.setFontFamily("Liberation Serif");

			footerRun.setText("Page ");

			footerParagraph.getCTP().addNewFldSimple().setInstr("PAGE \\* MERGEFORMAT");

			footerRun = footerParagraph.createRun();  
			footerRun.setFontSize(headerFooterSize);
			footerRun.setFontFamily("Liberation Serif");

			footerRun.setText(" sur ");
			footerParagraph.getCTP().addNewFldSimple().setInstr("NUMPAGES \\* MERGEFORMAT");

			footerRun = footerParagraph.createRun();
			footerRun.setFontSize(headerFooterSize);
			footerRun.setFontFamily("Liberation Serif");

			footerRun.addTab();
			footerRun.addTab();
			footerRun.addTab();
			footerRun.addTab();
			footerRun.addTab();
			footerRun.addTab();
			footerRun.addTab();

			footerRun.setText("https://espritdeliberte.leswoody.net");				        

			XWPFTable table = document.createTable(leftPart.size(), 3);

			CTTblLayoutType type = table.getCTTbl().getTblPr().addNewTblLayout();
			type.setType(STTblLayoutType.FIXED);

			table.setTableAlignment(TableRowAlign.CENTER);
			table.setWidth("100%");

			table.getCTTbl().addNewTblGrid().addNewGridCol().setW("40%");
			table.getCTTbl().getTblGrid().addNewGridCol().setW("5%");
			table.getCTTbl().getTblGrid().addNewGridCol().setW("55%");

			int numRow = 0;
			for (Entry<String, String> e: leftPart.entrySet())
			{
				String key = e.getKey();
				String leftText = e.getValue();
				String rightText = rightPart.get(key);

				XWPFTableRow tableRow = table.getRow(numRow);

				tableRow.setCantSplitRow(true);

				XWPFTableCell leftCell   = tableRow.getCell(0);
				XWPFTableCell centerCell = tableRow.getCell(1);
				XWPFTableCell rightCell  = tableRow.getCell(2);

				XWPFParagraph paragraph = leftCell.getParagraphArray(0);
				XWPFRun runLeftCell = paragraph.createRun();

				paragraph.setSpacingAfter(0);

				if (isAT)
				{
					runLeftCell.setFontFamily("SBL Hebrew");
					paragraph.setAlignment(ParagraphAlignment.RIGHT);
					runLeftCell.setComplexScriptFontSize(13);
				}
				else
				{
					runLeftCell.setFontFamily("Ezra SIL");
					runLeftCell.setFontSize(12);
				}

				runLeftCell.setText(leftText);

				paragraph = rightCell.getParagraphArray(0);
				XWPFRun runRightCell = paragraph.createRun();
				runRightCell.setFontFamily("Liberation Serif");
				runRightCell.setFontSize(12);
				runRightCell.setText(rightText);

				if (numRow < leftPart.size() -1)
				{
					if (key.contains("_"))
						key = key.split("_")[1];

					paragraph = centerCell.getParagraphArray(0);
					paragraph.setAlignment(ParagraphAlignment.CENTER);
					centerCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

					XWPFRun runCenterCell = paragraph.createRun();
					runCenterCell.setFontFamily("Liberation Serif");
					runCenterCell.setFontSize(10);
					runCenterCell.setText(key);		
				}
				else
				{
					runLeftCell.setItalic(true);
					runRightCell.setItalic(true);
				}

				numRow++;
			}

			document.write(out);
			
			return outputFileName;
		}
	}
}
