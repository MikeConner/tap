package co.tapdatapp.tapandroid.tags;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class ImageYapaLineItem
extends YapaLineItem
implements View.OnClickListener,
           TapBitmap.Callback,
           ManageTagActivity.YapaLineItemWithImage {

    private ImageButton imageButton;

    protected ImageYapaLineItem(ManageTagActivity a, View v) {
        super(a, v);
        imageButton = (ImageButton)v.findViewById(R.id.imageButtonYapaImage);
        imageButton.setOnClickListener(this);
    }

    @Override
    public void setValues(Yapa y) {
        String image = y.getImage();
        imageButton.setTag(image);
        if (image != null && !image.isEmpty()) {
            if ("null".equals(image)) {
                throw new AssertionError("the image is the string \"null\"");
            }
            new TapBitmap().execute(this, image);
        }
        super.setValues(y);
    }

    @Override
    protected void updateYapaRecord(boolean changed) {
        super.updateYapaRecord(changed);
    }

    /**
     * Handle the image button click event
     */
    @Override
    public void onClick(View v) {
        activity.setYapaImageSelectedCallback(this);
    }

    /**
     * Called when a new image has been selected
     *
     * @param imageUrl URL of the full-sized image
     * @param thumbUrl URL of the thumbnail of the image
     * @param cache reference to a cache object to get the images from
     */
    @Override
    public void onImageSet(String imageUrl, String thumbUrl, AndroidCache cache) {
        boolean changed = yapa.setImageIfChanged(imageUrl);
        changed |= yapa.setThumbIfChanged(thumbUrl);
        imageButton.setImageBitmap(TapBitmap.fetchFromCache(thumbUrl, cache));
        updateYapaRecord(changed);
    }

    /**
     * Called when the background task has fetched the image
     * @param image Bitmap of the requested image
     */
    @Override
    public void onImageRetrieved(Bitmap image) {
        imageButton.setImageBitmap(image);
    }
}
