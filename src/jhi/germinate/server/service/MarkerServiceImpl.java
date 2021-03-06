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

package jhi.germinate.server.service;

import java.io.*;
import java.util.*;

import javax.servlet.annotation.*;

import jhi.germinate.client.service.*;
import jhi.germinate.server.database.query.*;
import jhi.germinate.server.manager.*;
import jhi.germinate.server.util.*;
import jhi.germinate.shared.*;
import jhi.germinate.shared.datastructure.*;
import jhi.germinate.shared.datastructure.database.*;
import jhi.germinate.shared.enums.*;
import jhi.germinate.shared.exception.*;
import jhi.germinate.shared.exception.IOException;
import jhi.germinate.shared.search.*;

/**
 * {@link MarkerServiceImpl} is the implementation of {@link MarkerService}.
 *
 * @author Sebastian Raubach
 */
@WebServlet(urlPatterns = {"/germinate/marker"})
public class MarkerServiceImpl extends BaseRemoteServiceServlet implements MarkerService
{
	private static final long serialVersionUID = 3127051583642953437L;

	private static final String QUERY_MARKER_DATA_WITH_NAMES = "SELECT `mapdefinitions`.`chromosome`, `mapdefinitions`.`definition_start`, `mapfeaturetypes`.`description`, `markers`.`id`, `markers`.`marker_name` FROM `mapdefinitions`, `mapfeaturetypes`, `markers`, `maps` WHERE `mapdefinitions`.`mapfeaturetype_id` = `mapfeaturetypes`.`id` AND `mapdefinitions`.`marker_id` = `markers`.`id` AND `maps`.`id` = `mapdefinitions`.`map_id` AND (`maps`.`user_id` = ? OR `maps`.`visibility` = 1) AND `markers`.`marker_name` IN (%s)";

	@Override
	public ServerResult<String> export(RequestProperties properties, PartialSearchQuery filter) throws InvalidSessionException, DatabaseException, IOException, InvalidArgumentException, InvalidSearchQueryException, InvalidColumnException
	{
		Session.checkSession(properties, this);
		UserAuth userAuth = UserAuth.getFromSession(this, properties);

		DefaultStreamer streamer = MapDefinitionManager.getStreamerForFilter(userAuth, filter, new Pagination(0, Integer.MAX_VALUE));

		File result = createTemporaryFile("download-markers", FileType.txt.name());

		try
		{
			Util.writeDefaultToFile(Util.getOperatingSystem(getThreadLocalRequest()), null, streamer, result);
		}
		catch (java.io.IOException e)
		{
			throw new IOException(e);
		}

		return new ServerResult<>(streamer.getDebugInfo(), result.getName());
	}

	@Override
	public PaginatedServerResult<List<Marker>> getMarkerForFilter(RequestProperties properties, Pagination pagination, PartialSearchQuery filter) throws InvalidSessionException, DatabaseException, InvalidColumnException, InvalidArgumentException, InvalidSearchQueryException
	{
		Session.checkSession(properties, this);
		UserAuth userAuth = UserAuth.getFromSession(this, properties);
		return MarkerManager.getForFilter(userAuth, pagination, filter);
	}

	@Override
	public PaginatedServerResult<List<MapDefinition>> getMapDefinitionForFilter(RequestProperties properties, Pagination pagination, PartialSearchQuery filter) throws InvalidSessionException,
			DatabaseException, InvalidColumnException, InvalidArgumentException, InvalidSearchQueryException
	{
		Session.checkSession(properties, this);
		UserAuth userAuth = UserAuth.getFromSession(this, properties);
		return MapDefinitionManager.getForFilter(userAuth, pagination, filter);
	}

	@Override
	public ServerResult<String> export(RequestProperties properties, Set<String> markerNames) throws InvalidSessionException, DatabaseException, IOException
	{
		Session.checkSession(properties, this);
		UserAuth userAuth = UserAuth.getFromSession(this, properties);

		String query = String.format(QUERY_MARKER_DATA_WITH_NAMES, StringUtils.generateSqlPlaceholderString(markerNames.size()));

		DefaultStreamer data = new DefaultQuery(query, userAuth)
				.setLong(properties.getUserId())
				.setStrings(markerNames)
				.getStreamer();

		File file = createTemporaryFile("markers", FileType.txt.name());

		try
		{
			Util.writeDefaultToFile(Util.getOperatingSystem(getThreadLocalRequest()), null, data, file);
		}
		catch (java.io.IOException e)
		{
			throw new IOException(e);
		}

		return new ServerResult<>(data.getDebugInfo(), file.getName());
	}

	@Override
	public ServerResult<List<Marker>> getByIds(RequestProperties properties, Pagination pagination, List<String> ids) throws InvalidSessionException, DatabaseException, InvalidColumnException
	{
		if (pagination == null)
			pagination = Pagination.getDefault();

		Session.checkSession(properties, this);
		UserAuth userAuth = UserAuth.getFromSession(this, properties);
		return MarkerManager.getByIds(userAuth, ids, pagination);
	}

	@Override
	public ServerResult<List<String>> getIdsForFilter(RequestProperties properties, PartialSearchQuery filter) throws InvalidSessionException,
			DatabaseException, InvalidColumnException, InvalidSearchQueryException, InvalidArgumentException
	{
		Session.checkSession(properties, this);
		UserAuth userAuth = UserAuth.getFromSession(this, properties);
		return MarkerManager.getIdsForFilter(userAuth, filter);
	}
}
