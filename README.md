# WebViewJavascriptBridge
WebViewJavascriptBridge for Android 是 WebViewJavascriptBridge for iOS/OSX的Android 实现, 项目里的WebViewJavascriptBridge.js.txt, 及测试网页ExampleApp.html 直接来自 WebViewJavascriptBridge for iOS/OSX.

使用方法:

1. 复制WVJBWebViewClient.java 到 Android 项目 src 下的一个子目录中, 按需修改package名称;
2. 复制WebViewJavascriptBridge.js.txt到assets目录;
3. 子类化 WVJBWebViewClient, 例如:

  class MyWebViewClient extends WVJBWebViewClient {
  
		public MyWebViewClient(WebView webView) {

			//需要支持JS send 方法时
			super(webView, new WVJBWebViewClient.WVJBHandler() {
  				@Override
  				public void request(Object data, WVJBResponseCallback callback) {		
  			  		//处理代码
  				}
  			});
			
	  	  	/*
  		  	//不需要支持JS send 方法时
  	  		super(webView);
  	  		*/  
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			//处理代码
			super.onPageFinished(view, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//处理代码
			return super.shouldOverrideUrlLoading(view, url);
		}

4. 设置WebView
  webView.setWebViewClient(new MyWebViewClient(webView));  

5. excuteJavascript 方法执行脚本:

  excuteJavascript(script); //不需要返回值, script前不要加javascript:前缀

 或
 
  excuteJavascript(script, callback); //需要返回值, script前不要加javascript:前缀

  当需要返回值时, Android 4.4及更高版本内部使用WebView.evaluateJavascript方法; 4.4以前版本则调用 mWebViewCore.stringByEvaluatingJavaScriptFromString 内部方法, 该方法要求Javascript方法必须能返回字符串.
  (注:因为stringByEvaluatingJavaScriptFromString在三星手机上出错,已改用addJavascriptInterface方法实现)
  
  如:
  excuteJavascript("document.body.offsetHeight", new JavascriptCallback() {
        public void onReceiveValue(String height) {
           //错误, height=null
        }
  }); 
 excuteJavascript("eval(document.body.offsetHeight).toString()", new JavascriptCallback() {
        public void onReceiveValue(String height) {
           //正确
        }
  }); 
  
  不需要返回值时可以直接调用:
  excuteJavascript("location.href='http://www.baidu.com/'”);

  WebViewJavascriptBridge for iOS/OSX 的下载地址: https://github.com/jcccn/WebViewJavascriptBridge
