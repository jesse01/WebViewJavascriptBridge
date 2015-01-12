package com.example.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class WVJBWebViewClient extends WebViewClient {
	
	private static final String kTag = "WVJB";
	private static final String kCustomProtocolScheme = "wvjbscheme";
	private static final String kQueueHasMessage = "__WVJB_QUEUE_MESSAGE__";
	
	private static boolean logging = false;

	protected WebView webView;

	private ArrayList<WVJBMessage> startupMessageQueue = null;
    private Map<String,WVJBResponseCallback> responseCallbacks = null;
    private Map<String,WVJBHandler> messageHandlers = null;
    private long uniqueId=0;
    private WVJBHandler messageHandler;
    private Method methodCallJavascript;
    private Object browserFrame;

	public interface WVJBResponseCallback {
		public void callback(Object data);
	}

	public interface WVJBHandler {
		public void request(Object data, WVJBResponseCallback callback);
	}

    public WVJBWebViewClient(WebView webView) {
    	this(webView, null);
    }

    public WVJBWebViewClient(WebView webView, WVJBHandler messageHandler) {
    	this.webView = webView;
    	this.webView.getSettings().setJavaScriptEnabled(true);
        this.responseCallbacks = new HashMap<String,WVJBResponseCallback>();
        this.messageHandlers = new HashMap<String,WVJBHandler>();
        this.startupMessageQueue = new ArrayList<WVJBMessage>();
        this.messageHandler = messageHandler;
    }

	public void enableLogging() { 
		logging = true; 
	}
    
    public void send(Object data) {
    	send(data, null);
    }

    public void send(Object data, WVJBResponseCallback responseCallback) {
    	sendData(data, responseCallback, null);
    }
    
    public void callHandler(String handlerName) {
    	callHandler(handlerName, null, null);
    }

    public void callHandler(String handlerName, Object data) {
    	callHandler(handlerName, data, null);
    }

    public void callHandler(String handlerName, Object data, WVJBResponseCallback responseCallback) {
        sendData(data, responseCallback, handlerName);
    }

    public void registerHandler(String handlerName, WVJBHandler handler) {
    	if(handlerName == null || handlerName.length() == 0 || handler == null) return;
    	messageHandlers.put(handlerName, handler);
    }

    private void sendData(Object data, WVJBResponseCallback responseCallback, String handlerName) {
    	if(data == null && (handlerName == null || handlerName.length() == 0)) return;
    	WVJBMessage message = new WVJBMessage();
    	if(data != null) {
			message.data = data;
    	}
        if (responseCallback != null) {
            String callbackId = "objc_cb_"+(++uniqueId);
            responseCallbacks.put(callbackId, responseCallback);
            message.callbackId = callbackId;
        }
        if(handlerName != null) {
            message.handlerName = handlerName;        	
        }
        queueMessage(message);
    }
    
    private void queueMessage(WVJBMessage message) {
        if (startupMessageQueue != null) {
        	startupMessageQueue.add(message);
        } else {
            dispatchMessage(message);
        }
    }

    private void dispatchMessage(WVJBMessage message) {
        String messageJSON = message2JSONObject(message)
        		.toString()
        		.replaceAll("\\\\", "\\\\\\\\")
        		.replaceAll("\"", "\\\\\"")
        		.replaceAll("\'", "\\\\\'")
        		.replaceAll("\n", "\\\\\n")
        		.replaceAll("\r", "\\\\\r")
        		.replaceAll("\f","\\\\\f");  
        
        log("SEND",messageJSON);

        if(Looper.myLooper() == Looper.getMainLooper()) {
            webView.loadUrl("javascript:WebViewJavascriptBridge._handleMessageFromObjC('"+messageJSON+"');");        	
        } else {
        	final String msgString = messageJSON;
        	new Runnable() {
		        @Override
		        public void run() {
		            webView.loadUrl("javascript:WebViewJavascriptBridge._handleMessageFromObjC('"+msgString+"');");        	
		        }
		    };        	
        }
    }
    
    private JSONObject message2JSONObject(WVJBMessage message) {
    	JSONObject jo=new JSONObject();
		try {
	    	if(message.callbackId != null) {
	    		jo.put("callbackId", message.callbackId);
	    	}
	    	if(message.data != null) {
	    		jo.put("data", message.data);    		
	    	}
	    	if(message.handlerName != null) {
	    		jo.put("handlerName", message.handlerName);    		    		
	    	}
	    	if(message.responseId != null) {
	    		jo.put("responseId", message.responseId);    		    		
	    	}	    	
	    	if(message.responseData != null) {
	    		jo.put("responseData", message.responseData);    		    		
	    	}	    	
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return jo;
    }

    private WVJBMessage JSONObject2WVJBMessage(JSONObject jo) {
    	WVJBMessage message=new WVJBMessage();
		try {
        	if(jo.has("callbackId")) {
        		message.callbackId = jo.getString("callbackId");
        	}
        	if(jo.has("data")) {
        		message.data = jo.get("data");
        	}
        	if(jo.has("handlerName")) {
        		message.handlerName = jo.getString("handlerName");
        	}
        	if(jo.has("responseId")) {
        		message.responseId = jo.getString("responseId");
        	}
        	if(jo.has("responseData")) {
        		message.responseData = jo.get("responseData");
        	}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return message;
    }

    private void flushMessageQueue()  {
        String messageQueueString = stringByEvaluatingJavaScriptFromString("WebViewJavascriptBridge._fetchQueue()");
        if(messageQueueString == null || messageQueueString.length() == 0) return;
		try {
	        JSONArray messages = new JSONArray(messageQueueString);
	        for (int i = 0; i < messages.length(); i++) {
	        	JSONObject jo = messages.getJSONObject(i);

	        	log("RCVD", jo);
	        	
	        	WVJBMessage message = JSONObject2WVJBMessage(jo);
	        	if(message.responseId != null) {
	                WVJBResponseCallback responseCallback = responseCallbacks.remove(message.responseId);
	                if(responseCallback != null) {
	                	responseCallback.callback(message.responseData);
	                }
	            } else {
	                WVJBResponseCallback responseCallback = null;
	                if (message.callbackId != null) {
		                final String callbackId = message.callbackId;
	                	responseCallback = new WVJBResponseCallback () {
							@Override
							public void callback(Object data) {
		                        WVJBMessage msg = new WVJBMessage();
		                        msg.responseId = callbackId;
		                        msg.responseData = data;
		                        queueMessage(msg);
							}	                		
	                	};
	                }
	                
	                WVJBHandler handler;
	                if (message.handlerName != null) {
	                    handler = messageHandlers.get(message.handlerName);
	                } else {
	                    handler = messageHandler;
	                }
	                if(handler!=null) {
	                	handler.request(message.data, responseCallback);
	                }
	            }
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    void log(String action, Object json) {
        if (!logging) return;
        String jsonString = json.toString();
        if (jsonString.length() > 500) {
        	Log.i(kTag, action+": "+ jsonString.substring(0, 500) + " [...]");
        } else {
        	Log.i(kTag, action+": "+ jsonString);
        }
    }

	public String stringByEvaluatingJavaScriptFromString(String script) {
        try {
        	if(browserFrame == null || methodCallJavascript == null) {
        		Object webViewObject;
        		Field wc;
                if (Build.VERSION.SDK_INT >= 16) {
                	Field mp = WebView.class.getDeclaredField("mProvider");
                	mp.setAccessible(true);
                	webViewObject = mp.get(webView);
                    wc = webViewObject.getClass().getDeclaredField("mWebViewCore");
                } else {
                	webViewObject = webView;
                    wc = WebView.class.getDeclaredField("mWebViewCore");                	
                }
                wc.setAccessible(true);
                Object webViewCore = wc.get(webViewObject);
                Field bf = webViewCore.getClass().getDeclaredField("mBrowserFrame");
                bf.setAccessible(true);
                browserFrame = bf.get(webViewCore);
                methodCallJavascript = browserFrame.getClass().getDeclaredMethod("stringByEvaluatingJavaScriptFromString",String.class);
        	}
            methodCallJavascript.setAccessible(true);        		
            Object value = methodCallJavascript.invoke(browserFrame, script);
            return String.valueOf(value);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
    
    @Override
	public void onPageFinished(WebView view, String url) {
    	try {  
            InputStream is = webView.getContext().getAssets().open("WebViewJavascriptBridge.js.txt");  
    		int size = is.available();  		    		  
            byte[] buffer = new byte[size];  
            is.read(buffer);  
            is.close();  
            String js = new String(buffer);
            webView.loadUrl("javascript:"+js);
        } catch (IOException e) {
        	e.printStackTrace();
        }  		    	
	    
	    if (startupMessageQueue != null) {
	    	for(int i = 0; i < startupMessageQueue.size(); i++) {
	    		dispatchMessage(startupMessageQueue.get(i));
	    	}
	    	startupMessageQueue = null;
	    }
	    super.onPageFinished(view, url);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    if (url.startsWith(kCustomProtocolScheme)) {
	    	if(url.indexOf(kQueueHasMessage) > 0) {
	            flushMessageQueue();
	        }
	        return true;
	    }
		return super.shouldOverrideUrlLoading(view, url);
	}
	
	private class WVJBMessage {
		Object data=null;
		String callbackId = null;
		String handlerName = null;	
		String responseId = null;
		Object responseData = null;
	}

}
