package demo_rx1;

import rx.Observable;
import rx.Single;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MainDemoRx1 {

    public static int retryCountMax = 3;
    public static int retryDelay = 1000;


    public static void main(String[] args) throws InterruptedException {
        testMainIdea();

//        testIdea1();
    }

    private static void testIdea1() throws InterruptedException {
        Observable.range(1, 5)
                .take(retryCountMax)
                .delay(retryDelay, TimeUnit.MILLISECONDS)
                .subscribe(i -> System.out.println("currentTime = " + LocalDateTime.now() + " , value = " + i + ", thread = " + Thread.currentThread().getName()));


        Thread.sleep(10000L);
    }

    public static int testId = 6;
    private static final Object lock = new Object();

    private static void testMainIdea() {
        String str = Single.just(testId)
                .map(id -> generateIdStr(testId).orElseThrow(() -> {
                    synchronized (lock) {
                        System.out.println("not found product_id " + testId + ", time = " + LocalDateTime.now());
                        testId--;
                    }
                    return new NullPointerException("product_id: ");
                }))
                .retryWhen(error -> error.take(retryCountMax)
                        .doOnNext(value -> System.out.println("test emit here: value = " + value + ", thread = " + Thread.currentThread().getName()))
                        .delay(retryDelay, TimeUnit.MILLISECONDS))
                .onErrorResumeNext(Single.just(null))
                .toBlocking()
                .value();

        System.out.println("------- str = " + str);
    }

    private static Optional<String> generateIdStr(Integer id) {
        if (id < 5) {
            return Optional.of("str" + id);
        }

        try {
            Thread.sleep(10000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new NullPointerException("testNullHere");
    }

}

// @Override
//    public Optional<TblProduct> getProductById(long pid) {
//        TblProduct product = Single.just(pid)
//                .map(id -> catalogRepository.getProductById(id).orElseThrow(() -> {
//                    log.info("not found product_id {}", id);
//                    return new ProductNotFoundException("product_id: " + id);
//                }))
//                .retryWhen(errors -> errors.take(retryCountMax).delay(retryDelay, TimeUnit.MILLISECONDS))
//                .onErrorResumeNext(Single.just(null))
//                .toBlocking()
//                .value();
//
//        return product != null? Optional.of(product): Optional.empty();
//    }
