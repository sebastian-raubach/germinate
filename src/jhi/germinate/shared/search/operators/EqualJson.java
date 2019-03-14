/*
 *  Copyright 2019 Information and Computational Sciences,
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

package jhi.germinate.shared.search.operators;

import java.util.*;

import jhi.germinate.shared.*;
import jhi.germinate.shared.exception.*;
import jhi.germinate.shared.search.*;
import jhi.germinate.shared.search.SearchCondition.*;

/**
 * Implementation of the = operator
 *
 * @author Toby Philp
 * @author Sebastian Raubach
 */
public class EqualJson implements ComparisonOperator
{
	private static final long serialVersionUID = -6996059089452093921L;

	private String columnName;

	@Override
	public String getColumnName()
	{
		return columnName;
	}

	@Override
	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	@Override
	public RequiredNumberOfValues getRequiredNumberOfvalues()
	{
		return RequiredNumberOfValues.ONE;
	}

	@Override
	public DataType[] getAllowedTypesOfValue()
	{
		return new DataType[]{DataType.ANY};
	}

	@Override
	public String toString()
	{
		return " = ";
	}

	@Override
	public String toPreparedStatementString(List<String> values) throws InvalidArgumentException
	{
		if (values.size() < 1)
		{
			throw new InvalidArgumentException("Equal to requires that one comparison value is provided.");
		}
		else if (StringUtils.isEmpty(values.get(0)))
		{
			throw new InvalidArgumentException("Equal to requires that one comparison value is provided.");
		}

		return " NOT ISNULL(JSON_SEARCH(LOWER(" + columnName + "), 'one', LOWER(?)))";
	}

	@Override
	public List<String> getValues(List<String> values)
	{
		return values;
	}

	@Override
	public KeyValuePairCompatibility getKeyValuePairCompatibility()
	{
		return KeyValuePairCompatibility.NOT_COMPATIBLE;
	}
}