package com.voitov.ownobservableimpl;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ProduceConsumerBenchmarkUseCase.OnBenchmarkListener {
    private Button buttonMessageProducer;
    private TextView textViewElapsedTime;
    private TextView textViewConsumedMessages;
    private ProgressBar progressBarGoingOn;

    private ProduceConsumerBenchmarkUseCase useCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initState();

        //DI to be added
        useCase = new ProduceConsumerBenchmarkUseCase();
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
                useCase.startBenchmarkingWithCallback();
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
        useCase.registerListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        useCase.unregisterListener(this);
    }

    @UiThread
    @Override
    public void onBenchmarkCompleted(ScreenState state) {
        if (state instanceof ScreenState.ComputationCompleted) {
            ScreenState.ComputationCompleted explicitCast = (ScreenState.ComputationCompleted) state;
            textViewElapsedTime.setText(String.format(getResources().getString(R.string.pattern_elapsed_time), explicitCast.getElapsedTime()));
            textViewConsumedMessages.setText(String.valueOf(explicitCast.getConsumedMessages()));
            progressBarGoingOn.setVisibility(View.GONE);
            buttonMessageProducer.setEnabled(true);
        } else {
            throw new IllegalStateException("Unexpected screen state: " + state);
        }
    }
}