/**
 * Use Amazon AWS to store images, videos, etc
 */

package co.tapdatapp.tapandroid.remotedata;

import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class AmazonStorage implements RemoteStorageDriver {

    private static AmazonS3Client s3Client;

    static {
        Log.d("AWS", TapApplication.string(R.string.AWS_ACCESS_ID));
        Log.d("AWS", TapApplication.string(R.string.AWS_SECRET_KEY));
        s3Client = new AmazonS3Client(
            new BasicAWSCredentials(
                TapApplication.string(R.string.AWS_ACCESS_ID),
                TapApplication.string(R.string.AWS_SECRET_KEY)
            )
        );
    }

    @Override
    public String store(byte[] data) {
        String key = UUID.randomUUID().toString();
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(data.length);
        String bucket = TapApplication.string(R.string.AWS_BUCKET);
        PutObjectRequest por = new PutObjectRequest(
            bucket,
            key,
            new ByteArrayInputStream(data),
            metaData
        );
        s3Client.putObject(por);
        return s3Client.getResourceUrl(bucket, key);
    }
}
