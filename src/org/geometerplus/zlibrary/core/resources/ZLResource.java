/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.resources;

import org.geometerplus.android.fbreader.api.ApiClientImplementation;
import org.geometerplus.android.fbreader.api.ApiException;

public class ZLResource {
	public final String Name;
	
	public static ApiClientImplementation Api = null;
	public static boolean ConnectedToApi = false;
	
	public static void connect() {
		ConnectedToApi = true;
	}

	public static ZLResource resource(String key) {
		return new ZLResource(key);
	}

	protected ZLResource(String name) {
		Name = name;
	}

	public String getValue() {
		if (ConnectedToApi) {
			try {
				return Api.getResourceValue(Name);
			} catch (ApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Name;
	}

	public ZLResource getResource(String key) {
		return new ZLResource(Name + "/" + key);
	}
}
