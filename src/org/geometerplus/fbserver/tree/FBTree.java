/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbserver.tree;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public abstract class FBTree implements Comparable<FBTree> {
	
	private volatile TreeSet<FBTree> mySubTrees = new TreeSet<FBTree>();

	public static enum Status {
		READY_TO_OPEN,
		WAIT_FOR_OPEN,
		ALWAYS_RELOAD_BEFORE_OPENING,
		CANNOT_OPEN
	};

	public abstract String getUniqueId();
	
	public String getEncodedId() {
		try {
			return URLEncoder.encode(getUniqueId(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return getUniqueId();
		}
	}


	public abstract String getName();

	public String getTreeTitle() {
		return getName();
	}
	
	public List<FBTree> subTrees() {
		if (mySubTrees == null) {
			return Collections.emptyList();
		}
		synchronized (mySubTrees) {
			return new ArrayList<FBTree>(mySubTrees);
		}
	}
	
	public final void clear() {
		if (mySubTrees != null) {
			mySubTrees.clear();
		}
	}
	
	public void addSubTree(FBTree t) {
		mySubTrees.add(t);
	}
	
	public boolean removeSubTree(FBTree t) {
		return mySubTrees.remove(t);
	}
	
	public boolean contains(FBTree t) {
		return mySubTrees.contains(t);
	}


	protected String getSortKey() {
		final String sortKey = getName();
		if (sortKey == null ||
			sortKey.length() <= 1 ||
			Character.isLetterOrDigit(sortKey.charAt(0))) {
			return sortKey;
		}

		for (int i = 1; i < sortKey.length(); ++i) {
			if (Character.isLetterOrDigit(sortKey.charAt(i))) {
				return sortKey.substring(i);
			}
		}
		return sortKey;
	}

	private static int compareStringsIgnoreCase(String s0, String s1) {
	  	final int len = Math.min(s0.length(), s1.length());
		for (int i = 0; i < len; ++i) {
		  	char c0 = s0.charAt(i);
		  	char c1 = s1.charAt(i);
			if (c0 == c1) {
			  	continue;
			}
			c0 = Character.toLowerCase(c0);
			c1 = Character.toLowerCase(c1);
			if (c0 == c1) {
			  	continue;
			}
			return c0 - c1;
		}
		if (s0.length() > len) {
		  	return 1;
		}
		if (s0.length() > len) {
		  	return -1;
		}
		return 0;
	}

	@Override
	public int compareTo(FBTree tree) {
		final String key0 = getSortKey();
		final String key1 = tree.getSortKey();
		if (key0 == null) {
			return (key1 == null) ? 0 : -1;
		}
		if (key1 == null) {
			return 1;
		}
		final int diff = compareStringsIgnoreCase(key0, key1);
		return diff != 0 ? diff : getName().compareTo(tree.getName());
	}

	public abstract String getSummary();


	public Status getOpeningStatus() {
		return Status.READY_TO_OPEN;
	}

	public String getOpeningStatusMessage() {
		return null;
	}

	public void waitForOpening() {
	}
}
