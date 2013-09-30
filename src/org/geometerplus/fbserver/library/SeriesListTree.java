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

public class SeriesListTree extends FirstLevelTree {
	SeriesListTree(IBookCollection collection) {
		super(collection, ROOT_BY_SERIES);
	}

	@Override
	public Status getOpeningStatus() {
		if (!Collection.hasSeries()) {
			return Status.CANNOT_OPEN;
		}
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public String getOpeningStatusMessage() {
		return getOpeningStatus() == Status.CANNOT_OPEN
			? "noSeries" : super.getOpeningStatusMessage();
	}

	@Override
	public void waitForOpening() {
		clear();
		for (String s : Collection.series()) {
			createSeriesSubTree(s);
		}
	}

	@Override
	protected boolean onBookEventInternal(BookEvent event, Book book) {
		switch (event) {
			case Added:
			case Updated:
			{
				// TODO: remove empty series tree after update (?)
				final SeriesInfo info = book.getSeriesInfo();
				// TODO: pass series
				return info != null && createSeriesSubTree(info.Series.getTitle());
			}
			case Removed:
				// TODO: remove empty series tree (?)
				return false;
			default:
				return false;
		}
	}

	private boolean createSeriesSubTree(String seriesTitle) {
		// TODO: pass series as parameter
		final Series series = new Series(seriesTitle);
		final LibraryTree temp = LibraryTreeProvider.getSeriesTree(Collection, series, null);
		if (contains(temp)) {
			return false;
		} else {
			addSubTree(temp);
			return true;
		}
	}
}
