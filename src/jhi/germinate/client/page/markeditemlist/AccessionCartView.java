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

package jhi.germinate.client.page.markeditemlist;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.rpc.*;

import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.*;

import java.util.*;

import jhi.germinate.client.i18n.*;
import jhi.germinate.client.service.*;
import jhi.germinate.client.util.*;
import jhi.germinate.client.widget.table.pagination.*;
import jhi.germinate.shared.*;
import jhi.germinate.shared.datastructure.*;
import jhi.germinate.shared.datastructure.Pagination;
import jhi.germinate.shared.datastructure.database.*;
import jhi.germinate.shared.enums.*;
import jhi.germinate.shared.search.*;

/**
 * @author Sebastian Raubach
 */
public class AccessionCartView extends AbstractCartView<Accession>
{
	@Override
	protected MarkedItemList.ItemType getItemType()
	{
		return MarkedItemList.ItemType.ACCESSION;
	}

	@Override
	protected void writeToFile(List<String> markedIds, AsyncCallback<ServerResult<String>> callback)
	{
		GroupService.Inst.get().exportForIds(Cookie.getRequestProperties(), markedIds, GerminateDatabaseTable.germinatebase, callback);
	}

	@Override
	protected AccessionTable getTable(final List<String> markedIds)
	{
		return new AccessionTable(DatabaseObjectPaginationTable.SelectionMode.NONE, true)
		{
			@Override
			protected Request getData(Pagination pagination, PartialSearchQuery filter, final AsyncCallback<PaginatedServerResult<List<Accession>>> callback)
			{
				return AccessionService.Inst.get().getByIds(Cookie.getRequestProperties(), pagination, markedIds, new AsyncCallback<ServerResult<List<Accession>>>()
				{
					@Override
					public void onFailure(Throwable caught)
					{
						callback.onFailure(caught);
					}

					@Override
					public void onSuccess(ServerResult<List<Accession>> result)
					{
						callback.onSuccess(new PaginatedServerResult<>(result.getDebugInfo(), result.getServerResult(), markedIds.size()));
					}
				});
			}

			@Override
			protected boolean preventAllItemMarking()
			{
				return true;
			}

			@Override
			protected boolean supportsFiltering()
			{
				return false;
			}
		};
	}

	@Override
	protected void setUpContent()
	{
		super.setUpContent();
		String markedIds = getItemType().getMarkedIdString();

		String url = GerminateSettingsHolder.get().templateMarkedAccessionUrl.getValue();

		if (!StringUtils.isEmpty(url, markedIds))
		{
			url = url.replace("{{IDS}}", markedIds);
			panel.add(new Heading(HeadingSize.H3, Text.LANG.markedItemsAccessionExport()));
			Anchor anchor = new Anchor();
			anchor.setText(Text.LANG.markedItemsAccessionExport());
			anchor.addStyleName(Style.combine(Styles.BTN, ButtonType.PRIMARY.getCssName(), Style.mdiLg(Style.MDI_EXPORT)));
			anchor.setHref(url);
			anchor.setTarget("_blank");
			panel.add(anchor);
		}
	}
}
