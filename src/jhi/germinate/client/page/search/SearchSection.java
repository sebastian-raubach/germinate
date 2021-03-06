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

package jhi.germinate.client.page.search;

import com.google.gwt.core.client.*;
import com.google.gwt.i18n.client.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;

import org.gwtbootstrap3.client.shared.event.*;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.*;

import java.util.*;

import jhi.germinate.client.i18n.*;
import jhi.germinate.client.widget.element.*;
import jhi.germinate.shared.*;

/**
 * @author Sebastian Raubach
 */
public class SearchSection extends Composite implements HasWidgets
{
	private static final NumberFormat FORMAT = NumberFormat.getFormat(Text.LANG.pagerNumberFormat());

	interface SearchSectionUiBinder extends UiBinder<Panel, SearchSection>
	{
	}

	private static SearchSectionUiBinder ourUiBinder = GWT.create(SearchSectionUiBinder.class);

	@UiField
	Panel         panel;
	@UiField
	PanelHeader   panelHeader;
	@UiField
	SpanPanel     icon;
	@UiField
	PanelCollapse target;
	@UiField
	Heading       header;
	@UiField
	Progress      progress;
	@UiField
	Label         label;
	@UiField
	PanelBody     body;

	public SearchSection()
	{
		initWidget(ourUiBinder.createAndBindUi(this));

		String id = "search-" + RandomUtils.RANDOM.nextLong();

		target.setId(id);
		panelHeader.setDataTarget("#" + id);
	}

	public void setPreventHideSibling(String preventHideSibling)
	{
		setPreventHideSibling(Boolean.parseBoolean(preventHideSibling));
	}

	public void setPreventHideSibling(boolean preventHideSibling)
	{
		panelHeader.setDataParent(null);
	}

	public void setHeading(String title)
	{
		header.setText(title);
	}

	public void clear()
	{
		body.clear();
		setLabel(0);
		progress.setVisible(false);
	}

	public void addShownHandler(ShownHandler handler)
	{
		target.addShownHandler(handler);
	}

	@Override
	public Iterator<Widget> iterator()
	{
		return body.iterator();
	}

	@Override
	public boolean remove(Widget w)
	{
		return body.remove(w);
	}

	public void add(Widget widget)
	{
		body.add(widget);
	}

	public void setLoading(boolean loading)
	{
		progress.setVisible(loading);
		label.setVisible(!loading);
	}

	public void setMdi(String style)
	{
		icon.setVisible(true);
		icon.addStyleName(Style.mdi(style));
	}

	public void setLabel(Integer value)
	{
		if (value == null)
		{
			label.setVisible(false);
		}
		else
		{
			label.setVisible(true);
			label.setText(FORMAT.format(value));
			label.setType(value > 0 ? LabelType.PRIMARY : LabelType.DEFAULT);
		}
	}
}