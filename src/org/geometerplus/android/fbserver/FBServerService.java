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

import org.geometerplus.android.fbserver.opds.*;
import org.geometerplus.android.fbreader.api.ApiClientImplementation;
import org.geometerplus.android.fbreader.api.ApiException;
import org.geometerplus.android.fbreader.libraryService.*;
import org.geometerplus.fbserver.book.*;
import org.geometerplus.fbserver.book.IBookCollection.Status;
import org.geometerplus.fbserver.library.LibraryTreeProvider;
import org.geometerplus.fbserver.library.RootTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;


public class FBServerService extends Service implements BookCollectionShadow.Listener {

	private final IBookCollection myCollection = new BookCollectionShadow();

	static FBServerService Instance = null;

	final static String PORT = "server_port";
	final static String IP = "server_ip";

	final static String ASK_STATE = "ask_state";

	final static int STATE_STARTED = 0;
	final static int STATE_STARTING = 1;
	final static int STATE_FAILED = 2;
	final static int STATE_STOPPING = 3;
	final static int STATE_STOPPED = 4;

	private int myState;

	private int myPort;
	private String myError = "";
	
	private RootTree myRootTree;
	
	final Handler myHandler = new Handler() {
		public void handleMessage (Message msg) {
			Intent i = new Intent();
			i.putExtra(PORT, Integer.toString(myPort));
			if (myServer != null) {
				i.putExtra(IP, myServer.getIp());
			}
			switch (myState) {
			case STATE_STARTED:
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
				FBServerService.this.stopForeground(true);
				break;
			}
		}
	};

	private OPDSServer myServer = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Instance = this;
		myState = STATE_STARTING;
		myHandler.sendEmptyMessage(0);

		myMessageReceiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ASK_STATE);
		registerReceiver(myMessageReceiver, filter);
		((BookCollectionShadow)myCollection).addListener(this);
		((BookCollectionShadow)myCollection).bindToService(this, new Runnable() {
			@Override
			public void run() {
				setBuildStatus(myCollection.status());
			}
		});
		
		ZLResource.Api = new ApiClientImplementation(this, new  ApiClientImplementation.ConnectionListener() {
			@Override
			public void onConnected() {
				ZLResource.ConnectedToApi = true;
			}
		});
		ZLResource.Api.connect();
		myRootTree = (RootTree) LibraryTreeProvider.getRootTree(myCollection);
		
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		final Thread starter = new Thread(new Runnable() {
			public void run () {
				try {
					String portStr = intent.getStringExtra(PORT);
					myPort = Integer.parseInt(portStr);
					myServer = new OPDSServer(myPort, FBServerService.this, myCollection);
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
					Instance = null;
				}
			});
			finisher.start();
		} else {
			myState = STATE_STOPPED;
			myHandler.sendEmptyMessage(0);
			Instance = null;
		}
		if (myMessageReceiver != null) {
			unregisterReceiver(myMessageReceiver);
		}
		((BookCollectionShadow)myCollection).unbind();
		if (ZLResource.ConnectedToApi) {
			ZLResource.Api.disconnect();
			ZLResource.ConnectedToApi = false;
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
		this.startForeground(1, notification);
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

	private Status myBuildStatus = Status.NotStarted;
	
	
	private boolean myFullyInited() {
		return Status.Succeeded.equals(myBuildStatus) && ZLResource.ConnectedToApi;
	}

	void setBuildStatus(Status status) {
		myBuildStatus = status;
		tryToInit();
	}

	void tryToInit() {
		if (myFullyInited()) {
			init();
			myState = STATE_STARTED;
			myHandler.sendEmptyMessage(0);
		}
	}

	private void init() {
		String name;
		try {
			name = ZLResource.Api.getResourceValue("library");
		} catch (ApiException e) {
			e.printStackTrace();
			name = "FBReader Library";
		}
//		myServer.expose(name);
		
		myRootTree.init();
	}

	@Override
	public void onBookEvent(BookEvent event, Book book) {
		if (myFullyInited()) {
			myRootTree.onBookEvent(event, book);
			myServer.clearCache();
		}
	}

	@Override
	public void onBuildEvent(Status status) {
		setBuildStatus(status);
	}

}
