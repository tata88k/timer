package com.pacific.app.timber;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.pacific.app.timber.databinding.ActivityMainBinding;
import com.pacific.timer.rx3.Rx3Timer;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityMainBinding binding;
    Rx3Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.start.setOnClickListener(this);
        binding.stop.setOnClickListener(this);
        binding.restart.setOnClickListener(this);
        binding.pause.setOnClickListener(this);
        binding.resume.setOnClickListener(this);

        timer = Rx3Timer.builder()
                .initialDelay(0) //default is 0
                .period(1) //default is 1
                .take(30) //default is 60
                .unit(TimeUnit.SECONDS) // default is TimeUnit.SECONDS
                .onEmit(count -> {
                    if (count < 10) {
                        binding.text.setText("0" + count + " s");
                    } else {
                        binding.text.setText(count + " s");
                    }
                })
                .onError(e -> binding.text.setText(R.string.count))
                .onComplete(() -> binding.text.setText(R.string.count))
                .build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                timer.start();
                break;
            case R.id.stop:
                timer.stop();
                binding.text.setText(R.string.count);
                break;
            case R.id.restart:
                timer.restart();
                break;
            case R.id.pause:
                timer.pause();
                break;
            case R.id.resume:
                timer.resume();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        timer.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.stop();
    }
}
