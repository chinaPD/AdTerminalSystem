package com.bupt.adsystem.RemoteServer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.Const;
import com.bupt.adsystem.Utils.Property;
import com.bupt.adsystem.model.ElevatorInfo;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import org.xutils.common.util.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hadoop on 16-10-21.
 */
public class NetUtil {

    private static final String TAG = "NetUtil";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    public static final String NameSpace = Property.SOAP_NAME_SPACE;

    public static final int QUEST_SUCCESS = 1;
    public static final int MALFORMED_URL = 2;
    public static final int IO_EXCEPTION = 3;
    public static final int QUEST_FileServer_SUCCESS = 4;
    public static final int QUEST_Monitor_SUCCESS = 5;

    private static ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public static void asyncReportElevatorInfo(ElevatorInfo info, final Handler handler) {
        asyncReportElevatorInfo(Property.HTTP_GET_UPLOAD_INFO_URL_BASE, info, handler);
    }

    public static void asyncReportElevatorInfo(final String serverUrl, ElevatorInfo info, final Handler handler) {
        final String httpGetUrl = convertElevatorInfo2HttpGetUrl(serverUrl, info);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(httpGetUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        if (handler != null) handler.sendEmptyMessage(Const.UpdateElevatorInfoSuccess);
                    } else {
                        if (handler != null) handler.sendEmptyMessage(Const.UpdateElevatorInfoFailed);
                    }
                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    if (handler != null) handler.sendEmptyMessage(Const.UpdateElevatorInfoFailed);
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    if (handler != null) handler.sendEmptyMessage(Const.UpdateElevatorInfoFailed);
                    e.printStackTrace();
                } catch (IOException e) {
                    if (handler != null) handler.sendEmptyMessage(Const.UpdateElevatorInfoFailed);
                    e.printStackTrace();
                }
            }
        };
        mExecutor.execute(runnable);
    }

    public static void postRequestTextFile(final String serverUrl, final String content, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoInput(true);     //设置这个连接是否可以写入数据
                    httpURLConnection.setDoOutput(true);    //设置这个连接是否可以输出数据
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");//设置消息的类型
                    httpURLConnection.connect();
                    OutputStream out = httpURLConnection.getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                    bw.write(content);
                    bw.flush();
                    out.close();
                    bw.close();

                    int httpCode;
                    if ((httpCode = httpURLConnection.getResponseCode()) == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        String text = read(inputStream).toString();
                        if (DEBUG) Log.d(TAG, text);
                        Message message = new Message();
                        message.what = QUEST_SUCCESS;
                        message.obj = text;
                        handler.sendMessage(message);
                    } else {
                        if (DEBUG) Log.d(TAG, "Url Connection Failed!\n" +
                                        "\tResponse Code is " + httpCode);
                    }

                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    // url converting failed
                    handler.sendEmptyMessage(MALFORMED_URL);
                    e.printStackTrace();
                } catch (IOException e) {
                    // url openConnection failed
                    handler.sendEmptyMessage(IO_EXCEPTION);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void getRequestTextFile(final String serverUrl, final Handler handler) {
        getRequestTextFile(serverUrl, handler, QUEST_SUCCESS);
    }

    public static void getRequestTextFile(final String serverUrl, final Handler handler, final int handlerCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DEBUG) Log.d(TAG, "Server Url: " + serverUrl);
                    URL url = new URL(serverUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();

                        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
                        StringBuffer buffer = new StringBuffer();
                        String line = "";
                        while ((line = in.readLine()) != null){
                            buffer.append(line);
                        }

//                        String text = read(inputStream).toString();
                        String text = buffer.toString();

                        if (DEBUG) Log.d(TAG, text);
                        Message message = new Message();
                        message.what = handlerCode;
                        message.obj = text;
                        handler.sendMessage(message);
                    } else {
                        if (DEBUG) Log.d(TAG, "Url Connection Failed!");
                    }

                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    // url converting failed
                    handler.sendEmptyMessage(MALFORMED_URL);
                    e.printStackTrace();
                } catch (IOException e) {
                    // url openConnection failed
                    handler.sendEmptyMessage(IO_EXCEPTION);
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 读取流中的数据
     */
    public static StringBuilder read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder;
    }

    // TODO: 根据客户端与服务器的交互协议，产生信息上报url，需要服务器与客户端商讨制定
    private static String convertElevatorInfo2HttpGetUrl(int isRepair, int moverDir, int battery, int doorOpen,
                                            int hasPerson, int CFloor, int CSignal) {
        String baseUrl = Property.HTTP_GET_UPLOAD_INFO_URL_BASE;
        StringBuilder sb = new StringBuilder();
        sb.append("Device_Id=10000000000000000001");
        sb.append("&isRepair=" + isRepair + "&");
        sb.append("&moveDirection" + moverDir);
        sb.append("&battery="+battery);
        sb.append("&isDoorOpen="+doorOpen);
        sb.append("&hasPerson="+hasPerson);
        sb.append("&CFloor="+CFloor);
        sb.append("&CSignal="+CSignal);
        return baseUrl + sb.toString();
    }
    private static String convertElevatorInfo2HttpGetUrl(String serverUrlBase, ElevatorInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append(serverUrlBase).append("?");
        sb.append("Device_Id=10000000000000000001");
        sb.append("&isRepair=" + info.getIsRepair() + "&");
        sb.append("&moveDirection" + info.getMoveDir());
        sb.append("&battery="+ info.getBattery());
        sb.append("&isDoorOpen="+ info.getDoorOpen());
        sb.append("&hasPerson="+ info.getHasPerson());
        sb.append("&CFloor="+ info.getCFloor());
        sb.append("&CSignal="+ info.getCSignal());
        return sb.toString();
    }

    public static void requestJsonFromWebservice(final String wsdl, final String serverMethodName,
                                                 final String jsonStr, final Handler handler) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpTransportSE httpSE = new HttpTransportSE(wsdl);
                SoapObject soapObject = new SoapObject(NameSpace, serverMethodName);
//                soapObject.addProperty("jsonStr", jsonStr);
                PropertyInfo propertyInfo = new PropertyInfo();
                propertyInfo.setName("jsonStr");
                propertyInfo.setValue(jsonStr);
                soapObject.addProperty(propertyInfo);
                // the SoapEnvelope Version should be consistent with the Server,
                // or it could happens XmlPullParserException
                final SoapSerializationEnvelope soapEnvelope = new
                        SoapSerializationEnvelope(SoapEnvelope.VER11);
                soapEnvelope.dotNet = false;
                soapEnvelope.bodyOut = soapObject;
                soapEnvelope.setOutputSoapObject(soapObject);
                try {
                    httpSE.call(wsdl + serverMethodName, soapEnvelope);
                    if (soapEnvelope.getResponse() != null) {
                        String result = soapEnvelope.getResponse().toString();
                        if (DEBUG) Log.d(TAG, "WebService response:\n" +
                                result);
                        if (handler == null) return;
                        Message message = new Message();
                        message.what = QUEST_FileServer_SUCCESS;
                        message.obj = result;
                        handler.sendMessage(message);
                    } else {
                        if (DEBUG) Log.d(TAG, "WebService request failed!");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
