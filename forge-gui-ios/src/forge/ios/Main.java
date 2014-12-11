package forge.ios;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIPasteboard;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosrobovm.IOSFiles;

import forge.Forge;
import forge.assets.AssetsDownloader;
import forge.interfaces.IDeviceAdapter;
import forge.util.FileUtil;

public class Main extends IOSApplication.Delegate {

    @Override
    protected IOSApplication createApplication() {
        final String assetsDir = new IOSFiles().getLocalStoragePath() + "/../../forge.ios.Main.app/";
        if (!AssetsDownloader.SHARE_DESKTOP_ASSETS) {
            FileUtil.ensureDirectoryExists(assetsDir);
        }

        final IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        final ApplicationListener app = Forge.getApp(new IOSClipboard(), new IOSAdapter(), assetsDir);
        final IOSApplication iosApp = new IOSApplication(app, config);
        return iosApp;
    }

    public static void main(String[] args) {
        final NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(args, null, Main.class);
        pool.close();
    }

    //special clipboard that works on iOS
    private static final class IOSClipboard implements com.badlogic.gdx.utils.Clipboard {
        @Override
        public String getContents() {
            return UIPasteboard.getGeneral().getString();
        }

        @Override
        public void setContents(final String contents0) {
            UIPasteboard.getGeneral().setString(contents0);
        }
    }

    private static final class IOSAdapter implements IDeviceAdapter {
        @Override
        public boolean isConnectedToInternet() {
            return true;
        }

        @Override
        public boolean isConnectedToWifi() {
            return true;
        }

        @Override
        public String getDownloadsDir() {
            return new IOSFiles().getExternalStoragePath();
        }

        @Override
        public boolean openFile(String filename) {
            return new IOSFiles().local(filename).exists();
        }

        @Override
        public void exit() {
            // Not possible on iOS
        }
    }
}