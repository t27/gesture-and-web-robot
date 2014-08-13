
package com.blueserial;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.t27.blueserial.R;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * @author 2011A8PS255P
 * Purpose of the class is to handle all the activities once we are connected with the vehicle
 */
public class MainActivity extends Activity implements SensorEventListener{

	private static final String TAG = "BlueTest5-MainActivity";
	private int mMaxChars = 50000;//Default
	private UUID mDeviceUUID;//device UUID is a address needed for bluetooth connection
	private BluetoothSocket mBTSocket;// used to get inputStream and outputStream so that we can send and receive message
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;

	// All controls here
	private TextView mTxtReceive;
	private EditText mEditSend;
	private Button mBtnDisconnect;// button to break the connection
	private Button mBtnSend;// buton to send the command
	private Button mBtnClear;// button to clear the command
	private Button mBtnClearInput;
	private ScrollView scrollView;
	private CheckBox chkScroll;
	private CheckBox chkReceiveText;
	private CheckBox chkAccel;
	String btcommand;// store command
	private boolean mIsBluetoothConnected = false;

	private BluetoothDevice mDevice;
	
	private SensorManager mSensorManager; 
	private Sensor mAccelerometer;// will use this object to deal with accelerometer

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActivityHelper.initialize(this);

		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		mDevice = b.getParcelable(Homescreen.DEVICE_EXTRA);
		mDeviceUUID = UUID.fromString(b.getString(Homescreen.DEVICE_UUID));
		mMaxChars = b.getInt(Homescreen.BUFFER_SIZE);

		Log.d(TAG, "Ready");
		/**
		 * connecting buttons with the layout
		 */
		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnClear = (Button) findViewById(R.id.btnClear);
		mTxtReceive = (TextView) findViewById(R.id.txtReceive);
		mEditSend = (EditText) findViewById(R.id.editSend);
		scrollView = (ScrollView) findViewById(R.id.viewScroll);
		chkScroll = (CheckBox) findViewById(R.id.chkScroll);
		chkAccel = (CheckBox) findViewById(R.id.chkAccel);
		chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
		mBtnClearInput = (Button) findViewById(R.id.btnClearInput);

		mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, 500000);//SensorManager.SENSOR_DELAY_NORMAL=200000
		btcommand=new String("x");
		
		/**
		 * following are on click action listener for various buttons
		 */
		mBtnDisconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsUserInitiatedDisconnect = true;
				new DisConnectBT().execute();
			}
		});

		mBtnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					mBTSocket.getOutputStream().write(mEditSend.getText().toString().getBytes());
					mEditSend.setText("");
				} catch (IOException e) {
					System.out.println("Unable to send command"+""+e.getMessage());
				}
			}
		});

		mBtnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mEditSend.setText("");
			}
		});
		
		mBtnClearInput.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mTxtReceive.setText("");
			}
		});

	}

	/**
	 * Thread completely dedicated to reading input from Arduino connected to vehicle
	 */
	private class ReadInput implements Runnable {

		private boolean bStop = false;
		private Thread t;

		public ReadInput() {
			t = new Thread(this, "Input Thread");
			t.start();
		}

		public boolean isRunning() {
			return t.isAlive();
		}

		@Override
		public void run() {
			InputStream inputStream;

			try {
				inputStream = mBTSocket.getInputStream();//get inputStream needed to read data
				while (!bStop) {
					byte[] buffer = new byte[256];
					if (inputStream.available() > 0) {
						inputStream.read(buffer);
						int i = 0;
						/*
						 * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
						 */
						for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
						}
						final String strInput = new String(buffer, 0, i);

						/*
						 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
						 */

						if (chkReceiveText.isChecked()) {
							mTxtReceive.post(new Runnable() {
								@Override
								public void run() {
									mTxtReceive.append(strInput);
									//Uncomment below for testing
									//mTxtReceive.append("\n");
									//mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() + "\n");
									
									int txtLength = mTxtReceive.getEditableText().length();  
									if(txtLength > mMaxChars){
										mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
									}

									if (chkScroll.isChecked()) { // Scroll only if this is checked
										scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
													@Override
													public void run() {
														scrollView.fullScroll(View.FOCUS_DOWN);
													}
												});
									}
								}
							});
						}

					}
					Thread.sleep(500);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		public void stop() {
			bStop = true;
		}

	}

	private class DisConnectBT extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (mReadThread != null) {
				mReadThread.stop();
				while (mReadThread.isRunning())
					; // Wait until it stops
				mReadThread = null;

			}

			try {
				mBTSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mIsBluetoothConnected = false;
			if (mIsUserInitiatedDisconnect) {
				finish();
			}
		}

	}

	private void msg(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		if (mBTSocket != null && mIsBluetoothConnected) {
			new DisConnectBT().execute();
		}
		Log.d(TAG, "Paused");
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		if (mBTSocket == null || !mIsBluetoothConnected) {
			new ConnectBT().execute();
		}
		Log.d(TAG, "Resumed");
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, 500000);//SensorManager.SENSOR_DELAY_NORMAL=200000
		
		
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopped");
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private class ConnectBT extends AsyncTask<Void, Void, Void> {
		private boolean mConnectSuccessful = true;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
		}

		@Override
		protected Void doInBackground(Void... devices) {

			try {
				if (mBTSocket == null || !mIsBluetoothConnected) {
					mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					mBTSocket.connect();
				}
			} catch (IOException e) {
				// Unable to connect to device
				e.printStackTrace();
				mConnectSuccessful = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (!mConnectSuccessful) {
				Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
				finish();
			} else {
				msg("Connected to device");
				mIsBluetoothConnected = true;
				mReadThread = new ReadInput(); // Kick off input reader
			}

			progressDialog.dismiss();
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}

	/**
	 * logic to decide the direction of movement of vehicle
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		mEditSend.setText("vv");
		if (chkAccel.isChecked()) {
			
			mEditSend.setText("bb");
			// value of acceleration in x,y,z, direction
			double xVal = event.values[0];
			double yVal = event.values[1];
			double zVal = event.values[2];
			
			/*REMOVING TILT DETECTION TO SEND RAW ACCEL VALUes*/
			double XYangle = Math.atan2(zVal,
					Math.sqrt(Math.pow(xVal, 2) + Math.pow(yVal, 2)));//front and back tilt
			XYangle = Math.toDegrees(XYangle);
			double YZangle = Math.atan2(xVal,
					Math.sqrt(Math.pow(zVal, 2) + Math.pow(yVal, 2)));//left right tilt
			YZangle = Math.toDegrees(YZangle);
			int stoplimit, rightleftlimit, offset;
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String uuid = prefs.getString("prefUuid", "Null");
			
//			offset =prefs.getInt("prefDefaultOffset", 25);
//			stoplimit = prefs.getInt("prefFBlimit", 10);//SL
//			rightleftlimit = prefs.getInt("prefRLlimit", 15);//RLL
			
			offset =25;//prefs.getInt("prefDefaultOffset", 25);
			stoplimit =10;// prefs.getInt("prefFBlimit", 10);//SL
			rightleftlimit =15;// prefs.getInt("prefRLlimit", 15);//RLL
			
			String dir = "q";
			if (XYangle < (-stoplimit) + offset) {
				dir = "s";//back
			} else if (XYangle <= stoplimit + offset) {
				dir = "x";//stop
			} else if (XYangle > stoplimit + offset) {
				dir = "w";//front
			}
			if (YZangle < -rightleftlimit) {
				dir = "d";//right
			} else if (YZangle <= rightleftlimit) {
				//none
			} else if (YZangle > rightleftlimit) {
				dir = "a";//left
			}
			mEditSend.setText(dir);
			
			if (dir!=btcommand) 
			{
				btcommand=dir;
				try {
					mBTSocket.getOutputStream().write(btcommand.getBytes());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
//			int intx,inty,intz;
//			intx=(int)xVal*10;
//			inty=(int)yVal*10;
//			intz=(int)zVal*10;
//			
//			String gen="s,"+intx+","+inty+","+intz+",e";
//			try {
//				mBTSocket.getOutputStream().write(gen.getBytes());
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			
		}
		
	}

}
