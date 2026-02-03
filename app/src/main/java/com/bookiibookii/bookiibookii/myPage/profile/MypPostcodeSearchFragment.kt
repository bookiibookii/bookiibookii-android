package com.bookiibookii.bookiibookii.myPage.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.bookiibookii.bookiibookii.databinding.FragmentMypPostcodeSearchBinding

class MypPostcodeSearchFragment : Fragment() {

    private var _binding: FragmentMypPostcodeSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypPostcodeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        initWebView()
    }

    private fun initWebView() {
        val webView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // 자바스크립트 인터페이스 연결 (이름: "Android")
        webView.addJavascriptInterface(MyJavaScriptInterface(), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 페이지 로드 완료 시 다음 우편번호 API 실행
                webView.loadUrl("javascript:execDaumPostcode();")
            }
        }

        // 다음 우편번호 API를 실행할 HTML 코드를 직접 로드
        // (별도의 웹 서버 없이 앱 내에서 실행하기 위함)
        val html = """
            <html>
            <head>
                <script src="https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
            </head>
            <body>
                <div id="layer" style="display:block;width:100%;height:100%;-webkit-overflow-scrolling:touch;"></div>
                <script>
                    function execDaumPostcode() {
                        new daum.Postcode({
                            oncomplete: function(data) {
                                // 주소 선택 시 Android 인터페이스 호출
                                var fullAddr = data.address;
                                var extraAddr = '';

                                if(data.userSelectedType === 'R'){
                                    if(data.bname !== ''){ extraAddr += data.bname; }
                                    if(data.buildingName !== ''){ extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName); }
                                    fullAddr += (extraAddr !== '' ? ' ('+ extraAddr +')' : '');
                                }

                                window.Android.processDATA(data.zonecode, fullAddr);
                            },
                            width : '100%',
                            height : '100%'
                        }).embed(document.getElementById('layer'));
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        // HTML 데이터 로드
        webView.loadDataWithBaseURL("https://daum.net", html, "text/html", "UTF-8", null)
    }

    // 자바스크립트에서 호출할 브릿지 클래스
    inner class MyJavaScriptInterface {
        @JavascriptInterface
        fun processDATA(zonecode: String?, address: String?) {
            // 결과 데이터를 Bundle에 담아 이전 프래그먼트로 전달
            setFragmentResult("requestKeyPostcode", bundleOf(
                "zonecode" to zonecode,
                "address" to address
            ))

            // UI 스레드에서 백스택 제거 (화면 닫기)
            activity?.runOnUiThread {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}