# WebViewJavascriptBridge
WebViewJavascriptBridge for Android 是 WebViewJavascriptBridge for iOS/OSX的Android 实现, 项目里的WebViewJavascriptBridge.js.txt, 及测试网页ExampleApp.html 直接来自 WebViewJavascriptBridge for iOS/OSX.

使用方法:

1. 复制WVJBWebViewClient.java 到 Android 项目 src 下的一个子目录中, 按需修改package名称;
2. 复制WebViewJavascriptBridge.js.txt到assets目录;
3. 创建WVJBWebViewClient子类使用, 例如:

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

stringByEvaluatingJavaScriptFromString 方法:
  stringByEvaluatingJavaScriptFromString返回字符串接果, 参数必须是能够返回字串符串的方法. 
  
  String height = stringByEvaluatingJavaScriptFromString("document.body.offsetHeight"); //错误, 将返回null
  String height = stringByEvaluatingJavaScriptFromString("eval(document.body.offsetHeight).toString()"); //正确
  
  如果不需JS返回值, 建议仍使用loadUrl方法.
  
  
  WebViewJavascriptBridge for iOS/OSX 的下载地址: https://github.com/jcccn/WebViewJavascriptBridge
