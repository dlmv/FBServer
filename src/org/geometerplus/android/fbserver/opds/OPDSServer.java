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
import android.net.wifi.WifiManager;
import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import nanohttpd.*;

public class OPDSServer extends NanoHTTPD {

	private final int myPort;
	private WifiManager.MulticastLock myLock = null;
	private ArrayList<JmDNS> myJmDNSes = new ArrayList<JmDNS>();
	private Context myContext;

	public OPDSServer(int port, Context context) throws IOException {
		super(port, Environment.getExternalStorageDirectory());
		myContext = context;
		myPort = port;
		expose();
		OPDSCreator.init(context);
	}

	public String getHostAddress() {
		return myServerSocket.getInetAddress().getHostAddress();
	}

	private void expose() throws IOException {
		ServiceInfo serviceInfo = ServiceInfo.create("_stanza._tcp.local.", "FBReader server", myPort, 0, 0, "path=/stanza");
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

	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
		OPDSItem item = OPDSItem.get(uri);
		if (item != null) {
			if (item instanceof OPDSCatalog) {
				String msg = OPDSCreator.getInstance().createFeed((OPDSCatalog)item);
				return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
			}
		}
		return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_HTML, "404");
	}

	public void stop() {
		unexpose();
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
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return addresses;
	}

}
