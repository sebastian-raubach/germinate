/**
 * Germinate 3 is written and developed by Sebastian Raubach and Paul Shaw from the Information and Computational Sciences Group at JHI Dundee. For
 * further information contact us at germinate@hutton.ac.uk or visit our webpages at https://ics.hutton.ac.uk/germinate
 *
 * Copyright © 2005-2017, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package jhi.germinate.util.importer.pedigree;

import org.apache.poi.openxml4j.exceptions.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.*;

import jhi.germinate.shared.datastructure.database.*;
import jhi.germinate.util.importer.reader.*;

/**
 * {@link ExcelPedigreeStringReader} implements {@link IStreamableReader} and reads and streams one {@link PedigreeDefinition} object at a time.
 *
 * @author Sebastian Raubach
 */
public class ExcelPedigreeStringReader implements IStreamableReader<PedigreeDefinition>
{
	private XSSFSheet dataSheet;

	private int rowCount   = 0;
	private int currentRow = 0;
	private XSSFRow      row;
	private XSSFWorkbook wb;

	@Override
	public boolean hasNext() throws IOException
	{
		return ++currentRow < rowCount;
	}

	@Override
	public PedigreeDefinition next() throws IOException
	{
		row = dataSheet.getRow(currentRow);
		return parse();
	}

	@Override
	public void init(File input) throws IOException, InvalidFormatException
	{
		wb = new XSSFWorkbook(input);

		dataSheet = wb.getSheet("DATA-STRING");

		rowCount = dataSheet.getPhysicalNumberOfRows();
	}

	@Override
	public void close() throws IOException
	{
		if (wb != null)
			wb.close();
	}

	private PedigreeDefinition parse()
	{
		return new PedigreeDefinition()
				.setAccession(new Accession().setGeneralIdentifier(IExcelReader.getCellValue(wb, row, 0)))
				.setDefinition(IExcelReader.getCellValue(wb, row, 1))
				.setNotation(new PedigreeNotation()
						.setName(IExcelReader.getCellValue(wb, row, 2))
						.setDescription(IExcelReader.getCellValue(wb, row, 2))
						.setCreatedOn(new Date())
						.setUpdatedOn(new Date()))
				.setDescription(new PedigreeDescription()
						.setName(IExcelReader.getCellValue(wb, row, 3))
						.setDescription(IExcelReader.getCellValue(wb, row, 3))
						.setAuthor(IExcelReader.getCellValue(wb, row, 4))
						.setCreatedOn(new Date())
						.setUpdatedOn(new Date()))
				.setCreatedOn(new Date())
				.setUpdatedOn(new Date());
	}
}