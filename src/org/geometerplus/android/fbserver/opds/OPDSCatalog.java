/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbserver.opds;

import java.util.*;

public class OPDSCatalog extends OPDSItem {

	private ArrayList<OPDSItem> myChildren = new ArrayList<OPDSItem>();

	public OPDSCatalog(String id, String title) {
		super(id, title);
	}

	public void setChildren(List<OPDSItem> ch) {
		myChildren = new ArrayList(ch);
	}

	public void addChild(OPDSItem ch) {
		myChildren.add(ch);
	}

	public List<OPDSItem> getChildren() {
		return myChildren;
	}

	public String getFeed() {
		return OPDSCreator.getInstance().createFeed(this);
	}

	protected String getEntry() {
		return OPDSCreator.getInstance().createEntry(this);
	}

}
