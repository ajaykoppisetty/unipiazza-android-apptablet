package com.unipiazza.apptablet;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class AttivitAppRESTClient {
	private static AttivitAppRESTClient instance;
	private static Object lock = new Object();

	public static AttivitAppRESTClient getInstance(Context context) {
		if (instance == null) {
			return new AttivitAppRESTClient();
		} else {
			synchronized (instance) {
				return instance;
			}
		}
	}

	public void postAuthenticate(final Context context, final String email, final String password, final HttpCallback callback) {
		Log.v("UNIPIAZZA", "postAuthenticate");
		JsonObject json = new JsonObject();
		json.addProperty("grant_type", "password");
		json.addProperty("email", email);
		json.addProperty("password", password);
		json.addProperty("scope", "admin");

		Ion.with(context)
				.load(UnipiazzaParams.LOGIN_URL)
				.setJsonObjectBody(json)
				.asJsonObject()
				.setCallback(new FutureCallback<JsonObject>() {
					@Override
					public void onCompleted(Exception e, JsonObject result) {
						Log.v("UNIPIAZZA", "postAuthenticate result=" + result);
						Log.v("UNIPIAZZA", "postAuthenticate e=" + e);
						if (e == null) {
							try {
								String access_token = result.get("access_token").getAsString();
								String refresh_token = result.get("refresh_token").getAsString();
								int expires_in = result.get("expires_in").getAsInt();
								CurrentAdmin.getInstance().setAuthenticated(context
										, access_token, refresh_token, expires_in
										, password);
								callback.onSuccess(result);
							} catch (Exception ex) {
								ex.printStackTrace();
								callback.onFail(result, ex);
							}
						} else
							callback.onFail(result, e);
					}

				});

	}

	public void refreshToken(final Context context, final String refresh_token, final HttpCallback callback) {
		Log.v("UNIPIAZZA", "refreshToken");

		JsonObject json = new JsonObject();
		json.addProperty("grant_type", "refresh_token");
		json.addProperty("refresh_token", refresh_token);

		Ion.with(context)
				.load(UnipiazzaParams.LOGIN_URL)
				.setJsonObjectBody(json)
				.asJsonObject()
				.setCallback(new FutureCallback<JsonObject>() {
					@Override
					public void onCompleted(Exception e, JsonObject result) {
						Log.v("UNIPIAZZA", "result=" + result);
						if (e == null) {
							try {
								String access_token = result.get("access_token").getAsString();
								String refresh_token = result.get("refresh_token").getAsString();
								int expires_in = result.get("expires_in").getAsInt();
								CurrentAdmin.getInstance().setToken(context, access_token, refresh_token, expires_in);
								if (callback != null)
									callback.onSuccess(result);
							} catch (Exception ex) {
								ex.printStackTrace();
								if (callback != null)
									callback.onFail(result, ex);
							}
						} else {
							if (callback != null)
								callback.onFail(result, e);
						}
					}

				});

	}

	public void postRegistration(final Context context, final String hash, final String name, final String surname
			, final String email, final boolean checked, final boolean checkToken, final HttpCallback callback) {
		if (checkToken) {
			CurrentAdmin.getInstance().checkToken(context, new HttpCallback() {

				@Override
				public void onSuccess(JsonObject result) {
					postRegistrationHttp(context, hash, name, surname, email, checked, checkToken, callback);
				}

				@Override
				public void onFail(JsonObject result, Throwable e) {
				}

			});
		} else
			postRegistrationHttp(context, hash, name, surname, email, checked, checkToken, callback);
	}

	protected void postRegistrationHttp(Context context, String hash, String name
			, String surname, String email, boolean checked, boolean checkToken, final HttpCallback callback) {
		JsonObject json = new JsonObject();
		JsonObject jsonReceipt = new JsonObject();
		jsonReceipt.addProperty("first_name", name);
		jsonReceipt.addProperty("last_name", surname);
		jsonReceipt.addProperty("email", email);
		if (checked)
			jsonReceipt.addProperty("hash_keychain", hash);
		else
			jsonReceipt.addProperty("hash_card", hash);
		json.add("user", jsonReceipt);

		String url;
		String access_token = CurrentAdmin.getInstance().getAccessToken(context);
		url = UnipiazzaParams.REGISTER_URL + "?access_token=" + access_token;

		Ion.with(context)
				.load(url)
				.setJsonObjectBody(json)
				.asJsonObject().setCallback(new FutureCallback<JsonObject>() {

					@Override
					public void onCompleted(Exception e, JsonObject result) {
						Log.v("UNIPIAZZA", "postRegistration result=" + result);
						Log.v("UNIPIAZZA", "postRegistration e=" + e);
						if (e == null) {
							try {
								if (!result.get("error").getAsBoolean())
									callback.onSuccess(result);
								else
									callback.onFail(result, e);
							} catch (Exception ex) {
								ex.printStackTrace();
								callback.onFail(result, ex);
							}
						} else
							callback.onFail(result, e);
					}
				});
	}
}
