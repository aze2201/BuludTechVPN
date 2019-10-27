package tk.usagis2.BuludTechVPN.CSV;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by UsagiS2 on 23/03/2016.
 */
public class HTTPLoader {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void get(String url, String cookie, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if(!cookie.equals(""))
            client.addHeader("Cookie", cookie);
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    public static void setMaxRetriesAndTimeout(int retry, int timeOut){
        client.setMaxRetriesAndTimeout(retry, timeOut);
    }
}
