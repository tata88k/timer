package com.pacific.timer.rx2;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public final class Rx2Timer {

    private final long take;
    private final long period;
    private final long initialDelay;
    private final TimeUnit unit;
    private final Action onComplete;
    private final Consumer<Long> onEmit;
    private final Consumer<Throwable> onError;

    private long pauseTake = 0l;
    private long resumeTake = 0l;
    private boolean isPause = false;
    private boolean isStarted = false;
    private Disposable disposable;

    private Rx2Timer(Builder builder) {
        take = builder.take;
        period = builder.period;
        initialDelay = builder.initialDelay;
        unit = builder.unit;
        onComplete = builder.onComplete;
        onEmit = builder.onEmit;
        onError = builder.onError;
    }

    /**
     * is in pause state
     */
    public boolean isPause() {
        return isPause;
    }

    /**
     * restart timer , all pause states are cleaned
     */
    public Rx2Timer restart() {
        stop();
        return start();
    }

    /**
     * start timer
     */
    public Rx2Timer start() {
        if (isPause) {
            return restart();
        }
        if (disposable == null || disposable.isDisposed()) {
            disposable = Observable.interval(initialDelay, period, unit)
                    .subscribeOn(Schedulers.single())
                    .take(take + 1)
                    .map(new Function<Long, Long>() {
                        @Override
                        public Long apply(Long aLong) {
                            pauseTake = aLong;
                            return take - aLong;
                        }
                    })
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) {
                            isStarted = true;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (onEmit != null) {
                                onEmit.accept(aLong);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (onError != null) {
                                onError.accept(throwable);
                            }
                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            if (onComplete != null) {
                                onComplete.run();
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
        if (disposable != null) {
            disposable.dispose();
        }
        if (isPause) {
            cleanPauseState();
        }
    }

    /**
     * pause timer
     */
    public void pause() {
        if (isPause || !isStarted) {
            return;
        }
        stop();
        isPause = true;
        resumeTake += pauseTake;
    }

    /**
     * resume timer
     */
    public void resume() {
        if (!isPause) {
            return;
        }
        isPause = false;
        if (disposable == null || disposable.isDisposed()) {
            disposable
                    = Observable.interval(initialDelay, period, unit)
                    .subscribeOn(Schedulers.single())
                    .take(take + 1 - resumeTake)
                    .map(new Function<Long, Long>() {
                        @Override
                        public Long apply(Long aLong) {
                            pauseTake = aLong;
                            return take - aLong - resumeTake;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (onEmit != null) {
                                onEmit.accept(aLong);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            cleanPauseState();
                            if (onError != null) {
                                onError.accept(throwable);
                            }
                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            cleanPauseState();
                            if (onComplete != null) {
                                onComplete.run();
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
        isStarted = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private long take = 60;
        private long period = 1;
        private long initialDelay = 0;
        private TimeUnit unit = TimeUnit.SECONDS;
        private Action onComplete;
        private Consumer<Long> onEmit;
        private Consumer<Throwable> onError;

        Builder() {
        }

        /**
         * counting number , default value is 60
         *
         * @param take take value
         */
        public Builder take(int take) {
            this.take = take;
            return this;
        }

        /**
         * period, default value is 1
         *
         * @param period period value
         */
        public Builder period(int period) {
            this.period = period;
            return this;
        }

        /**
         * delay to begin , default value is 0
         *
         * @param initialDelay delay value
         */
        public Builder initialDelay(int initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * time unit , default value is TimeUnit.SECONDS
         *
         * @param unit unit value
         */
        public Builder unit(TimeUnit unit) {
            this.unit = unit;
            return this;
        }

        public Builder onComplete(Action onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public Builder onEmit(Consumer<Long> onEmit) {
            this.onEmit = onEmit;
            return this;
        }

        public Builder onError(Consumer<Throwable> onError) {
            this.onError = onError;
            return this;
        }

        public Rx2Timer build() {
            return new Rx2Timer(this);
        }
    }
}
