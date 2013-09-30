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

import java.math.BigDecimal;

import org.geometerplus.fbserver.book.*;
import org.geometerplus.fbserver.tree.FBTree;

import android.util.Log;

public class BookTree extends LibraryTree {
	public final Book Book;

	BookTree(IBookCollection collection, Book book) {
		super(collection);
		Book = book;
	}

	@Override
	public String getName() {
		return Book.getTitle();
	}

	@Override
	public Book getBook() {
		return Book;
	}

	@Override
	public String getUniqueId() {
		return "book_" + Book.getId();
	}


	@Override
	public boolean containsBook(Book book) {
		return book != null && book.equals(Book);
	}

	@Override
	protected String getSortKey() {
		return Book.getSortKey();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof BookTree)) {
			return false;
		}
		return Book.equals(((BookTree)object).Book);
	}

	@Override
	public String getSummary() {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (Author author : Book.authors()) {
			if (count++ > 0) {
				builder.append(",  ");
			}
			builder.append(author.DisplayName);
			if (count == 5) {
				break;
			}
		}
		return builder.toString();
	}

	@Override
	public int compareTo(FBTree tree) {
		if (tree instanceof BookTree) {
			final BigDecimal index0 = Book.getSeriesInfo() == null ? null : Book.getSeriesInfo().Index;
			final BigDecimal index1 =((BookTree)tree).Book.getSeriesInfo() == null ? null : ((BookTree)tree).Book.getSeriesInfo().Index;
			final int cmp;
			if (index0 == null) {
				cmp = index1 == null ? 0 : 1;
			} else {
				cmp = index1 == null ? -1 : index0.compareTo(index1);
			}
			if (cmp != 0) {
				return cmp;
			} 
		}
		final int cmp = super.compareTo(tree);
		if (cmp == 0 && tree instanceof BookTree) {
			final Book b = ((BookTree)tree).Book;
			if (Book != null && b != null) {
				return Book.File.compareTo(b.File);
			}
		}
		return cmp;
	}
}
