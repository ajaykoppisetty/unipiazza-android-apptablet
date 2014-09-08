package com.unipiazza.apptablet;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WaitingTap extends Activity {

	// url
	// JSON Node names
	private TextView mTextView;
	private NfcAdapter mNfcAdapter;
	// Progress Dialog
	private ProgressDialog pDialog;
	public static final String TAG = "NfcDemo";

	//Controllo Smartphone NFC
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waiting_tap);
		ImageView gyroView = (ImageView) findViewById(R.id.taploop);
		gyroView.setBackgroundResource(R.drawable.loop_animation);
		AnimationDrawable gyroAnimation = (AnimationDrawable) gyroView.getBackground();
		gyroAnimation.start();
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		if (mNfcAdapter == null) {
			// Stop here, we definitely need NFC
			Toast.makeText(this, "Questo dispositivo non supporta l'NFC", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (!mNfcAdapter.isEnabled()) {
			mTextView.setText("L'NFC � disabilitato, abilitalo e riavvia.");
		}
		handleIntent(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();

		setupForegroundDispatch(this, mNfcAdapter);
	}

	@Override
	protected void onPause() {
		/**
		 * Call this before onPause, otherwise an IllegalArgumentException is
		 * thrown as well.
		 */
		mNfcAdapter.disableForegroundDispatch(this);
		stopForegroundDispatch(this, mNfcAdapter);
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		/**
		 * This method gets called, when a new Intent gets associated with the
		 * current activity instance. Instead of creating a new activity,
		 * onNewIntent will be called. For more information have a look at the
		 * documentation.
		 * 
		 * In our case this method gets called, when the user attaches a Tag to
		 * the device.
		 */
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
				NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
			String type = intent.getType();
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Log.v("value ", "TAG Preso : " + tag);
			byte[] tag_id = tag.getId();
			String tag_id_string = bytesToHex(tag_id);
			Log.i("tag ID Appena preso", tag_id_string);
			SharedPreferences sp19 = PreferenceManager
					.getDefaultSharedPreferences(WaitingTap.this);
			Editor edit19 = sp19.edit();
			edit19.putString("tag_id_string", tag_id_string);
			edit19.commit();
			Log.i("tag ID", tag_id_string);
			new NdefReaderTask().execute(tag);
		}

	}

	final protected static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * @param activity
	 *            The corresponding {@link Activity} requesting the foreground
	 *            dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
		String[][] techList = new String[][] {};
		adapter.enableForegroundDispatch(activity, pendingIntent, null, techList);
	}

	/**
	 * @param activity
	 *            The corresponding {@link BaseActivity} requesting to stop the
	 *            foreground dispatch.
	 * @param adapter
	 *            The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
		@Override
		protected String doInBackground(Tag... params) {
			Tag tag = params[0];
			Ndef ndef = Ndef.get(tag);
			if (ndef == null) {
				// NDEF is not supported by this Tag.
				Log.v("value ", "NDEF is not supported by this Tag");
				return null;
			}
			NdefMessage ndefMessage = ndef.getCachedNdefMessage();
			NdefRecord[] records = ndefMessage.getRecords();
			for (NdefRecord ndefRecord : records) {
				if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
					try {
						return readText(ndefRecord);
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, "Unsupported Encoding", e);
					}
				}
			}
			return null;
		}

		private String readText(NdefRecord record) throws UnsupportedEncodingException {
			/*
			 * See NFC forum specification for "Text Record Type Definition" at
			 * 3.2.1
			 * 
			 * http://www.nfc-forum.org/specs/
			 * 
			 * bit_7 defines encoding bit_6 reserved for future use, must be 0
			 * bit_5..0 length of IANA language code
			 */
			byte[] payload = record.getPayload();
			// Get the Text Encoding
			String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
			// Get the Language Code
			int languageCodeLength = payload[0] & 0063;
			// String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			// e.g. "en"
			// Get the Text
			return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
		}

		//Evento dopo il TAP corretto
		@Override
		protected void onPostExecute(final String tag) {
			if (tag != null) {
				String[] res_array = new String[] { tag };
				Log.v("value ", "res_array : " + res_array);
				Log.v("value ", "tag_id_string : " + tag);
				SharedPreferences sp9 = PreferenceManager.getDefaultSharedPreferences(WaitingTap.this);
				Editor edit9 = sp9.edit();
				edit9.putString("hash_pass", tag);
				edit9.commit();
				SharedPreferences sp19 = PreferenceManager.getDefaultSharedPreferences(WaitingTap.this);
				String tag_id_string = sp19.getString("tag_id_string", "anon");
				Log.v("value ", "tag_id_string : " + tag_id_string);
				new SearchId().execute(tag_id_string);

			}
		}

		//JSON Ricerca ID
		class SearchId extends AsyncTask<String, String, String> {
			/**
			 * Before starting background thread Show Progress Dialog
			 * */
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pDialog = new ProgressDialog(WaitingTap.this);
				pDialog.setMessage("Sto cercando l'utente...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}

			/**
			 * Ricerca ID
			 * */
			protected String doInBackground(String... res_array) {
				SharedPreferences sp19 = PreferenceManager.getDefaultSharedPreferences(WaitingTap.this);
				String tag_id_string = sp19.getString("tag_id_string", "anon");
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				Log.v("value ", "Tag_Id_String da inviare : " + tag_id_string);
				params.add(new BasicNameValuePair("hash_pass", tag_id_string));
				return tag_id_string;
			}

			/**
			 * After completing background task Dismiss the progress dialog
			 * **/
			protected void onPostExecute(String file_url) {
				// dismiss the dialog once done
				pDialog.dismiss();
				if (file_url != null) {
					//Messaggio che compare sempre
					//Toast.makeText(WaitingTap.this, file_url, Toast.LENGTH_LONG).show();
				}
				Intent i = new Intent(WaitingTap.this, Home.class);
				startActivity(i);
			}

		}
	}

	@Override
	public void onBackPressed() {
		//Non uscire, cane!
	}
}
