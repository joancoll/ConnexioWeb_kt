package cat.dam.andy.connexioweb_kt

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Variables de membre i objectes
    private lateinit var layout: RelativeLayout
    private lateinit var etUrl: EditText
    private lateinit var btnSearch: Button
    private lateinit var connectionDetect: ConnectionDetect
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initListeners()
        initWebView()
    }

    private fun initViews() {
        layout = findViewById(R.id.rl_main)
        etUrl = findViewById(R.id.et_url)
        btnSearch = findViewById(R.id.btn_search)
        webView = findViewById(R.id.wv_page)
        connectionDetect = ConnectionDetect(applicationContext)
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.GONE
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(progressBar, params)
    }

    private fun initListeners() {
        btnSearch.setOnClickListener {
            getWeb(etUrl.text.toString())
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true // Activa Javascript
        webSettings.builtInZoomControls = true // Permet fer zoom amb gestos
        webSettings.loadWithOverviewMode = true // Carrega la web en una vista completa
        webSettings.useWideViewPort =
            true // Mostra la vista com un navegador i escala per veure tot

        // Utilitzarem .setWebChromeClient en lloc de WebViewClient si necessitem més control
        // Per exemple, percentatge de càrrega, diàleg Javascript, obtenció de la icona favicon...
        initWebClient()
    }

    private fun initWebClient() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                progressBar.visibility = View.VISIBLE
                getWeb(request.url.toString())
                return true
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed()
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
                // Per tornar a permetre la interacció de l'usuari, afegim el següent codi
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }
    }

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    private fun getWeb(url: String) {
        if (connectionDetect.haveConnection()) {
            // Per desactivar la interacció de l'usuari, afegim el següent codi
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            if (url.isEmpty()) {
                etUrl.setText("https://www.google.cat")
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                etUrl.setText("http://$url")
            }
            progressBar.visibility = View.VISIBLE
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(etUrl.text.toString())
        } else {
            webView.loadUrl("about:blank")
            createToast(getString(R.string.dont_have_connection))
        }
    }

    private fun createToast(text: String) {
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Si es pot tornar enrere en la web, ho fem; sinó, cridem el comportament per defecte
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
