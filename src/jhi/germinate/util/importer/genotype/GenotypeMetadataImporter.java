/**
 * Germinate 3 is written and developed by Sebastian Raubach and Paul Shaw from the Information and Computational Sciences Group at JHI Dundee. For
 * further information contact us at germinate@hutton.ac.uk or visit our webpages at https://ics.hutton.ac.uk/germinate
 *
 * Copyright © 2005-2017, Information & Computational Sciences, The James Hutton Institute. All rights reserved. Use is subject to the accompanying
 * licence terms.
 */

package jhi.germinate.util.importer.genotype;

import java.io.*;
import java.io.IOException;
import java.util.*;

import jhi.germinate.server.database.*;
import jhi.germinate.shared.*;
import jhi.germinate.shared.datastructure.database.*;
import jhi.germinate.shared.exception.*;
import jhi.germinate.util.importer.reader.*;

/**
 * {@link GenotypeMetadataImporter} uses an {@link IDataReader} to read and parse the {@link Dataset} object and then writes it to a Germinate
 * database.
 *
 * @author Sebastian Raubach
 */
public class GenotypeMetadataImporter extends DataImporter<Dataset>
{
	private static Set<Long> createdDatasetIds    = new HashSet<>();
	private static Set<Long> createdExperimentIds = new HashSet<>();

	private Dataset dataset;
	private File    hdf5File;

	public static void main(String[] args)
	{
		new GenotypeMetadataImporter()
				.run(args);
	}

	@Override
	protected IDataReader getFallbackReader()
	{
		return new ExcelGenotypeMetadataReader();
	}

	@Override
	protected void deleteInsertedItems()
	{
		deleteItems(createdDatasetIds, "datasets");
		deleteItems(createdExperimentIds, "experiments");

		if (hdf5File != null && hdf5File.exists())
			hdf5File.delete();
	}

	@Override
	protected void write(Dataset entry) throws DatabaseException
	{
		try
		{
			createOrGetExperiment(entry);
			createDataset(entry);
		}
		catch (IOException e)
		{
			throw new DatabaseException(e);
		}
	}

	/**
	 * Imports the {@link Experiment} object into the database if it doesn't already exist, otherwise returns the existing object from the database.
	 *
	 * @param entry The {@link Dataset} object containing the {@link Experiment} to import.
	 * @throws DatabaseException Thrown if the interaction with the database fails.
	 */
	private void createOrGetExperiment(Dataset entry) throws DatabaseException
	{
		String name = entry.getExperiment().getName();

		if (StringUtils.isEmpty(name))
			return;

		DatabaseStatement stmt = databaseConnection.prepareStatement("SELECT id FROM experiments WHERE experiment_name = ?");
		stmt.setString(1, name);

		DatabaseResult rs = stmt.query();

		if (rs.next())
		{
			entry.setExperiment(Experiment.Parser.Inst.get().parse(rs, null, true));
		}
		else
		{
			Experiment.Writer.Inst.get().write(databaseConnection, entry.getExperiment());
			createdExperimentIds.add(entry.getExperiment().getId());
		}
	}

	/**
	 * Imports the {@link Dataset} object into the database if it doesn't already exist, otherwise returns the existing object from the database.
	 *
	 * @param entry The {@link Dataset} object to import.
	 * @throws DatabaseException Thrown if the interaction with the database fails.
	 */
	private void createDataset(Dataset entry) throws DatabaseException, IOException
	{
		String name = entry.getDescription();

		if (StringUtils.isEmpty(name) || entry.getExperiment() == null)
			throw new DatabaseException("Invalid experiment or dataset name.");

		hdf5File = File.createTempFile("germinate_genotype_", ".hdf5");

		dataset = entry;
		dataset.setSourceFile(hdf5File.getName());
		Dataset.Writer.Inst.get().write(databaseConnection, dataset);
		createdDatasetIds.add(dataset.getId());
	}

	public Dataset getDataset()
	{
		return dataset;
	}

	public File getHdf5File()
	{
		return hdf5File;
	}
}
