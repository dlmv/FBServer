/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbserver.library;

import org.geometerplus.fbserver.book.IBookCollection;


public class RootTree extends LibraryTree {
	
	private String myName;
	
	public RootTree(IBookCollection collection, String name) {
		super(collection);
		myName = name;
		addSubTree(LibraryTreeProvider.getAuthorListTree(Collection));
		addSubTree(LibraryTreeProvider.getTitleListTree(Collection));
		addSubTree(LibraryTreeProvider.getSeriesListTree(Collection));
		addSubTree(LibraryTreeProvider.getTagListTree(Collection));
	}

//	public SearchResultsTree createSearchResultsTree(String pattern) {
//		return new SearchResultsTree(Collection, LibraryTree.ROOT_FOUND, pattern);
//	}

	@Override
	public String getName() {
		return myName;
	}

	@Override
	public String getSummary() {
		return resource().getValue();
	}

	@Override
	public String getUniqueId() {
		return "root";
	}

}
