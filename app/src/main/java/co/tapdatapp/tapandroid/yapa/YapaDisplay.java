/**
 * Class to centralize handling of Yapa.
 */

package co.tapdatapp.tapandroid.yapa;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class YapaDisplay {

    public final static String TRANSACTION_ID = "TxId";
    public final static String DELAY_TIME = "delayTime";

    public static final ScheduledExecutorService delayWorker;

    static {
        delayWorker = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Get a drawable of the correct icon for the type of Yapa
     *
     * @param t Transaction to determine the yapa type
     * @return drawable of the correct icon
     */
    public Drawable getIcon(Transaction t) {
        Resources res = TapApplication.get().getResources();
        switch(t.getContentType()){
            case Yapa.TYPE_IMAGE :
                return res.getDrawable(R.drawable.yapa_image);
            case Yapa.TYPE_URL :
                return res.getDrawable(R.drawable.yapa_url);
            case Yapa.TYPE_TEXT :
                return res.getDrawable(R.drawable.yapa_text);
            case Yapa.TYPE_COUPON :
                return res.getDrawable(R.drawable.yapa_coupon);
            case Yapa.TYPE_AUDIO :
                return res.getDrawable(R.drawable.yapa_audio);
            case Yapa.TYPE_VIDEO :
                return res.getDrawable(R.drawable.yapa_video);
            default :
                throw new AssertionError("Unknown Yapa type " + t.getContentType());
        }
    }

    /**
     * Get the correct Activity to display the yapa based on the type
     *
     * @param transaction the transaciton containing the yapa
     * @return class to instantiate to display the yapa
     */
    public Class getDisplayClass(Transaction transaction) {
        switch (transaction.getContentType()) {
            case Yapa.TYPE_IMAGE :
                return YapaImage.class;
            case Yapa.TYPE_URL :
                return YapaUrl.class;
            case Yapa.TYPE_TEXT :
                return YapaText.class;
            case Yapa.TYPE_COUPON :
                return  YapaCoupon.class;
            case Yapa.TYPE_AUDIO :
                return YapaAudio.class;
            case Yapa.TYPE_VIDEO :
                return YapaVideo.class;
            default :
                throw new AssertionError(
                    "Invalid Yapa Type: " + transaction.getContentType()
                );
        }
    }

}
