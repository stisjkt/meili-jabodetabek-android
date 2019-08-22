package utilities;

import android.webkit.JavascriptInterface;

import listeners.OnWebInterfaceCalledListener;

/**
 * Author : Rahadi Jalu
 * Email  : 14.8325@stis.ac.id
 * Company: Politeknik Statistika STIS
 */
public class WebAppInterface {

    private OnWebInterfaceCalledListener listener;

    public WebAppInterface(OnWebInterfaceCalledListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void doEvent(String type) {
        if (listener != null) {
            listener.onEvent(type);
        }
    }
}
