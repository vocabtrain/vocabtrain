package org.devwork.vocabtrain;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.devwork.vocabtrain.ProgressDialogFragment.ProgressDialogFragmentListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;


public class BluetoothSync {
	public static final String TAG = Constants.PACKAGE_NAME + ".BluetoothSync";
	private final FragmentActivity activity;
	
	class ClientThread extends Thread implements ProgressDialogFragmentListener{

	    private final BluetoothDevice bluetoothDevice;
	    private final ProgressDialogFragment progressDialog;
	    private boolean dialogDismissed = false;
	    private final String databaseFilename;

	    public ClientThread(BluetoothDevice bluetoothDevice) {
	        this.bluetoothDevice = bluetoothDevice;
	        this.progressDialog = ProgressDialogFragment.createInstance(activity.getString(R.string.bluetooth_sender_title), activity.getString(R.string.bluetooth_sender_desc), true);
	        DatabaseHelper dbh = new DatabaseHelper(activity);
	        databaseFilename = dbh.getFilename();
	        dbh.close();
	    }
	 
	    public void run() {
	    	if(dialogDismissed) return;
	    	if(!dialogDismissed && !progressDialog.isAdded() && !activity.isFinishing())
	    		progressDialog.show(activity.getSupportFragmentManager(), TAG);
	    	
	    	
	        // Cancel discovery because it will slow down the connection
	        bluetoothAdapter.cancelDiscovery();
	        BluetoothSocket bluetoothSocket = null;
	        try {
	        	
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	        	
	        	bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(Constants.BLUETOOTH_UID));
	        	bluetoothSocket.connect();
            	if(!activity.isFinishing())
            		activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
		                	progressDialog.setMessage(activity.getString(R.string.bluetooth_sender_sending));
						}
            		});
	        	
	        	if(dialogDismissed) return;
	            DatabaseFunctions.copyFile(new FileInputStream(databaseFilename), bluetoothSocket.getOutputStream());
	            
	        } 
	        catch (final IOException e) 
	        {
	        	catchError(e);
			} 
	        finally
	        {
	        	if(!activity.isFinishing())
	        		progressDialog.dismiss();
	            try {
	            	if(bluetoothSocket != null)
	            		bluetoothSocket.close();
	            } catch (IOException closeException) { }
	            
	        }
	    }
	    
	    
	    
	 
		@Override
		public void onDismiss() {
			dialogDismissed = true;
		}

		@Override
		public void onCreate(ProgressDialog dialog) {
		}
	    
	}
	private void catchError(final Exception e)
	{
		if(!activity.isFinishing())
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setMessage(activity.getString(R.string.bluetooth_error, e.getMessage())).setTitle(activity.getString(R.string.error_title));
					final AlertDialog alert = builder.create();
					alert.show();
				}
			});
	}
	
	class ServerThread extends Thread implements ProgressDialogFragmentListener {
	    private final BluetoothServerSocket serverSocket;
	    private final ProgressDialogFragment progressDialog;
	    private boolean dialogDismissed = false;
	    private final String databaseFilename;
	    public ServerThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	    	
	        this.progressDialog = ProgressDialogFragment.createInstance(activity.getString(R.string.bluetooth_receiver_title), activity.getString(R.string.bluetooth_receiver_desc), true);
	        DatabaseHelper dbh = new DatabaseHelper(activity);
	        databaseFilename = dbh.getFilename();
	        dbh.close();
	        		
	    	
	        BluetoothServerSocket tmpServerSocket = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
				tmpServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.BLUETOOTH_CAPTION, UUID.fromString(Constants.BLUETOOTH_UID));
	        } catch (IOException e) { }
	        serverSocket = tmpServerSocket;
	    }
	 
	    public void run() {
	    	if(dialogDismissed) return;
	    	if(!dialogDismissed && !progressDialog.isAdded() && !activity.isFinishing())
	    		progressDialog.show(activity.getSupportFragmentManager(), TAG);

	    	try
	    	{
	    	
		        BluetoothSocket bluetoothSocket = null;
		        // Keep listening until exception occurs or a socket is returned
		        while(bluetoothSocket == null) 
		        {
		                bluetoothSocket = serverSocket.accept();
		                if(dialogDismissed) return;
		                if(bluetoothSocket != null)
		                {
		                	if(!activity.isFinishing())
		                		activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
					                	progressDialog.setMessage(activity.getString(R.string.bluetooth_receiver_receiving));
									}
		                		});
		                	DatabaseFunctions.copyFile(bluetoothSocket.getInputStream(), new FileOutputStream(databaseFilename));
		    	            break;
		                }
		    	}
	    	} 
	    	catch (final IOException e) {
	    		e.printStackTrace();
	    		catchError(e);
			}
	    	finally {
            	try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
	    	if(!activity.isFinishing())
	    		progressDialog.dismiss();
	    }

		@Override
		public void onDismiss() {
			dialogDismissed = true;
		}

		@Override
		public void onCreate(ProgressDialog dialog) {
		}
	}

	
	
	
	BluetoothAdapter bluetoothAdapter;
	
	public BluetoothSync(final FragmentActivity activity)
	{
		this.activity = activity;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(R.string.no_bluetooth).setTitle(activity.getString(R.string.no_bluetooth_title));
			final AlertDialog alert = builder.create();
			alert.show();
			return;
		}
		if (!bluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    activity.startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BLUETOOTH);
		    return;
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(R.string.select_bluetooth_role).setTitle(activity.getString(R.string.select_bluetooth_role_title))
		.setNeutralButton(R.string.button_sender, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(final DialogInterface dialog, final int id)
			{
				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1);
				Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
				final BluetoothDevice[] devices = new BluetoothDevice[pairedDevices.size()];
				// If there are paired devices
				if (pairedDevices.size() > 0) {
				    // Loop through paired devices
					int i = 0;
				    for (BluetoothDevice device : pairedDevices) {
				    	devices[i++] = device;
				    	arrayAdapter.add(device.getName() + "\n" + device.getAddress());
				    }
				}
				final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(R.string.bluetooth_select_servers);
				builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog,	int which) {
						Log.e(TAG, "which: " + which);
						new ClientThread(devices[which]).start();

						
					}
	
					
				});
				final AlertDialog alert = builder.create();
				alert.show();
				
			}
		})
		.setPositiveButton(R.string.button_receiver, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id)
			{
				new ServerThread().start();
			} 
		})
		;
		final AlertDialog alert = builder.create();
		alert.show();
	}
}
