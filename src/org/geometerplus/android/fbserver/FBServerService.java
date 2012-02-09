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
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.io.*;

import org.geometerplus.android.fbserver.opds.*;


public class FBServerService extends Service {

	final static String PORT = "server_port";
	final static String NAME = "server_name";
	final static String RESULT = "server_result";

	final static int STARTED = 0;
	final static int FAILED = 1;
	final static int STOPPED = 2;

	private NotificationManager mNM;
	private int NOTIFICATION = 0;
	private int myPort;
	private String myError = "";

	final Handler myHandler = new Handler() {
		public void handleMessage (Message msg) {
			int res = msg.getData().getInt(RESULT);
			if (res == STARTED) {
				Toast.makeText(getApplicationContext(), "Server started on port: " + Integer.toString(myPort), Toast.LENGTH_SHORT).show();
				sendBroadcast(new Intent(FBServerActivity.START));
				showNotification();
			}
			if (res == FAILED) {
				Toast.makeText(getApplicationContext(), myError, Toast.LENGTH_SHORT).show();
				stopSelf();
			}
			if (res == STOPPED) {
				Toast.makeText(getApplicationContext(), "Server stopped", Toast.LENGTH_SHORT).show();
				sendBroadcast(new Intent(FBServerActivity.STOP));
				mNM.cancel(NOTIFICATION);
			}
		}
	};

	private OPDSServer myServer = null;

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		sendBroadcast(new Intent(FBServerActivity.STARTING));
	}

	private void createOPDS(String name) {
		OPDSCatalog root = new OPDSCatalog("/", name);
		OPDSCatalog test1 = new OPDSCatalog("/test1/", "Test1");
		OPDSCatalog test2 = new OPDSCatalog("/test2/", "Test2");
		OPDSCatalog test11 = new OPDSCatalog("/test11/", "Test11");
		OPDSCatalog test12 = new OPDSCatalog("/test12/", "Test12");
		root.addChild(test1);
		root.addChild(test2);
		test1.addChild(test11);
		test1.addChild(test12);
		OPDSItem.save(root);
		OPDSItem.save(test1);
		OPDSItem.save(test2);
		OPDSItem.save(test11);
		OPDSItem.save(test12);
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		final Thread starter = new Thread(new Runnable() {
			public void run () {
				try {
					String portStr = intent.getStringExtra(PORT);
					String name = intent.getStringExtra(NAME);
					createOPDS(name);
					myPort = Integer.parseInt(portStr);
					final int port = myPort;
					myServer = new OPDSServer(port, FBServerService.this);
					Message myMessage=new Message();
					Bundle resBundle = new Bundle();
					resBundle.putInt(RESULT, STARTED);
					myMessage.setData(resBundle);
					myHandler.sendMessage(myMessage);
				} catch (Exception e) {
					Message myMessage=new Message();
					Bundle resBundle = new Bundle();
					resBundle.putInt(RESULT, FAILED);
					myError = e.getMessage();
					myMessage.setData(resBundle);
					myHandler.sendMessage(myMessage);
				}
			}
		});
		starter.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (myServer != null) {
			final Thread finisher = new Thread(new Runnable() {
				public void run () {
					myServer.stop();
					Message myMessage=new Message();
					Bundle resBundle = new Bundle();
					resBundle.putInt(RESULT, STOPPED);
					myMessage.setData(resBundle);
					myHandler.sendMessage(myMessage);
				}
			});
			finisher.start();
		} else {
			Toast.makeText(getApplicationContext(), "Server stopped", Toast.LENGTH_SHORT).show();
			sendBroadcast(new Intent(FBServerActivity.STOP));
		}
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
}
