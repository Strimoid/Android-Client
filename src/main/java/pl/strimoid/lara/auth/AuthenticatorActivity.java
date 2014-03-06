package pl.strimoid.lara.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import pl.strimoid.lara.R;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager mAccountManager;
    private String mAccessToken, mUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.authenticator_login);

        mAccountManager = AccountManager.get(getBaseContext());

        if (mAccountManager.getAccountsByType("pl.strimoid").length > 0
                && getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Toast.makeText(this,
                    "Możesz korzystać tylko z jednego konta jednocześnie", Toast.LENGTH_LONG);
            finish();
        }

        WebView wv = (WebView) findViewById(R.id.web_view);

        wv.setWebViewClient(new WebViewClient() {

            boolean authComplete = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                setProgressBarVisibility(true);
            }

            public void onProgressChanged(WebView view, int progress) {
                setProgress(progress * 100);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                setProgressBarVisibility(false);

                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authComplete = true;

                    Log.i("strimoid", "got auth code: " + uri.getQueryParameter("code"));

                    String authCode = uri.getQueryParameter("code");
                    useAuthCode(authCode);

                } else if (url.contains("error=access_denied")) {
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
        });

        Uri.Builder uriBuilder = Uri.parse("http://strimoid.pl/oauth2/authorize").buildUpon();
        uriBuilder.appendQueryParameter("response_type", "code");
        uriBuilder.appendQueryParameter("client_id", "droid");
        uriBuilder.appendQueryParameter("redirect_uri", "http://strimoid.pl/");
        uriBuilder.appendQueryParameter("scope", "basic contents entries notifications");
        uriBuilder.appendQueryParameter("state", "xyz");

        wv.loadUrl(uriBuilder.build().toString());
    }

    private void useAuthCode(String authCode) {
        Ion.with(this, "http://strimoid.pl/oauth2/token")
                .basicAuthentication("droid", "droid")
                .setBodyParameter("grant_type", "authorization_code")
                .setBodyParameter("code", authCode)
                .setBodyParameter("redirect_uri", "http://strimoid.pl/")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Toast.makeText(getApplicationContext(),
                                    "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_LONG).show();
                            return;
                        }

                        mAccessToken = result.get("access_token").getAsString();
                        loadUsername();
                    }
                });
    }

    private void loadUsername() {
        Ion.with(this, "http://api.strimoid.pl/me?access_token=" + mAccessToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Toast.makeText(getApplicationContext(),
                                    "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_LONG).show();
                            return;
                        }

                        mUsername = result.get("_id").getAsString();

                        Log.i("strimoid", "username: " + mUsername);

                        finishLogin();
                    }
                });
    }

    private void finishLogin() {
        String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        Account account = new Account(mUsername, accountType);

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false))
            mAccountManager.addAccountExplicitly(account, null, null);

        mAccountManager.setAuthToken(account, "access", mAccessToken);

        Log.i("auth_act", "set token | auth: " + mAccessToken);

        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAccessToken);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

}
