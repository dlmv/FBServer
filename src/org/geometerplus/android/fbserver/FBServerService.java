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

package org.geometerplus.android.fbserver;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.io.*;

import org.geometerplus.android.fbserver.opds.*;
import org.geometerplus.android.fbreader.libraryService.*;


public class FBServerService extends Service implements ServiceConnection {

	static boolean exists = false;

	final static String PORT = "server_port";
	final static String NAME = "server_name";

	final static String ASK_STATE = "ask_state";

	final static int STATE_STARTED = 0;
	final static int STATE_STARTING = 1;
	final static int STATE_FAILED = 2;
	final static int STATE_STOPPING = 3;
	final static int STATE_STOPPED = 4;

	private int myState;

	private NotificationManager mNM;
	private int NOTIFICATION = 0;
	private int myPort;
	private String myName = "";
	private String myError = "";

	final Handler myHandler = new Handler() {
		public void handleMessage (Message msg) {
			Intent i = new Intent();
			i.putExtra(PORT, Integer.toString(myPort));
			i.putExtra(NAME, myName);
			switch (myState) {
				case STATE_STARTED:
					createOPDS();
					Toast.makeText(getApplicationContext(), "Server is running on port: " + Integer.toString(myPort), Toast.LENGTH_SHORT).show();
					i.setAction(FBServerActivity.STARTED);
					sendBroadcast(i);
					showNotification();
					break;
				case STATE_STARTING:
					i.setAction(FBServerActivity.STARTING);
					sendBroadcast(i);
					break;
				case STATE_FAILED:
					Toast.makeText(getApplicationContext(), myError, Toast.LENGTH_SHORT).show();
					i.setAction(FBServerActivity.STOPPING);
					sendBroadcast(i);
					stopSelf();
					break;
				case STATE_STOPPING:
					i.setAction(FBServerActivity.STOPPING);
					sendBroadcast(i);
					break;
				case STATE_STOPPED:
					Toast.makeText(getApplicationContext(), "Server stopped", Toast.LENGTH_SHORT).show();
					i.setAction(FBServerActivity.STOPPED);
					sendBroadcast(i);
					mNM.cancel(NOTIFICATION);
					break;
			}
		}
	};

	private OPDSServer myServer = null;

	@Override
	public void onCreate() {
		exists = true;
		super.onCreate();
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		myState = STATE_STARTING;
		myHandler.sendEmptyMessage(0);

		myMessageReceiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ASK_STATE);
		registerReceiver(myMessageReceiver, filter);
	}

	private boolean isLibraryServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("org.geometerplus.android.fbreader.libraryService.LibraryService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void createOPDS() {
		if (!isLibraryServiceRunning()) {
			Toast.makeText(getApplicationContext(), "LibraryService is not running", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent();
		i.setClassName("org.geometerplus.zlibrary.ui.android", "org.geometerplus.android.fbreader.libraryService.LibraryService");
		getApplicationContext().bindService(i, this, 0);

	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		final Thread starter = new Thread(new Runnable() {
			public void run () {
				try {
					String portStr = intent.getStringExtra(PORT);
					myName = intent.getStringExtra(NAME);
					myPort = Integer.parseInt(portStr);
					final int port = myPort;
					myServer = new OPDSServer(port, myName, FBServerService.this);
					myState = STATE_STARTED;
					myHandler.sendEmptyMessage(0);
				} catch (Exception e) {
					myError = e.getMessage();
					myState = STATE_FAILED;
					myHandler.sendEmptyMessage(0);
				}
			}
		});
		starter.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		myState = STATE_STOPPING;
		myHandler.sendEmptyMessage(0);
		if (myServer != null) {
			final Thread finisher = new Thread(new Runnable() {
				public void run () {
					myServer.stop();
					myState = STATE_STOPPED;
					myHandler.sendEmptyMessage(0);
					exists = false;
				}
			});
			finisher.start();
		} else {
			myState = STATE_STOPPED;
			myHandler.sendEmptyMessage(0);
			exists = false;
		}
		if (myMessageReceiver != null) {
			unregisterReceiver(myMessageReceiver);
		}
		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	private void showNotification() {
		CharSequence text = "FBServer is running";

		Notification notification = new Notification(android.R.drawable.star_on, text,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, FBServerActivity.class), 0);

		notification.setLatestEventInfo(this, "FBServer is running",
					   text, contentIntent);
		mNM.notify(NOTIFICATION, notification);
	}

	private class MessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Thread.sleep(200);//wait for activity to start waiting // maybe no need?
			} catch (InterruptedException e) {
			}
			myHandler.sendEmptyMessage(0);
		}
	}

	private MessageReceiver myMessageReceiver;

//----------from ServiceConnection:

	private LibraryInterface Iface;

	public void onServiceConnected(ComponentName name, IBinder binder) {
		Iface = LibraryInterface.Stub.asInterface(binder);

		OPDSCatalog root = new OPDSCatalog(OPDSServer.ROOT_URL, myName);
		OPDSItem.save(root);
		BookObject book = null;
		try {
			book = Iface.getRecentBook();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		String path = book.Path;
		int index = path.lastIndexOf(':');
		if (index != -1) {
			path = path.substring(0, index);
		}
		if (path.startsWith("/sdcard")) {
			path = path.substring("/sdcard".length());
		}
		OPDSBook recent = new OPDSBook(path, "Recent book");
		recent.setFilePath(path);
		recent.setType("application/fb2+zip");
		root.addChild(recent);
		OPDSItem.save(recent);
	}

	public void onServiceDisconnected(ComponentName name) {
		Iface = null;
	}

}
