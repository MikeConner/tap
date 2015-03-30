/**
 * Use Amazon AWS to store images, videos, etc
 */

package co.tapdatapp.tapandroid.remotedata;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class AmazonStorage implements RemoteStorageDriver {

    private static AmazonS3Client s3Client;

    static {
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
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        try {
            String type = URLConnection.guessContentTypeFromStream(stream);
            if (type != null && !type.isEmpty()) {
                metaData.setContentType(type);
                MimeTypeMap mtm = MimeTypeMap.getSingleton();
                key += "." + mtm.getExtensionFromMimeType(type);
            }
        }
        catch (IOException ioe) {
            // This happens if the mime-type cannot be determined, in
            // which case we proceed without specifying a mime-type or
            // changing the file extension
        }
        Log.d("AWS", "Storing with name " + key);
        String bucket = TapApplication.string(R.string.AWS_BUCKET);
        PutObjectRequest por = new PutObjectRequest(
            bucket,
            key,
            stream,
            metaData
        );
        s3Client.putObject(por);
        return s3Client.getResourceUrl(bucket, key);
    }

    @Override
    public void overWrite(String url, byte[] data) {
        String[] pieces = url.split("/");
        String key = pieces[pieces.length - 1];
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
    }
}
