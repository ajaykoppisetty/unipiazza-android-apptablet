package com.unipiazza.apptablet;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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
		name = (EditText) findViewById(R.id.name);
		surname = (EditText) findViewById(R.id.surname);
		email = (EditText) findViewById(R.id.email);
		mSubmit = (Button) findViewById(R.id.signup);
		mPortachiaviChekbox = (CheckBox) findViewById(R.id.portachiavi_box);

		validator = new Validator(this);
		validator.setValidationListener(this);

		mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				validator.validate();
			}
		});
	}

	private SpannableStringBuilder createError(String error) {
		int ecolor = Color.BLACK; // whatever color you want
		ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
		SpannableStringBuilder ssbuilder = new SpannableStringBuilder(error);
		ssbuilder.setSpan(fgcspan, 0, error.length(), 0);
		return ssbuilder;
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

	public void onValidationSucceeded() {
		Toast.makeText(this, "Yay! we got it right!", Toast.LENGTH_SHORT).show();
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

}
