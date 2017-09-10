![](https://github.com/thepacific/rx2-timer/blob/master/rx2-timer/demo.png)

This is a simple rxjava2 timer. I copy this class into all the little apps I make. I'm tired of doing it. Now it's a library

[ ![Download](https://api.bintray.com/packages/thepacific/maven/rx2timer/images/download.svg) ](https://bintray.com/thepacific/maven/rx2timer/_latestVersion)

Usage
-----

```java
timer = Rx2Timer.builder()
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

timer.start();
timer.stop();
timer.restart();
timer.pause();
timer.resume();
                
```

Gradle
--------

```groovy
compile 'com.github.thepacific:rx2timer:0.0.1'
```

License
-------

[The MIT License ](https://opensource.org/licenses/MIT)

