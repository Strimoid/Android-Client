package pl.strimoid.lara.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Cancellable;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.ion.Ion;

import java.io.Serializable;

public class OAuth2 implements AsyncHttpClientMiddleware, Serializable {

    private final static OAuth2 mInstance = new OAuth2();

    private AccountManager mAccountManager;
    private Account mAccount;
    private Context mContext;
    private String mAccessToken;

    private OAuth2() {}

    public static OAuth2 getInstance() {
        return mInstance;
    }

    public void useAccount(Context context, Account account) {
        this.mContext = context;
        this.mAccount = account;

        this.mAccountManager = AccountManager.get(mContext);

        getToken();
    }

    private void getToken() {
        mAccountManager.getAuthToken(mAccount, "access", null, true, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    mAccessToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                } catch (Exception e) {}
            }
        }, null);
    }

    private void invalidateToken() {
        mAccountManager.invalidateAuthToken("pl.strimoid", mAccessToken);
    }

    @Override
    public Cancellable getSocket(GetSocketData data) {
        if (mAccessToken != null && !TextUtils.isEmpty(mAccessToken))
            data.request.addHeader("Authorization", "Bearer " + mAccessToken);

        return null;
    }

    @Override
    public void onSocket(OnSocketData data) {}

    @Override
    public void onHeadersReceived(OnHeadersReceivedData data) {
        String wwwAuthenticate = data.headers.getWwwAuthenticate();

        // If token is not valid anymore, we need to invalidate it
        if (wwwAuthenticate != null && wwwAuthenticate.contains("invalid_token"))
            invalidateToken();
    }

    @Override
    public void onBodyDecoder(OnBodyData data) {}

    @Override
    public void onRequestComplete(OnRequestCompleteData data) {}
}
