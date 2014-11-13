package scal.io.liger;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/**
 * Created by mnbogner on 11/7/14.
 */
public class LigerDownloaderService extends DownloaderService {

    public static final String BASE64_PUBLIC_KEY = "???";

    @Override
    public String getPublicKey() {
        return null; // need Base64-encoded RSA public key of publisher account
    }

    @Override
    public byte[] getSALT() {
        return new byte[0]; // how to create a valid SALT?
    }

    @Override
    public String getAlarmReceiverClassName() {
        return LigerAlarmReceiver.class.getName();
    }
}
