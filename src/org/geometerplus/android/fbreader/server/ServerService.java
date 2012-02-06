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

package org.geometerplus.android.fbreader.server;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.io.*;

import org.geometerplus.android.fbreader.server.opds.*;


public class ServerService extends Service {

	final static String PORT = "server_port";
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
				sendBroadcast(new Intent(FBReaderServerActivity.START));
			}
			if (res == FAILED) {
				Toast.makeText(getApplicationContext(), myError, Toast.LENGTH_SHORT).show();
				stopSelf();
			}
			if (res == STOPPED) {
				Toast.makeText(getApplicationContext(), "Server stopped", Toast.LENGTH_SHORT).show();
				sendBroadcast(new Intent(FBReaderServerActivity.STOP));
			}
		}
	};

	private OPDSServer myServer = null;

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		sendBroadcast(new Intent(FBReaderServerActivity.STARTING));
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		myPort = intent.getIntExtra(PORT, 8080);
		final Thread starter = new Thread(new Runnable() {
			public void run () {
				final int port = myPort;
				try {
					myServer = new OPDSServer(port, ServerService.this);
					Message myMessage=new Message();
					Bundle resBundle = new Bundle();
					resBundle.putInt(RESULT, STARTED);
					myMessage.setData(resBundle);
					myHandler.sendMessage(myMessage);
				} catch (IOException e) {
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
		mNM.cancel(NOTIFICATION);
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
			sendBroadcast(new Intent(FBReaderServerActivity.STOP));
		}
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	private void showNotification() {
		CharSequence text = "fgsgds";

		Notification notification = new Notification(android.R.drawable.star_on, text,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, FBReaderServerActivity.class), 0);

		notification.setLatestEventInfo(this, "fgsfds",
					   text, contentIntent);
		mNM.notify(NOTIFICATION, notification);
	}
}
