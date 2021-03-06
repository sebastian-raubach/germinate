/*
 *  Copyright 2017 Information and Computational Sciences,
 *  The James Hutton Institute.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jhi.germinate.util.importer.marker;

import org.apache.poi.ss.usermodel.*;

import java.util.*;

import jhi.germinate.shared.datastructure.database.Map;
import jhi.germinate.shared.datastructure.database.*;
import jhi.germinate.util.importer.reader.*;

/**
 * {@link ExcelMarkerTypeReader} implements {@link IBatchReader} and reads the single {@link Map} object from the metadata sheet.
 *
 * @author Sebastian Raubach
 */
public class ExcelMarkerTypeReader extends ExcelBatchReader<MarkerType>
{
	private Sheet dataSheet;

	@Override
	public List<MarkerType> readAll()
	{
		List<MarkerType> result = new ArrayList<>();

		result.add(new MarkerType()
				.setDescription(utils.getCellValue(dataSheet.getRow(13), 2)));

		return result;
	}

	@Override
	public void init(Workbook wb)
	{
		dataSheet = wb.getSheet("METADATA");
	}
}
