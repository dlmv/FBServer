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

public class OPDSCreator {

	private static OPDSCreator ourInstance;

	private static final String FEED_TEMPLATE_FILE = "FEED.template";
	private static final String BOOKENTRY_TEMPLATE_FILE = "BOOKENTRY.template";
	private static final String CATALOGENTRY_TEMPLATE_FILE = "CATALOGENTRY.template";

	private String myFeedTemplate;
	private String myBookEntryTemplate;
	private String myCatalogEntryTemplate;

	public static void init(Context context) {
		if (ourInstance == null) {
			ourInstance = new OPDSCreator(context);
		}
	}

	public static OPDSCreator getInstance() {
		return ourInstance;
	}

	private static String convertStreamToString(InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	private OPDSCreator(Context context) {
		AssetManager mgr = context.getAssets();
		try {
			myFeedTemplate = convertStreamToString(mgr.open(FEED_TEMPLATE_FILE));
			myBookEntryTemplate = convertStreamToString(mgr.open(BOOKENTRY_TEMPLATE_FILE));
			myCatalogEntryTemplate = convertStreamToString(mgr.open(CATALOGENTRY_TEMPLATE_FILE));
		} catch (IOException e) {
		}
	}

	String createFeed(OPDSCatalog o, String iconUrl) {
		String entries = "";
		for (OPDSItem i : o.getChildren()) {
			entries = entries + i.getEntry();
		}
		return myFeedTemplate
			.replace("%ID%", o.Id)
			.replace("%TITLE%", o.Title)
			.replace("%START%", OPDSServer.ROOT_URL)
			.replace("%ICON%", iconUrl)
			.replace("%ENTRIES%", entries);
	}

	String createEntry(OPDSBook o) {
		return myBookEntryTemplate
			.replace("%ID%", o.Id)
			.replace("%TITLE%", o.Title)
			.replace("%LINK%", o.Id)
			.replace("%TYPE%", o.getType());
	}

	String createEntry(OPDSCatalog o) {
		return myCatalogEntryTemplate
			.replace("%ID%", o.Id)
			.replace("%TITLE%", o.Title)
			.replace("%LINK%", o.Id);
	}

}
