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

import org.geometerplus.fbserver.book.*;

import android.util.Log;

public class AuthorTree extends FilteredTree {
	public final Author Author;

	AuthorTree(IBookCollection collection, Author author) {
		super(collection, new Filter.ByAuthor(author));
		Author = author;
	}

	@Override
	public String getName() {
		return Author.NULL.equals(Author)
				? resource().getResource("unknownAuthor").getValue() : Author.DisplayName;
	}

	@Override
	public String getUniqueId() {
		return "author_" + getSortKey();
	}

	@Override
	protected String getSortKey() {
		if (Author.NULL.equals(Author)) {
			return null;
		}
		return new StringBuilder()
		.append("author:")
		.append(Author.SortKey)
		.append(":")
		.append(Author.DisplayName)
		.toString();
	}

	private SeriesTree getSeriesSubTree(Series series) {
		final LibraryTree temp = LibraryTreeProvider.getSeriesTree(Collection, series, Author);
		if (contains(temp)) {
		} else {
			addSubTree(temp);
		}
		return (SeriesTree) temp;
	}

	@Override
	protected boolean createSubTree(Book book) {
		final SeriesInfo seriesInfo = book.getSeriesInfo();
		if (seriesInfo != null) {
			return getSeriesSubTree(seriesInfo.Series).createSubTree(book);
		}
		final LibraryTree temp = LibraryTreeProvider.getBookTree(Collection, book);
		if (contains(temp)) {
			return false;
		} else {
			addSubTree(temp);
			return true;
		}
	}
}
