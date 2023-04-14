package com.voitov.ownobservableimpl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button buttonMessageProducer;
    private TextView textViewElapsedTime;
    private TextView textViewConsumedMessages;
    private ProgressBar progressBarGoingOn;

    private ProduceConsumerBenchmarkUseCase useCase;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupDI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initState();
    }

    private void setupDI() {
        ApplicationCompositionRule composition = ((MyApplication) getApplication()).getCompositionRoot();
        useCase = new ProduceConsumerBenchmarkUseCase(
                composition.getPoolExecutor()
        );
    }

    private void initState() {
        textViewElapsedTime.setText("");
        textViewConsumedMessages.setText("");
        progressBarGoingOn.setVisibility(View.GONE);

        buttonMessageProducer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarGoingOn.setVisibility(View.VISIBLE);
                buttonMessageProducer.setEnabled(false);
                disposable = useCase.startBenchmarking()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(MainActivity.this::onBenchmarkCompleted);
            }
        });
    }

    private void initViews() {
        buttonMessageProducer = findViewById(R.id.buttonMessageProducer);
        textViewElapsedTime = findViewById(R.id.textViewElapsedTime);
        textViewConsumedMessages = findViewById(R.id.textViewConsumedMessages);
        progressBarGoingOn = findViewById(R.id.progressBarGoingOn);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @UiThread
    private void onBenchmarkCompleted(ScreenState state) {
        if (state instanceof ScreenState.ComputationCompleted) {
            ScreenState.ComputationCompleted explicitCast = (ScreenState.ComputationCompleted) state;
            textViewElapsedTime.setText(String.format(getResources().getString(R.string.pattern_elapsed_time), explicitCast.getElapsedTime()));
            textViewConsumedMessages.setText(String.valueOf(explicitCast.getConsumedMessages()));
            progressBarGoingOn.setVisibility(View.GONE);
            buttonMessageProducer.setEnabled(true);
        } else if (state instanceof ScreenState.ComputationCancelled) {
            textViewConsumedMessages.setText("Computation was cancelled");
            progressBarGoingOn.setVisibility(View.GONE);
            buttonMessageProducer.setEnabled(true);
        } else {
            throw new IllegalStateException("Unexpected screen state: " + state);
        }
    }
}
