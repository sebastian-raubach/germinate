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

package jhi.germinate.client.util;

import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.ui.*;

import org.gwtbootstrap3.client.ui.constants.*;

import java.util.*;
import java.util.stream.*;

import jhi.germinate.client.i18n.Text;
import jhi.germinate.client.page.markeditemlist.*;
import jhi.germinate.client.util.event.*;
import jhi.germinate.client.util.parameterstore.*;
import jhi.germinate.client.widget.structure.*;
import jhi.germinate.shared.*;
import jhi.germinate.shared.Style;
import jhi.germinate.shared.datastructure.*;
import jhi.germinate.shared.enums.*;

/**
 * @author Sebastian Raubach
 */
public final class MarkedItemList
{
	/**
	 * Initializes the {@link MarkedItemList}. This includes handling of click events on the label and reading the previously marked items from the
	 * {@link TypedParameterStore}.
	 */
	public static void init()
	{
		if (GerminateSettingsHolder.isPageAvailable(Page.MARKED_ITEMS))
		{
			reset();

			/* Check if the method has already been called before */
			if (!initialized)
			{
				initialized = true;

				UListElement list = Document.get().createULElement();

				list.addClassName(Style.combine(Styles.DROPDOWN_MENU, Style.BOOTSTRAP_DROPDOWN_ALERT));

				for (final MarkedItemList.ItemType type : MarkedItemList.ItemType.values())
				{
					MarkedItemListItem item = new MarkedItemListItem(type);

					list.appendChild(item.getElement());
				}

				RootPanel p = RootPanel.get(Id.STRUCTURE_MARKED_ITEM_UL);
				p.setVisible(true);
				p.getElement().appendChild(list);
				p.removeFromParent();
			}
		}
		else
		{
			JavaScript.remove("#" + Id.STRUCTURE_MARKED_ITEM_UL);
		}
	}

	private static boolean initialized = false;

	private static Map<ItemType, Set<String>> IDS = new HashMap<>();

	private MarkedItemList()
	{

	}

	public enum ItemType
	{
		ACCESSION(Parameter.markedAccessionIds, Text.LANG.searchAccessions(), GerminateDatabaseTable.germinatebase, Style.MDI_FLOWER),
		MARKER(Parameter.markedMarkerIds, Text.LANG.searchMarkers(), GerminateDatabaseTable.markers, Style.MDI_DNA),
		LOCATION(Parameter.markedCollectingsiteIds, Text.LANG.searchCollectingsites(), GerminateDatabaseTable.locations, Style.MDI_MAP_MARKER);

		private final Parameter              parameter;
		private final String                 displayName;
		private final GerminateDatabaseTable target;
		private final String                 icon;

		ItemType(Parameter parameter, String displayName, GerminateDatabaseTable target, String icon)
		{
			this.parameter = parameter;
			this.displayName = displayName;
			this.target = target;
			this.icon = icon;
		}

		public void setMarkedIds(Set<String> items)
		{
			StringListParameterStore.Inst.get().put(parameter, new ArrayList<>(items));
		}

		public List<String> getMarkedIds()
		{
			return StringListParameterStore.Inst.get().get(parameter);
		}

		public String getMarkedIdString() {
			return StringParameterStore.Inst.get().get(parameter);
		}

		public Parameter getParameter()
		{
			return parameter;
		}

		public String getDisplayName()
		{
			return displayName;
		}

		public String getIcon()
		{
			return icon;
		}

		public GerminateDatabaseTable getTarget()
		{
			return target;
		}

		public AbstractCartView getContent()
		{
			switch (this)
			{
				case ACCESSION:
					return new AccessionCartView();
				case MARKER:
					return new MarkerCartView();
				case LOCATION:
					return new CollectingsiteCartView();
				default:
					return null;
			}
		}
	}

	/**
	 * Resets the shopping cart content, i.e. re-loads the save ids from the parameter store
	 */
	public static void reset()
	{
		for (ItemType type : ItemType.values())
		{
			IDS.put(type, new HashSet<>());
			IDS.get(type).addAll(type.getMarkedIds());
		}

		/* Extend the cookie */
		Cookie.extend(Parameter.markedAccessionIds.name(), false);
		Cookie.extend(Parameter.markedMarkerIds.name(), false);
		Cookie.extend(Parameter.markedCollectingsiteIds.name(), false);
	}

	/**
	 * Checks if the given accession id have already been marked
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @param id   The id to check
	 * @return <code>true</code> if the given id has already been marked
	 */
	public static boolean contains(ItemType type, String id)
	{
		return IDS.get(type).contains(id);
	}

	/**
	 * Checks if the given accession ids have already been marked
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @param ids  The ids to check
	 * @return <code>true</code> if all of the given ids have already been marked
	 */
	public static boolean contains(ItemType type, Collection<String> ids)
	{
		if (CollectionUtils.isEmpty(ids))
			return false;

		for (String id : ids)
		{
			if (!contains(type, id))
				return false;
		}

		return true;
	}

	/**
	 * Toggles the givem id for the given {@link ItemType}. Toggling means that it'll remove it if present and add it if absent.
	 *
	 * @param itemType The {@link ItemType}
	 * @param idString The id of the item
	 */
	public static void toggle(ItemType itemType, String idString)
	{
		if (contains(itemType, idString))
		{
			remove(itemType, idString);
		}
		else
		{
			add(itemType, idString);
		}
	}

	/**
	 * Sets the marked accession ids
	 *
	 * @param type       The {@link MarkedItemList.ItemType}
	 * @param theNewGuys The new marked ids
	 */
	public static void set(ItemType type, Collection<String> theNewGuys)
	{
		IDS.put(type, new HashSet<>(theNewGuys));

		type.setMarkedIds(IDS.get(type));

		GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, theNewGuys));
	}

	/**
	 * Gets the marked accession ids
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @return The marked ids
	 */
	public static Set<String> get(ItemType type)
	{
		return IDS.get(type);
	}

	public static int getSize(ItemType type) {
		return IDS.get(type).size();
	}

	/**
	 * Gets the marked accession ids
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @return The marked ids
	 */
	public static Set<Long> getAsLong(ItemType type)
	{
		return IDS.get(type)
				  .stream()
				  .map(s ->
				  {
					  try
					  {
						  return Long.parseLong(s);
					  }
					  catch (Exception e)
					  {
						  return null;
					  }
				  })
				  .filter(Objects::nonNull)
				  .collect(Collectors.toSet());
	}

	/**
	 * Adds the given id and marks it
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @param id   The id to mark
	 * @return <code>true</code> if the id has successfully been added
	 */
	public static boolean add(ItemType type, String id)
	{
		boolean result = false;
		if (!StringUtils.isEmpty(id))
		{
			result = IDS.get(type).add(id);
		}

		type.setMarkedIds(IDS.get(type));

		if (!result)
		{
			Notification.notify(Notification.Type.ERROR, Text.LANG.notificationCartAlreadyMarked());
		}

		GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, Collections.singletonList(id)));

		return result;
	}

	/**
	 * Adds the given ids to the cart
	 *
	 * @param type   The {@link MarkedItemList.ItemType}
	 * @param newIds The new ids
	 * @return <code>true</code> if at least one item has been added
	 */
	public static boolean add(ItemType type, Collection<String> newIds)
	{
		if (CollectionUtils.isEmpty(newIds))
			return false;

		boolean result = false;
		for (String id : newIds)
		{
			if (!StringUtils.isEmpty(id))
				result |= IDS.get(type).add(id);
		}

		type.setMarkedIds(IDS.get(type));

		if (!result)
		{
			Notification.notify(Notification.Type.ERROR, Text.LANG.notificationCartAlreadyMarked());
		}

		GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, newIds));

		return result;
	}

	/**
	 * Removes the given ids from the cart
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @param id   The id to remove
	 * @return <code>true</code> if at least one item has been removed
	 */
	public static boolean remove(ItemType type, String id)
	{
		boolean result = false;
		if (!StringUtils.isEmpty(id))
			result = IDS.get(type).remove(id);

		type.setMarkedIds(IDS.get(type));

		GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, Collections.singletonList(id)));

		return result;
	}

	/**
	 * Removes the given ids from the cart
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 * @param ids  The ids to remove
	 * @return <code>true</code> if at least one item has been removed
	 */
	public static boolean remove(ItemType type, Collection<String> ids)
	{
		if (CollectionUtils.isEmpty(ids))
			return false;

		boolean result = false;
		for (String id : ids)
		{
			if (!StringUtils.isEmpty(id))
				result |= IDS.get(type).remove(id);
		}

		type.setMarkedIds(IDS.get(type));

		GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, ids));

		return result;
	}

	/**
	 * Clears the {@link MarkedItemList} for a given type and updates the view
	 *
	 * @param type The {@link MarkedItemList.ItemType}
	 */
	public static void clear(ItemType type)
	{
		IDS.get(type).clear();
		type.setMarkedIds(new HashSet<>());

		GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, null));
	}

	/**
	 * Clears the {@link MarkedItemList} for all types and updates the view
	 */
	public static void clearAll()
	{
		for (ItemType type : ItemType.values())
		{
			IDS.get(type).clear();
			type.setMarkedIds(new HashSet<>());

			GerminateEventBus.BUS.fireEvent(new MarkedItemListEvent(type, null));
		}
	}
}
