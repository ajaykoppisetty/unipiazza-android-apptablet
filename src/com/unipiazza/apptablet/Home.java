package com.unipiazza.apptablet;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Home extends Activity {
	JSONParser jParser = new JSONParser();
	// url per la ricerca dell'ID
	private EditText name, surname, email;
	private Button mSubmit;
	private NfcAdapter mNfcAdapter;
	private SharedPreferences sp;
	private String[][] techListsArray;
	private PendingIntent pendingIntent;
	public static final String MIME_TEXT_PLAIN = "text/plain";

	// Classe di controllo connessione
	public class ConnectionDetector {
		private Context _context;

		public ConnectionDetector(Context context) {
			this._context = context;
		}

		public boolean isConnectingToInternet() {
			ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null)
					for (int i = 0; i < info.length; i++)
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
			}
			return false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		sp = PreferenceManager.getDefaultSharedPreferences(Home.this);
		String username = sp.getString("username", "");
		String id_attivita = sp.getString("id_attivita", "");

		if (!username.isEmpty() && !id_attivita.isEmpty()) {
			Intent i = new Intent(Home.this, WaitingTap.class);
			Log.d("value", "Home attività " + username + " con id_attivita = " + id_attivita + " avvenuto con successo!!");
			finish();
			startActivity(i);
			Toast.makeText(Home.this, "Buongiorno " + username + "!", Toast.LENGTH_LONG).show();
			return;
		}

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		name = (EditText) findViewById(R.id.name);
		surname = (EditText) findViewById(R.id.surname);
		mSubmit = (Button) findViewById(R.id.signup);
	}

	/**
	 * Background Async Task per cercare l'ID facendo una chiamata HTTP
	 * */
	class LoadAllProducts extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args) {
			return null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(Home.this, WaitingTap.class);
		Log.v("action ", "Tasto indietro premuto");
		startActivity(i);
		finish();
	}

	public void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}


	public void onNewIntent(Intent intent) {
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		//do nothing
		Log.v("UNIPIAZZA", "onNewIntent");
	}

	private void prepareNFCIntercept() {
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		techListsArray = new String[][] {};
	}
}
