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

public abstract class OPDSItem {

	private static final HashMap<String, OPDSItem> ourIds = new HashMap<String, OPDSItem>();

	public OPDSItem(String id, String title) {
		Id = id;
		Title = title;
	}

	public static boolean save(OPDSItem i) {
		if (!ourIds.containsKey(i.Id)) {
			ourIds.put(i.Id, i);
			return true;
		}
		return false;
	}

	public static OPDSItem get(String id) {
		return ourIds.get(id);
	}

	public abstract String getFeed();
	protected abstract String getEntry();

	public String Id; //Now is also a url
	public String Title;

};
