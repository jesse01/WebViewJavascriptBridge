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

5. 用 excuteJavascript 方法执行脚本:

  excuteJavascript(script); //不需要返回值, script前不要加javascript:前缀

 或
 
  excuteJavascript(script, callback); //需要返回值, script前不要加javascript:前缀

  executeJavascript方法的内部实现机制:
  
  a. Android 4.4及更高版本下, 使用WebView.evaluateJavascript方法执行脚本;
  
  b. Android 4.4以下版本若需要返回值则采用addJavascriptInterface机制实现;
  
  c. Android 4.4以下版本若不需要返回值则使用loadUrl方法执行脚本.
  

  WebViewJavascriptBridge for iOS/OSX 的下载地址: https://github.com/jcccn/WebViewJavascriptBridge
