/*
 *  Copyright 2018 Information and Computational Sciences,
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

package jhi.germinate.test.importer;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import jhi.germinate.server.database.query.*;
import jhi.germinate.shared.datastructure.*;
import jhi.germinate.shared.exception.*;
import jhi.germinate.util.importer.compound.*;
import jhi.germinate.util.importer.genotype.*;
import jhi.germinate.util.importer.mcpd.*;
import jhi.germinate.util.importer.pedigree.*;
import jhi.germinate.util.importer.phenotype.*;

/**
 * @author Sebastian Raubach
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TemplateImporterTest extends DatabaseTest
{
	@Test
	public void importMcpdData() throws DatabaseException
	{
		File template = new File("datatemplates/example-germplasm-mcpd.xlsx");
		assert template.exists();

		new ExcelMcpdImporter()
				.run(template, server, database, username, password, null);

		ServerResult<Long> count = new ValueQuery("SELECT COUNT(1) AS count FROM `germinatebase`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 2000L);

		count = new ValueQuery("SELECT COUNT(1) AS count FROM `locations`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 10L);

		count = new ValueQuery("SELECT COUNT(1) AS count FROM `synonyms`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 8L);

		count = new ValueQuery("SELECT COUNT(1) AS count FROM `attributedata`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 32L);
	}

	@Test
	public void importExcelGenotypicData() throws DatabaseException
	{
		File template = new File("datatemplates/example-allele-calls.xlsx");
		assert template.exists();

		// Import the data
		new ExcelGenotypeDataImporter()
				.run(template, server, database, username, password, null);

		// Count the number of markers
		ServerResult<Long> count = new ValueQuery("SELECT COUNT(1) AS count FROM markers")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1000L);

		// Count the number of markers on the map
		count = new ValueQuery("SELECT COUNT(1) AS count FROM mapdefinitions")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1000L);

		// Count the number of dataset members
		count = new ValueQuery("SELECT COUNT(1) AS count FROM datasetmembers")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1500L);
	}

	@Test
	public void importTabDelimitedGenotypicData() throws DatabaseException
	{
		File template = new File("datatemplates/example-allele-calls-text.txt");
		assert template.exists();

		new TabDelimitedGenotypeDataImporter()
				.run(template, server, database, username, password, null);

		// Check if there's a dataset with the correct name
		ServerResult<Long> count = new ValueQuery("SELECT COUNT(1) AS count FROM datasets WHERE name = 'Dataset name goes here'")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1L);

		// Check if there's a map with the correct name
		count = new ValueQuery("SELECT COUNT(1) AS count FROM maps WHERE name = 'Map name here'")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1L);

		// Check if there's a marker type with the correct name
		count = new ValueQuery("SELECT COUNT(1) AS count FROM markertypes WHERE description = 'SNP'")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1L);

		// Count the number of markers
		count = new ValueQuery("SELECT COUNT(1) AS count FROM markers")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 5000L);

		// Count the number of markers on the map
		count = new ValueQuery("SELECT COUNT(1) AS count FROM mapdefinitions")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 6000L);

		// Count the number of dataset members
		count = new ValueQuery("SELECT COUNT(1) AS count FROM datasetmembers")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 8500L);
	}

	@Test
	public void importTrialsData() throws DatabaseException
	{
		File template = new File("datatemplates/example-trials-data.xlsx");
		assert template.exists();

		new PhenotypeDataImporter()
				.run(template, server, database, username, password, null);

		// Check the overall number of rows
		ServerResult<Long> count = new ValueQuery("SELECT COUNT(1) AS count FROM `phenotypedata`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 33513L);

		// Check the number of rows with recording dates
		count = new ValueQuery("SELECT COUNT(1) AS count FROM `phenotypedata` WHERE NOT ISNULL(recording_date)")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 31951L);

		// Check if the dataset is there and also the location
		count = new ValueQuery("SELECT COUNT(1) AS count FROM `datasets` LEFT JOIN `locations` ON `locations`.`id` = `datasets`.`location_id` WHERE name = 'Trials dataset' AND `locations`.`site_name` = 'Balruddery Farm' AND NOT ISNULL(JSON_SEARCH(dublin_core, 'one', 'CC-BY-SA'))")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 1L);

		// Check the number of non-accessions
		count = new ValueQuery("SELECT COUNT(1) AS count FROM `germinatebase` WHERE `entitytype_id` > 1")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 826L);
	}

	@Test
	public void importCompoundData() throws DatabaseException
	{
		File template = new File("datatemplates/example-compound-data.xlsx");
		assert template.exists();

		new CompoundDataImporter()
				.run(template, server, database, username, password, null);

		// Check the overall number of rows
		ServerResult<Long> count = new ValueQuery("SELECT COUNT(1) AS count FROM `compounddata`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 9998L);
	}

	@Test
	public void importPedigreeData() throws DatabaseException
	{
		File template = new File("datatemplates/example-pedigree-data.xlsx");
		assert template.exists();

		new PedigreeImporter()
				.run(template, server, database, username, password, null);
		new PedigreeStringImporter()
				.run(template, server, database, username, password, null);

		// Check the overall number of rows
		ServerResult<Long> count = new ValueQuery("SELECT COUNT(1) AS count FROM `pedigrees`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 9L);

		count = new ValueQuery("SELECT COUNT(1) AS count FROM `pedigreedefinitions`")
				.setDatabase(db)
				.run("count")
				.getLong(0L);
		assert Objects.equals(count.getServerResult(), 20L);
	}
}