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

public final class TitleTree extends FilteredTree {
	public final String Prefix;

	TitleTree(IBookCollection collection, String prefix) {
		super(collection, new Filter.ByTitlePrefix(prefix));
		Prefix = prefix;
	}

	@Override
	public String getName() {
		return Prefix;
	}

	@Override
	public String getUniqueId() {
		return "prefix_" + getName();
	}

	@Override
	protected boolean createSubTree(Book book) {
		return createBookTree(book);
	}
}
