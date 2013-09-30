package org.geometerplus.fbserver.library;

import java.util.HashMap;

import org.geometerplus.fbserver.book.*;

import android.util.Log;

public abstract class LibraryTreeProvider {
	
	public static String getMethodName(final int depth){
		return new Exception().getStackTrace()[1].toString();
	}

	private static final HashMap<String, LibraryTree> ourMap = new HashMap<String, LibraryTree>();

	private static LibraryTree getTreeInternal(LibraryTree t) {
		if (!ourMap.containsKey(t.getUniqueId())) {
			ourMap.put(t.getUniqueId(), t);
		}
		return ourMap.get(t.getUniqueId());
	}

	public static LibraryTree getTreeById(String id) {
		if (ourMap.containsKey(id)) {
			return ourMap.get(id);
		}
		return null;

	}

	public static LibraryTree getRootTree(IBookCollection collection) {
		RootTree t = new RootTree(collection);
		return getTreeInternal(t);
	}

	public static LibraryTree getAuthorListTree(IBookCollection collection) {
		AuthorListTree t = new AuthorListTree(collection);
		return getTreeInternal(t);
	}

	public static LibraryTree getTitleListTree(IBookCollection collection) {
		TitleListTree t = new TitleListTree(collection);
		return getTreeInternal(t);
	}

	public static LibraryTree getTagListTree(IBookCollection collection) {
		TagListTree t = new TagListTree(collection);
		return getTreeInternal(t);
	}

	public static LibraryTree getSeriesListTree(IBookCollection collection) {
		SeriesListTree t = new SeriesListTree(collection);
		return getTreeInternal(t);
	}

	public static LibraryTree getAuthorTree(IBookCollection collection, Author a) {
		AuthorTree t = new AuthorTree(collection, a);
		return getTreeInternal(t);
	}

	public static LibraryTree getBookTree(IBookCollection collection, Book b) {
		BookTree t = new BookTree(collection, b);
		return getTreeInternal(t);
	}

	public static LibraryTree getSeriesTree(IBookCollection collection, Series s, Author a) {
		SeriesTree t = new SeriesTree(collection, s, a);
		return getTreeInternal(t);
	}

	public static LibraryTree getTagTree(IBookCollection collection, Tag tg) {
		TagTree t = new TagTree(collection, tg);
		return getTreeInternal(t);
	}

	public static LibraryTree getTitleTree(IBookCollection collection, String s) {
		TitleTree t = new TitleTree(collection, s);
		return getTreeInternal(t);
	}



}
