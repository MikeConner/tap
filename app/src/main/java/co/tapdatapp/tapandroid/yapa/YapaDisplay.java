/**
 * Class to centralize handling of Yapa.
 */

package co.tapdatapp.tapandroid.yapa;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaDisplay {

    public static final String IMAGE = "image";
    public static final String URL = "url";
    public static final String TEXT = "text";

    public final static String TRANSACTION_ID = "TxId";

    public Drawable getIcon(Transaction t) {
        Resources res = TapApplication.get().getResources();
        switch(t.getContentType()){
            case IMAGE :
                return res.getDrawable(R.drawable.yapa_image);
            case URL :
                return res.getDrawable(R.drawable.yapa_link);
            case TEXT :
                return res.getDrawable(R.drawable._yapa_text);
            default :
                throw new AssertionError("Unknown Yapa type " + t.getContentType());
        }
    }

    public Class getDisplayClass(Transaction transaction) {
        switch (transaction.getContentType()) {
            case IMAGE :
                return YapaImage.class;
            case URL :
                return YapaUrl.class;
            case TEXT :
                return YapaText.class;
            default :
                throw new AssertionError(
                    "Invalid Yapa Type: " + transaction.getContentType()
                );
        }
    }

    public Class getSplashClass(Transaction transaction) {
        switch (transaction.getContentType()) {
            case IMAGE :
                return YapaImageSplash.class;
            case URL :
                return YapaUrlSplash.class;
            case TEXT :
                return YapaTextSplash.class;
            default :
                throw new AssertionError(
                        "Invalid Yapa Type: " + transaction.getContentType()
                );
        }
    }

}
