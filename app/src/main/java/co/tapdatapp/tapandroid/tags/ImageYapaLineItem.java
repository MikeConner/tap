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
implements View.OnClickListener, TapBitmap.Callback {

    private ImageButton imageButton;

    protected ImageYapaLineItem(ManageTagActivity a, View v) {
        super(a, v);
        imageButton = (ImageButton)v.findViewById(R.id.imageButtonYapaImage);
        imageButton.setOnClickListener(this);
    }

    public void setValues(Yapa y) {
        super.setValues(y);
        String image = y.getImage();
        imageButton.setTag(image);
        if (image != null && !image.isEmpty()) {
            new TapBitmap().execute(this, image);
        }
    }

    protected void updateYapaRecord() {
        yapa.setImage(yapa.getImage());
        super.updateYapaRecord();
    }

    /**
     * Handle the image button click event
     */
    @Override
    public void onClick(View v) {
        activity.setYapaImageSelectedCallback(this);
    }

    public void onImageSet(String imageUrl, String thumbUrl, AndroidCache cache) {
        yapa.setImage(imageUrl);
        yapa.setThumb(thumbUrl);
        imageButton.setImageBitmap(TapBitmap.fetchFromCache(thumbUrl, cache));
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
