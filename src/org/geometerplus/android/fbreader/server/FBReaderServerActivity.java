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
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.*;
import android.app.ProgressDialog;



public class FBReaderServerActivity extends Activity {

	private Button myStartButton;
	private Button myStopButton;
	private EditText myPortEdit;

	private ProgressDialog myProgress;
	private Handler myHandler = new Handler() {
			public void handleMessage(Message message) {
				myProgress.dismiss();
			}
		};

	final static String START = "service_start";
	final static String STOP = "service_stop";
	final static String STARTING = "service_starting";

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("org.geometerplus.android.fbreader.server.ServerService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(START)) {
				myStartButton.setEnabled(false);
				myStopButton.setEnabled(true);
				synchronized(FBReaderServerActivity.this) {
					FBReaderServerActivity.this.notify();
				}
			}
			if (intent.getAction().equals(STOP)) {
				myStartButton.setEnabled(true);
				myStopButton.setEnabled(false);
				synchronized(FBReaderServerActivity.this) {
					FBReaderServerActivity.this.notify();
				}
			}
			if (intent.getAction().equals(STARTING)) {
				myStartButton.setEnabled(false);
				myStopButton.setEnabled(false);
			}
		}
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
				try {
					myProgress = ProgressDialog.show(FBReaderServerActivity.this, "Please, wait...", "Starting", true, false);
					Intent startIntent = new Intent(FBReaderServerActivity.this, ServerService.class);
					startIntent.putExtra(ServerService.PORT, Integer.parseInt(myPortEdit.getText().toString()));
					startService(startIntent);
					final Thread runner = new Thread(new Runnable() {
						public void run() {
							synchronized(FBReaderServerActivity.this) {
								try {
									FBReaderServerActivity.this.wait();
								} catch (InterruptedException e) {
								}
							}
							myHandler.sendEmptyMessage(0);
						}
					});
					runner.start();
				} catch (NumberFormatException e) {
				}
			}
		});

		myStopButton = (Button)buttonView.findViewById(R.id.cancel_button);
		myStopButton.setText("Stop");
		myStopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				myProgress = ProgressDialog.show(FBReaderServerActivity.this, "Please, wait...", "Stopping", true, false);
				Intent startIntent = new Intent(FBReaderServerActivity.this, ServerService.class);
				stopService(startIntent);
				final Thread runner = new Thread(new Runnable() {
					public void run() {
						synchronized(FBReaderServerActivity.this) {
							try {
								FBReaderServerActivity.this.wait();
							} catch (InterruptedException e) {
							}
						}
						myHandler.sendEmptyMessage(0);
					}
				});
				runner.start();
			}
		});

		boolean running = isMyServiceRunning();
		myStartButton.setEnabled(!running);
		myStopButton.setEnabled(running);
		final TextView portLabel = (TextView)findViewById(R.id.port_label);
		portLabel.setText("Port:");
		myPortEdit = (EditText)findViewById(R.id.port);
		myPortEdit.setText("8080");

		myDataUpdateReceiver = new DataUpdateReceiver();
		IntentFilter filter = new IntentFilter(START);
		filter.addAction(STOP);
		filter.addAction(STARTING);
		registerReceiver(myDataUpdateReceiver, filter);
	}
}
