package com.unipiazza.apptablet;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;

public class CurrentAdmin extends User {

	private static CurrentAdmin instance;

	public static CurrentAdmin getInstance() {
		if (instance == null) {
			instance = new CurrentAdmin();
			return instance;
		} else
			return instance;
	}

	public CurrentAdmin(int id, String first_name, String last_name, String image_url) {
		super(id, first_name, last_name, image_url);
	}

	public CurrentAdmin() {
		super();
	}

	public void isAuthenticated(Context context, HttpCallback callback) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String access_token = pref.getString("access_token", "");
		String email = pref.getString("email", "");
		String refresh_token = pref.getString("refresh_token", "");

		if (!access_token.isEmpty() && !email.isEmpty() && !refresh_token.isEmpty()) {
			AttivitAppRESTClient.getInstance(context).refreshToken(context, refresh_token, callback);
		} else
			callback.onFail(null, null);
	}

	public void checkToken(Context context, final HttpCallback callback) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		int expires_in = pref.getInt("expires_in", 0);
		long token_date = pref.getLong("token_date", 0);
		Log.v("UNIPIAZZA", "System.currentTimeMillis() - token_date=" + (System.currentTimeMillis() - token_date));
		Log.v("UNIPIAZZA", "expires_in=" + expires_in);

		if (System.currentTimeMillis() - token_date >= expires_in) {
			String refresh_token = pref.getString("refresh_token", "");
			AttivitAppRESTClient.getInstance(context).refreshToken(context, refresh_token, new HttpCallback() {

				@Override
				public void onSuccess(JsonObject result) {
					callback.onSuccess(result);
				}

				@Override
				public void onFail(JsonObject result, Throwable e) {
					callback.onFail(result, e);
				}
			});
		} else
			callback.onSuccess(null);
	}

	public void setAuthenticated(Context context, String email, String first_name, String access_token
			, String refresh_token, int expires_in, int id, String password) {
		this.id = id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.image_url = image_url;

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();

		edit.putString("access_token", access_token);
		edit.putString("email", email);
		edit.putString("password", password);
		edit.putString("refresh_token", refresh_token);
		edit.putInt("expires_in", expires_in);
		edit.putInt("id_attivita", id);
		edit.commit();
	}

	public void setToken(Context context, String access_token, String refresh_token, int expires_in) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();
		edit.putString("access_token", access_token);
		edit.putString("refresh_token", refresh_token);
		edit.putInt("expires_in", expires_in);
		edit.putLong("token_date", System.currentTimeMillis());
		edit.commit();
	}

	public String getAccessToken(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString("access_token", "");
	}

	public void logout(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = pref.edit();

		edit.clear();
		edit.commit();

		File dir = context.getExternalCacheDir();

		File file = new File(dir.getAbsolutePath() + "/me.jpg");
		if (file.exists())
			file.delete();
	}

}
