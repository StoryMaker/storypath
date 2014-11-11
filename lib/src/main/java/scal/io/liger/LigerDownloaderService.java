package scal.io.liger;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/**
 * Created by mnbogner on 11/7/14.
 */
public class LigerDownloaderService extends DownloaderService {

    public static final String BASE64_PUBLIC_KEY = "???";

    @Override
    public String getPublicKey() {
        return null;
    }

    @Override
    public byte[] getSALT() {
        return new byte[0];
    }

    @Override
    public String getAlarmReceiverClassName() {
        return LigerAlarmReceiver.class.getName();
    }
}