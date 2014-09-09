package com.unipiazza.apptablet;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.Validator.ValidationListener;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Required;

public class Home extends Activity implements ValidationListener {
	JSONParser jParser = new JSONParser();
	// url per la ricerca dell'ID

	@Required(order = 1, message = "Il nome è un campo obbligatorio!")
	private EditText name;
	@Required(order = 2, message = "Il cognome è un campo obbligatorio!")
	private EditText surname;
	@Required(order = 3, message = "L'email è un campo obbligatorio!")
	@Email(order = 4, message = "Inserisci un indirizzo mail valido!")
	private EditText email;
	private Button mSubmit;
	private NfcAdapter mNfcAdapter;
	private SharedPreferences sp;
	private String[][] techListsArray;
	private PendingIntent pendingIntent;
	private CheckBox mPortachiaviChekbox;

	private Validator validator;

	private String hash;

	private ProgressDialog pDialog;
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

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		prepareNFCIntercept();
		name = (EditText) findViewById(R.id.name);
		surname = (EditText) findViewById(R.id.surname);
		email = (EditText) findViewById(R.id.email);
		mSubmit = (Button) findViewById(R.id.signup);
		mPortachiaviChekbox = (CheckBox) findViewById(R.id.portachiavi_box);
		hash = getIntent().getExtras().getString("hash");

		validator = new Validator(this);
		validator.setValidationListener(this);

		mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				validator.validate();
			}
		});
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

	public void onValidationSucceeded() {
		pDialog = new ProgressDialog(Home.this);
		pDialog.setMessage("Login in corso...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();
		AttivitAppRESTClient.getInstance(Home.this).postRegistration(Home.this, hash, name.getText().toString()
				, surname.getText().toString(), email.getText().toString()
				, mPortachiaviChekbox.isChecked(), true, new HttpCallback() {

					@Override
					public void onSuccess(JsonObject result) {
						pDialog.dismiss();
						Toast.makeText(Home.this, "Utente registrato", Toast.LENGTH_SHORT).show();
						finish();
					}

					@Override
					public void onFail(JsonObject result, Throwable e) {
						pDialog.dismiss();
						if (result == null || result.get("msg") == null)
							Toast.makeText(Home.this, "Errore nella registrazione", Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(Home.this, result.get("msg").getAsString(), Toast.LENGTH_SHORT).show();

					}
				});
	}

	public void onValidationFailed(View failedView, Rule<?> failedRule) {
		String message = failedRule.getFailureMessage();

		if (failedView instanceof EditText) {
			failedView.requestFocus();
			((EditText) failedView).setError(message);
		} else {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	public void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	public void onResume() {
		super.onResume();
		mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);
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
