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
import android.view.View;
import android.widget.*;

import android.util.Log;

public class FBServerActivity extends Activity {

	private Button myStartButton;
	private Button myStopButton;
	private EditText myPortEdit;
	private EditText myNameEdit;

	private String myPort;
	private String myName;

	private ProgressDialog myProgress;
	private Handler myHandler = new Handler() {
			public void handleMessage(Message message) {
				if (message.what == 1) {
					myPort = "8080";
					myName = "FBReader Library";
					changeState(STOPPED);
				}
				myProgress.dismiss();
			}
		};

	final static String STARTED = "service_start";
	final static String STOPPED = "service_stop";
	final static String STARTING = "service_starting";
	final static String STOPPING = "service_stopping";

	private boolean isMyServiceRunning() {
		if (FBServerService.exists) {
			return true;
		}
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("org.geometerplus.android.fbserver.FBServerService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			myPort = intent.getStringExtra(FBServerService.PORT);
			myName = intent.getStringExtra(FBServerService.NAME);
			changeState(intent.getAction());
		}
	}

	private void changeState(String state) {
		if (state.equals(STARTED)) {
			myStartButton.setEnabled(false);
			myStopButton.setEnabled(true);
			myPortEdit.setText(myPort);
			myNameEdit.setText(myName);
			myPortEdit.setFocusable(false);
			myNameEdit.setFocusable(false);
			synchronized(FBServerActivity.this) {
				FBServerActivity.this.notify();
			}
		}
		if (state.equals(STOPPED)) {
			myStartButton.setEnabled(true);
			myStopButton.setEnabled(false);
			myPortEdit.setText(myPort);
			myNameEdit.setText(myName);
			myPortEdit.setFocusableInTouchMode(true);
			myNameEdit.setFocusableInTouchMode(true);
			synchronized(FBServerActivity.this) {
				FBServerActivity.this.notify();
			}
		}
		if (state.equals(STARTING)) {
			myStartButton.setEnabled(false);
			myStopButton.setEnabled(false);
			myPortEdit.setFocusable(false);
			myNameEdit.setFocusable(false);
		}
		if (state.equals(STOPPING)) {
			myStartButton.setEnabled(false);
			myStopButton.setEnabled(false);
			myPortEdit.setText(myPort);
			myNameEdit.setText(myName);
			myPortEdit.setFocusable(false);
			myNameEdit.setFocusable(false);
		}
	}

	private void waitForResponse(String title, String message) {
		myProgress = ProgressDialog.show(this, title, message, true, false);
		final Thread checker = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						if (!isMyServiceRunning()) {
							myHandler.sendEmptyMessage(1);
							break;
						}
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
				}
			}
		});
		final Thread waiter = new Thread(new Runnable() {
			public void run() {
				synchronized(FBServerActivity.this) {
					try {
						FBServerActivity.this.wait();
					} catch (InterruptedException e) {
					}
				}
				checker.interrupt();
				myHandler.sendEmptyMessage(0);
			}
		});
		checker.start();
		waiter.start();
	}

	private DataUpdateReceiver myDataUpdateReceiver;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.mainwindow);
		final View buttonView = findViewById(R.id.start_stop_buttons);

		myStartButton = (Button)buttonView.findViewById(R.id.ok_button);
		myStartButton.setText("Start");
		myStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent startIntent = new Intent(FBServerActivity.this, FBServerService.class);
				startIntent.putExtra(FBServerService.PORT, myPortEdit.getText().toString());
				startIntent.putExtra(FBServerService.NAME, myNameEdit.getText().toString());
				startService(startIntent);
				waitForResponse("Please, wait...", "Starting");
			}
		});

		myStopButton = (Button)buttonView.findViewById(R.id.cancel_button);
		myStopButton.setText("Stop");
		myStopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent startIntent = new Intent(FBServerActivity.this, FBServerService.class);
				stopService(startIntent);
				waitForResponse("Please, wait...", "Stopping");
			}
		});

		final TextView portLabel = (TextView)findViewById(R.id.port_label);
		portLabel.setText("Port:");
		myPortEdit = (EditText)findViewById(R.id.port);

		final TextView nameLabel = (TextView)findViewById(R.id.name_label);
		nameLabel.setText("Name:");
		myNameEdit = (EditText)findViewById(R.id.name);

		myDataUpdateReceiver = new DataUpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(STARTED);
		filter.addAction(STOPPED);
		filter.addAction(STARTING);
		filter.addAction(STOPPING);
		registerReceiver(myDataUpdateReceiver, filter);
		sendBroadcast(new Intent(FBServerService.ASK_STATE));
		waitForResponse("Please, wait...", "Looking for running service");
	}

	@Override
	public void onDestroy() {
		if (myDataUpdateReceiver != null) {
			unregisterReceiver(myDataUpdateReceiver);
		}
		super.onDestroy();

	}
}
