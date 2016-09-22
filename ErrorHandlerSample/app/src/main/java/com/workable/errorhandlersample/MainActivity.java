package com.workable.errorhandlersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Range;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.workable.errorhandler.Action;
import com.workable.errorhandler.ErrorHandler;
import com.workable.errorhandlersample.models.GithubUser;
import com.workable.errorhandlersample.retrofit.GithubService;
import com.workable.errorhandlersample.retrofit.Routes;

import java.net.UnknownHostException;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String USERNAME = "charbgrfsdfds";

    private Retrofit retrofit;
    private TextView githubUsername;
    private EditText searchTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        githubUsername = (TextView) findViewById(R.id.found_username);
        searchTerm = (EditText) findViewById(R.id.github_username);

        Retrofit.Builder builder = new Retrofit.Builder()
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .baseUrl(Routes.GITHUB_API_BASE);

        retrofit = builder.build();

        initIMEActionListener();
    }

    private void initIMEActionListener() {
        searchTerm.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    initiateAPICall(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void initiateAPICall(String username) {
        retrofit
                .create(GithubService.class)
                .searchUser(username)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GithubUser>() {
                    @Override
                    public void onCompleted() {
                        //nothing here
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleWithErrorHandler(e);
                    }

                    @Override
                    public void onNext(GithubUser githubUser) {
                        showUser(githubUser);
                    }
                });
    }

    private void handleWithErrorHandler(Throwable e) {
        ErrorHandler
                .create()
                .on(404, new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        showMessage("KABOOM!!! User not found!");
                    }
                })
                .on("offline", new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        showMessage("Network dead!");
                        errorHandler.skipDefaults();
                    }
                })
                .always(new Action() {
                    @Override
                    public void execute(Throwable throwable, ErrorHandler errorHandler) {
                        githubUsername.setText("");
                    }
                })
                .handle(e);
    }

    private void showUser(GithubUser githubUser) {
        if (githubUser == null) return;

        githubUsername.setText(githubUser.getName());
        githubUsername.append("\n");
        githubUsername.append(githubUser.getUrl());
        githubUsername.append("\n");
        githubUsername.append(githubUser.getEmail());

    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
