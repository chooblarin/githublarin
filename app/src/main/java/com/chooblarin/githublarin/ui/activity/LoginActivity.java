package com.chooblarin.githublarin.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.chooblarin.githublarin.Application;
import com.chooblarin.githublarin.R;
import com.chooblarin.githublarin.api.auth.Credential;
import com.chooblarin.githublarin.api.client.GitHubApiClient;
import com.chooblarin.githublarin.databinding.ActivityLoginBinding;
import com.chooblarin.githublarin.model.User;
import com.trello.rxlifecycle.ActivityEvent;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {

    public static Intent createIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    private ActivityLoginBinding binding;

    @Inject
    GitHubApiClient apiClient;

    final private View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String username = binding.editTextUsername.getText().toString();
            final String password = binding.editTextPassword.getText().toString();
            login(username, password);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.textLoginButton.setOnClickListener(onLoginClickListener);
        checkAuth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }

    @Override
    protected void setupActivityComponent() {
        apiClient = Application.get(this).getAppComponent().apiClient();
    }

    private void checkAuth() {
        Context context = getApplicationContext();
        String username = Credential.username(context);
        String password = Credential.password(context);

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            login(username, password);
        }
    }

    private void login(String username, String password) {
        apiClient.login(username, password)
                .compose(this.<User>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    apiClient.setUser(user);
                    startActivity(MainActivity.createIntent(LoginActivity.this, user));
                    finish();

                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ログイン失敗", Toast.LENGTH_SHORT).show();
                });
    }
}
