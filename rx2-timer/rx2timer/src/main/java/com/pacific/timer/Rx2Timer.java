package com.pacific.timer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public final class Rx2Timer {
    private Disposable disposable;
    private long take;
    private long period;
    private long initialDelay;
    private TimeUnit unit;
    private OnComplete onComplete;
    private OnCount onCount;
    private OnError onError;
    private long pauseTake = 0l;
    private long resumeTake = 0l;
    private boolean isPause = false;

    private Rx2Timer(Builder builder) {
        take = builder.take;
        period = builder.period;
        initialDelay = builder.initialDelay;
        unit = builder.unit;
        onComplete = builder.onComplete;
        onCount = builder.onCount;
        onError = builder.onError;
    }

    /**
     * is in pause state
     *
     * @return
     */
    public boolean isPause() {
        return isPause;
    }

    /**
     * restart timer , all pause states are cleaned
     *
     * @return
     */
    public Rx2Timer restart() {
        stop();
        return start();
    }

    /**
     * start timer
     *
     * @return
     */
    public Rx2Timer start() {
        if (isPause) return restart();
        if (disposable == null || disposable.isDisposed()) {
            disposable
                    = Observable.interval(initialDelay, period, unit)
                    .subscribeOn(Schedulers.single())
                    .take(take + 1)
                    .map(new Function<Long, Long>() {
                        @Override
                        public Long apply(@NonNull Long aLong) throws Exception {
                            pauseTake = aLong;
                            return take - aLong;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(@NonNull Long aLong) throws Exception {
                            if (onCount != null) {
                                onCount.onCount(aLong);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            if (onError != null) {
                                onError.onError(throwable);
                            }
                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            if (onComplete != null) {
                                onComplete.onComplete();
                            }
                        }
                    });
        }
        return this;
    }

    /**
     * stop timer and all pause states are cleaned
     */
    public void stop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (isPause) cleanPauseState();
    }

    /**
     * pause timer
     */
    public void pause() {
        if (isPause) return;
        stop();
        isPause = true;
        resumeTake += pauseTake;
    }

    /**
     * resume timer
     */
    public void resume() {
        if (!isPause) return;
        isPause = false;
        if (disposable == null || disposable.isDisposed()) {
            disposable
                    = Observable.interval(initialDelay, period, unit)
                    .subscribeOn(Schedulers.single())
                    .take(take + 1 - resumeTake)
                    .map(new Function<Long, Long>() {
                        @Override
                        public Long apply(@NonNull Long aLong) throws Exception {
                            pauseTake = aLong;
                            return take - aLong - resumeTake;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(@NonNull Long aLong) throws Exception {
                            if (onCount != null) {
                                onCount.onCount(aLong);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            cleanPauseState();
                            if (onError != null) {
                                onError.onError(throwable);
                            }
                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            cleanPauseState();
                            if (onComplete != null) {
                                onComplete.onComplete();
                            }
                        }
                    });
        }
    }

    /**
     * clean pause states
     */
    public void cleanPauseState() {
        isPause = false;
        resumeTake = 0l;
        pauseTake = 0l;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long take = 60;
        private long period = 1;
        private long initialDelay = 0;
        private TimeUnit unit = TimeUnit.SECONDS;
        private OnComplete onComplete;
        private OnCount onCount;
        private OnError onError;

        Builder() {
        }

        /**
         * counting number , default value is 60
         *
         * @param take take value
         * @return
         */
        public Builder take(int take) {
            this.take = take;
            return this;
        }

        /**
         * period, default value is 1
         *
         * @param period period value
         * @return
         */
        public Builder period(int period) {
            this.period = period;
            return this;
        }

        /**
         * delay to begin , default value is 0
         *
         * @param initialDelay delay value
         * @return
         */
        public Builder initialDelay(int initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * time unit , default value is TimeUnit.SECONDS
         *
         * @param unit unit value
         * @return
         */
        public Builder unit(TimeUnit unit) {
            this.unit = unit;
            return this;
        }

        public Builder onComplete(OnComplete onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public Builder onCount(OnCount onCount) {
            this.onCount = onCount;
            return this;
        }

        public Builder onError(OnError onError) {
            this.onError = onError;
            return this;
        }

        public Rx2Timer build() {
            return new Rx2Timer(this);
        }
    }

    public interface OnComplete {
        void onComplete();
    }

    public interface OnCount {
        void onCount(Long count);
    }

    public interface OnError {
        void onError(Throwable throwable);
    }
}
