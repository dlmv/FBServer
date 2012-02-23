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

import android.content.res.AssetManager;
import android.content.Context;

import java.util.*;
import java.io.*;

public abstract class OPDSCreator {

	private static final String FEED_TEMPLATE_FILE = "FEED.template";
	private static final String BOOKENTRY_TEMPLATE_FILE = "BOOKENTRY.template";
	private static final String CATALOGENTRY_TEMPLATE_FILE = "CATALOGENTRY.template";

	private static String ourFeedTemplate;
	private static String ourBookEntryTemplate;
	private static String ourCatalogEntryTemplate;

	public static void init(Context context) {
		AssetManager mgr = context.getAssets();
		try {
			ourFeedTemplate = convertStreamToString(mgr.open(FEED_TEMPLATE_FILE));
			ourBookEntryTemplate = convertStreamToString(mgr.open(BOOKENTRY_TEMPLATE_FILE));
			ourCatalogEntryTemplate = convertStreamToString(mgr.open(CATALOGENTRY_TEMPLATE_FILE));
		} catch (IOException e) {
		}
	}

	private static String convertStreamToString(InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	static String createFeed(OPDSCatalog o, String iconUrl) {
		String entries = "";
		for (OPDSItem i : o.getChildren()) {
			entries = entries + i.getEntry();
		}
		return ourFeedTemplate
			.replace("%ID%", o.Id)
			.replace("%TITLE%", o.Title)
			.replace("%START%", OPDSServer.ROOT_URL)
			.replace("%ICON%", iconUrl)
			.replace("%ENTRIES%", entries);
	}

	static String createEntry(OPDSBook o) {
		return ourBookEntryTemplate
			.replace("%ID%", o.Id)
			.replace("%TITLE%", o.Title)
			.replace("%LINK%", o.Id)
			.replace("%TYPE%", o.getType());
	}

	static String createEntry(OPDSCatalog o) {
		return ourCatalogEntryTemplate
			.replace("%ID%", o.Id)
			.replace("%TITLE%", o.Title)
			.replace("%LINK%", o.Id);
	}

}
