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

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbserver.book.*;
import org.geometerplus.fbserver.tree.FBTree;

import android.util.Log;

public abstract class LibraryTree extends FBTree {
	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

	public final IBookCollection Collection;

	static final String ROOT_FOUND = "found";
	static final String ROOT_BY_AUTHOR = "byAuthor";
	static final String ROOT_BY_TITLE = "byTitle";
	static final String ROOT_BY_SERIES = "bySeries";
	static final String ROOT_BY_TAG = "byTag";

	protected LibraryTree(IBookCollection collection) {
		super();
		Collection = collection;
	}

	public Book getBook() {
		return null;
	}

	public boolean containsBook(Book book) {
		return false;
	}

	public boolean isSelectable() {
		return true;
	}

	boolean createTagSubTree(Tag tag) {
		final LibraryTree temp = LibraryTreeProvider.getTagTree(Collection, tag);
		if (contains(temp)) {
			return false;
		} else {
			addSubTree(temp);
			return true;
		}
	}

	boolean createBookTree(Book book) {
		final LibraryTree temp = LibraryTreeProvider.getBookTree(Collection, book);
		if (contains(temp)) {
			return false;
		} else {
			addSubTree(temp);
			return true;
		}
	}
	
	public void init() {
		waitForOpening();
		for (FBTree t : subTrees()) {
			if (t instanceof LibraryTree) {
				((LibraryTree) t).init();
			}
		}
	}

	public void onBookEvent(BookEvent event, Book book) {
		onBookEventInternal(event, book);
		for (FBTree t : subTrees()) {
			if (t instanceof LibraryTree) {
				((LibraryTree) t).onBookEvent(event, book);
			}
		}
	}
	
	protected boolean removeBook(Book b) {
		return removeSubTree(LibraryTreeProvider.getBookTree(Collection, b));
	}

	protected boolean onBookEventInternal(BookEvent event, Book book) {
		switch (event) {
			default:
			case Added:
				return false;
			case Removed:
				return removeBook(book);
			case Updated:
			{
				boolean changed = false;
				for (FBTree tree : subTrees()) {
					if (tree instanceof BookTree) {
						final Book b = ((BookTree)tree).Book;
						if (b.equals(book)) {
							b.updateFrom(book);
							changed = true;
						}
					}
				}
				return changed;
			}
		}
	}
	
	@Override
	public int compareTo(FBTree tree) {
		final int cmp = super.compareTo(tree);
		if (cmp == 0) {
			return getClass().getSimpleName().compareTo(tree.getClass().getSimpleName());
		}
		return cmp;
	}
}
