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

import java.io.*;
import java.net.*;
import java.util.*;
import android.os.*;

import javax.jmdns.*;

import org.geometerplus.fbserver.book.IBookCollection;
import org.geometerplus.fbserver.library.*;

import android.net.wifi.WifiManager;
import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.content.res.AssetManager;

import nanohttpd.*;

import android.util.Log;

public class OPDSServer extends NanoHTTPD {

	public static final String ROOT_URL = "/root";
	public static final String ICON_URL = "/icon.png";

	private String myIp = "";

	private static final String ICON_FILE = "fbreader.png";

	private final int myPort;

	private WifiManager.MulticastLock myLock = null;
	private ArrayList<JmDNS> myJmDNSes = new ArrayList<JmDNS>();
	private Context myContext;
	
	private final IBookCollection myCollection;

	private final HashMap<String, String> myCache = new HashMap<String, String>();

	public void clearCache() {
		myCache.clear();
	}

	public OPDSServer(int port, Context context, IBookCollection col) throws IOException {
		super(port, Environment.getRootDirectory());
		myContext = context;
		myPort = port;
		myCollection = col;
		//		expose();
		OPDSCreator.init(context);
	}

	public String getIconAddress() {
		return ICON_URL;
	}

	public void expose(String name) throws IOException {
		final WifiManager wifiManager = (WifiManager)myContext.getSystemService(Context.WIFI_SERVICE);
		myLock = wifiManager.createMulticastLock("FBServer_lock");
		myLock.setReferenceCounted(true);
		myLock.acquire();
		final List<InetAddress> addresses = getLocalIpAddresses();
		if (addresses.isEmpty()) {
		} else {
			for (InetAddress address : addresses) {
				final JmDNS mcDNS = JmDNS.create(address, "FBServer");
				myJmDNSes.add(mcDNS);
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("path", ROOT_URL);
				ServiceInfo serviceInfo = ServiceInfo.create("_opds._tcp.local.", name, myPort, 0, 0, props);
				mcDNS.registerService(serviceInfo);
			}
		}
	}

	private void unexpose() {
		for (JmDNS mcDNS : myJmDNSes) {
			try {
				mcDNS.unregisterAllServices();
				mcDNS.close();
			} catch (IOException e) {
			}
		}
		if (myLock != null) {
			myLock.release();
		}
		myJmDNSes = null;
	}

	public String getIp() {
		if (getLocalIpAddresses().size() > 0) {
			return getLocalIpAddresses().get(0).getHostAddress();
		}
		return "Not connected";
	}

	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
		try {
			if (uri.equals(ICON_URL)) {
				try {
					return new Response(HTTP_OK, "image/png", myContext.getAssets().open(ICON_FILE));
				} catch (IOException e) {
				}
			}
			uri = uri.substring(1);

			if ("search".equals(uri)) {
				String request = (String) parms.get("searchTerm");
				LibraryTree tree = LibraryTreeProvider.getSearchResultsTree(myCollection, request);
				String res = OPDSCreator.createFeed(tree, getIconAddress());
				return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, res);
			}

			if (myCache.containsKey(uri)) {
				return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, myCache.get(uri));
			} else {
				LibraryTree tree = LibraryTreeProvider.getTreeById(uri);
				Log.e("SERVER", uri);
				if (tree != null) {
					if (tree instanceof BookTree) {
						String file = tree.getBook().File;
						file = file.substring("file://".length());
						int index = file.indexOf(":");
						if (index != -1) {
							file = file.substring(0, index);
						}
						return serveFile(file, header, new File("/"), true);
					}
					String res = OPDSCreator.createFeed(tree, getIconAddress());
					return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, res);
				}
			}

			return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "404");
		} catch (Throwable t) {
			return new NanoHTTPD.Response(HTTP_INTERNALERROR, MIME_PLAINTEXT, "500");
		}
	}

	public void stop() {
		//		unexpose();
		super.stop();
	}

	private List<InetAddress> getLocalIpAddresses() {
		final List<InetAddress> addresses = new LinkedList<InetAddress>();
		Method testPtoPMethod = null;
		try {
			testPtoPMethod = NetworkInterface.class.getMethod("isPointToPoint");
		} catch (NoSuchMethodException e) {
		}
		try {
			for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				try {
					if (testPtoPMethod != null && (Boolean)testPtoPMethod.invoke(iface)) {
						continue;
					}
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
				for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
					if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
						addresses.add(addr);
						if (myIp == "") {
							myIp = addr.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return addresses;
	}

}
