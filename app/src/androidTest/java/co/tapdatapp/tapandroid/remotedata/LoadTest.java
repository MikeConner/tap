/**
 * Not a real unit test, instead uses the unit test framework to load the server by
 * sending transactions a fast as possible
 */

package co.tapdatapp.tapandroid.remotedata;

import android.util.Log;

import java.util.Random;

import co.tapdatapp.tapandroid.BaseUnitTest;
import co.tapdatapp.tapandroid.service.TapTxn;

public class LoadTest extends BaseUnitTest {
    /**
     * Number of threads to run in parallel
     */
    private static final int THREADS = 5;
    /**
     * Number of requests to make on each thread
     */
    private static final int REQUESTS = 100;
    /**
     * The tag ID to test against
     */
    private static final String TAG_ID = "98f7db3d15";
    /**
     * The maximum amount per tap, the actual amount is random between 1 and this value
     */
    private static final int MAX_TAP = 1;
    /**
     * This is the currency ID to use
     */
    private static final int CURRENCY = 5;

    private static final String LABEL = "LOAD_TEST";

    public void testLoad() throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        OneLoadThread[] jobs = new OneLoadThread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            jobs[i] = new OneLoadThread();
            threads[i] = new Thread(jobs[i]);
            threads[i].start();
        }
        for (int i = 0; i < THREADS; i++) {
            threads[i].join();
            jobs[i].statistics();
        }
        fail("Failure to ensure display of output");
    }

    private class OneLoadThread implements Runnable {

        private long startTime;
        private long endTime;
        private final Random random = new Random();
        private int success = 0;
        private int failure = 0;
        private Exception lastFailure;

        @Override
        public void run() {
            runTest();
        }

        public void runTest() {
            TapTxn txn = new TapTxn();
            startTime = System.nanoTime();
            for (int i = 0; i < REQUESTS; i++) {
                txn.setTagID(TAG_ID);
                txn.setTxnAmount(random.nextInt(MAX_TAP) + 1);
                txn.setCurrencyId(CURRENCY);
                try {
                    txn.TapAfool();
                    success++;
                }
                catch (Exception e) {
                    Log.e(LABEL, e.getMessage());
                    lastFailure = e;
                    failure++;
                }
            }
            endTime = System.nanoTime();
        }

        public void statistics() {
            float elapsedSeconds = (endTime - startTime) / 1000000000;
            Log.i(LABEL, "Elapsed time: " + elapsedSeconds);
            Log.i(LABEL, REQUESTS / elapsedSeconds + " taps per second");
            Log.i(LABEL, success + " succeeded, " + failure + " failed");
            if (lastFailure != null) {
                Log.e(LABEL, "Last failure: ", lastFailure);
            }
        }
    }

}
