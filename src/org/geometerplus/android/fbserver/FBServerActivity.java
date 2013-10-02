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

public class FBServerActivity extends Activity {

	private Button myStartButton;
	private Button myStopButton;
	private EditText myPortEdit;
	private EditText myNameEdit;
	private EditText myIpEdit;


	private String myIp = "Not connected";
	private String myName;
	private String myPort;
	
	public static final String PREFS_NAME = "FBSPrefs";
	public static final String PORT = "port";
	public static final String NAME = "name";

	private ProgressDialog myProgress;
	private Handler myHandler = new Handler() {
		public void handleMessage(Message message) {
			if (message.what == 1) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				int port = settings.getInt(PORT, 8080);
				myName = settings.getString(NAME, getResources().getString(R.string.libraryName) + " " + android.os.Build.MODEL);
				myPort = Integer.toString(port);
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
		if (FBServerService.Instance != null) {
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
			String ip = intent.getStringExtra(FBServerService.IP);
			if (ip != null) {
				myIp = ip;
			} else {
				myIp = "Not connected";
			}
			changeState(intent.getAction());
		}
	}

	private void changeState(String state) {
		if (state.equals(STARTED)) {
			myStartButton.setEnabled(false);
			myStopButton.setEnabled(true);
			myPortEdit.setText(myPort);
			myPortEdit.setFocusable(false);
			myNameEdit.setText(myName);
			myNameEdit.setFocusable(false);
			myIpEdit.setText(myIp);
			synchronized(FBServerActivity.this) {
				FBServerActivity.this.notify();
			}
		}
		if (state.equals(STOPPED)) {
			myStartButton.setEnabled(true);
			myStopButton.setEnabled(false);
			myPortEdit.setText(myPort);
			myIpEdit.setText(myIp);
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
			myIpEdit.setText(myIp);
			myPortEdit.setText(myPort);
			myPortEdit.setFocusable(false);
			myNameEdit.setText(myName);
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
		myStartButton.setText(getResources().getString(R.string.start));
		myStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent startIntent = new Intent(FBServerActivity.this, FBServerService.class);
				String name = myNameEdit.getText().toString();
				int port = Integer.parseInt(myPortEdit.getText().toString());
				if (name.equals("")) {
					Toast.makeText(FBServerActivity.this, getResources().getString(R.string.enterName), Toast.LENGTH_SHORT).show();
					return;
				}
				if (port < 8000 || port > 65535) {
					Toast.makeText(FBServerActivity.this, getResources().getString(R.string.wrongPort), Toast.LENGTH_SHORT).show();
					return;
				}
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt(PORT, port);
				editor.putString(NAME, name);
				editor.commit();
				startIntent.putExtra(FBServerService.PORT, myPortEdit.getText().toString());
				startIntent.putExtra(FBServerService.NAME, name);
				startService(startIntent);
				waitForResponse(getResources().getString(R.string.wait), getResources().getString(R.string.starting));
			}
		});

		myStopButton = (Button)buttonView.findViewById(R.id.cancel_button);
		myStopButton.setText(getResources().getString(R.string.stop));
		myStopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent startIntent = new Intent(FBServerActivity.this, FBServerService.class);
				stopService(startIntent);
				waitForResponse(getResources().getString(R.string.wait), getResources().getString(R.string.stopping));
			}
		});

		myIpEdit = (EditText)findViewById(R.id.ip);
		myIpEdit.setFocusable(false);
		
		myNameEdit = (EditText)findViewById(R.id.name);

		
		final TextView nameLabel = (TextView)findViewById(R.id.name_label);
		nameLabel.setText(getResources().getString(R.string.name));
		final TextView portLabel = (TextView)findViewById(R.id.port_label);
		portLabel.setText(getResources().getString(R.string.port));
		final TextView ipLabel = (TextView)findViewById(R.id.ip_label);
		ipLabel.setText(getResources().getString(R.string.ip));
		myPortEdit = (EditText)findViewById(R.id.port);
		myDataUpdateReceiver = new DataUpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(STARTED);
		filter.addAction(STOPPED);
		filter.addAction(STARTING);
		filter.addAction(STOPPING);
		registerReceiver(myDataUpdateReceiver, filter);
		sendBroadcast(new Intent(FBServerService.ASK_STATE));
		waitForResponse(getResources().getString(R.string.wait), getResources().getString(R.string.looking));
	}

	@Override
	public void onDestroy() {
		if (myDataUpdateReceiver != null) {
			unregisterReceiver(myDataUpdateReceiver);
		}
		super.onDestroy();

	}
}
