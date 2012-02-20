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

package org.geometerplus.android.fbserver;

import android.content.*;
import android.os.*;
import android.util.Log;

import org.geometerplus.android.fbserver.opds.*;
import org.geometerplus.android.fbreader.libraryService.*;

class LibraryServiceConnection implements ServiceConnection {

	public LibraryInterface Iface;

	public void onServiceConnected(ComponentName name, IBinder binder) {
		Log.d("fbserver", "Connected! Name: " + name.getClassName());
		Iface = LibraryInterface.Stub.asInterface(binder);

		OPDSCatalog root = new OPDSCatalog("/", "My Library");
		OPDSItem.save(root);
		BookObject book = null;
		try {
			book = Iface.getRecentBook();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		String path = book.Path;
		int index = path.lastIndexOf(':');
		if (index != -1) {
			path = path.substring(0, index);
		}
		if (path.startsWith("/sdcard")) {
			path = path.substring("/sdcard".length());
		}
		OPDSBook recent = new OPDSBook(path, "Recent book");
		recent.setFilePath(path);
		recent.setType("application/fb2+zip");
		root.addChild(recent);
		OPDSItem.save(recent);
	}

	public void onServiceDisconnected(ComponentName name) {
		Iface = null;
		Log.d("fbserver", "Disconnected!");
	}
}

