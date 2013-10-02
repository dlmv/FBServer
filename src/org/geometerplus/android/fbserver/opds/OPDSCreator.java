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

import java.io.*;

import org.geometerplus.fbserver.library.BookTree;
import org.geometerplus.fbserver.library.LibraryTree;
import org.geometerplus.fbserver.tree.FBTree;

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

	static String createFeed(LibraryTree o, String iconUrl) {
		String entries = "";
		for (FBTree i : o.subTrees()) {
			entries = entries + createEntry(i);
		}
		return ourFeedTemplate
				.replace("%ID%", o.getEncodedId())
				.replace("%TITLE%", o.getName())
				.replace("%START%", OPDSServer.ROOT_URL)
				.replace("%ICON%", iconUrl)
				.replace("%ENTRIES%", entries);
	}

	static String createEntry(FBTree o) {
		if (o instanceof LibraryTree) {
			if (o instanceof BookTree) {
				return createBookEntry((BookTree) o);
			}
			return createCatalogEntry((LibraryTree) o);
		}
		return "";
	}

	static String createBookEntry(BookTree o) {
		return ourBookEntryTemplate
				.replace("%ID%",  o.getEncodedId())
				.replace("%TITLE%", o.getName())
				.replace("%SUMMARY%", o.getSummary())
				.replace("%LINK%",  "/" + o.getEncodedId())
				.replace("%TYPE%", o.getBook().MimeType);//TODO
	}

	static String createCatalogEntry(LibraryTree o) {
		return ourCatalogEntryTemplate
				.replace("%ID%", o.getEncodedId())
				.replace("%TITLE%", o.getName())
				.replace("%SUMMARY%", o.getSummary())
				.replace("%LINK%", "/" + o.getEncodedId());
	}

}
