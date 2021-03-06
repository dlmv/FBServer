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

public final class SeriesTree extends FilteredTree {
	public final Series Series;

	private static Filter filter(Series series, Author author) {
		final Filter f = new Filter.BySeries(series);
		return author != null ? new Filter.And(f, new Filter.ByAuthor(author)) : f;
	}

	SeriesTree(IBookCollection collection, Series series, Author author) {
		super(collection, filter(series, author));
		Series = series;
	}

	@Override
	public String getName() {
		return Series.getTitle();
	}

	@Override
	public String getUniqueId() {
		return "series_" + Series.getSortKey();
	}

	@Override
	protected String getSortKey() {
		if (Author.NULL.equals(Series)) {
			return null;
		}
		return new StringBuilder()
			.append("Series:")
			.append(Series.getSortKey())
			.append(":")
			.append(Series.getTitle())
			.toString();
	}

	@Override
	protected boolean createSubTree(Book book) {
		final LibraryTree temp = LibraryTreeProvider.getBookTree(Collection, book);
		if (contains(temp)) {
			return false;
		} else {
			addSubTree(temp);
			return true;
		}
	}
}
