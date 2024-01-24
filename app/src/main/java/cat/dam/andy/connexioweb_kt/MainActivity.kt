package cat.dam.andy.connexioweb_kt

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.webkit.WebViewClient.ERROR_CONNECT
import android.webkit.WebViewClient.ERROR_HOST_LOOKUP
import android.webkit.WebViewClient.ERROR_IO
import android.webkit.WebViewClient.ERROR_TIMEOUT
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var layout: RelativeLayout
    private lateinit var etUrl: EditText
    private lateinit var btnSearch: Button
    private lateinit var connectionDetect: ConnectionDetect
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var isTimeout = false


    // Handler per gestionar el temps d'espera de 30 segons
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initListeners()
        initWebView()
    }


    private fun initViews() {
        // Inicialitza les vistes
        layout = findViewById(R.id.rl_main)
        etUrl = findViewById(R.id.et_url)
        btnSearch = findViewById(R.id.btn_search)
        webView = findViewById(R.id.wv_page)
        // Inicialitza el gestor de connexió
        connectionDetect = ConnectionDetect(applicationContext)
        // Inicialitza la barra de progrés
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.GONE
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(progressBar, params)
    }

    private fun initListeners() {
        // Inicialitza els listeners dels elements de la interfície d'usuari
        btnSearch.setOnClickListener {
            clearAndLoadBlankPage()
        }
    }

    private fun clearAndLoadBlankPage() {
        // Atura la càrrega de la pàgina actual
        webView.stopLoading()
        // Carrega una pàgina en blanc
        webView.loadUrl("about:blank")
        // Restaura la interacció de l'usuari
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        // Amaga la barra de progrés
        progressBar.visibility = View.GONE
        getWeb(etUrl.text.toString())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        // Inicialitza la configuració del WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true // Activa Javascript
        webSettings.builtInZoomControls = true // Permet fer zoom amb gestos
        webSettings.loadWithOverviewMode = true // Carrega la web en una vista completa
        webSettings.useWideViewPort =
            true // Mostra la vista com un navegador i escala per veure tot
        // Utilitzarem .setWebChromeClient en lloc de WebViewClient si necessitem més control
        // Per exemple, percentatge de càrrega, diàleg Javascript, obtenció de la icona favicon...
        // Inicialitza el WebClient
        initWebClient()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebClient() {
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // Inicia la càrrega de la nova URL
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

                // Aquí pots afegir la lògica que vulguis executar quan la pàgina s'hagi carregat completament
                // Per exemple, pots mostrar un missatge, fer alguna comprovació, etc.
                checkIfPageLoadedSuccessfully(view)
            }

            private fun checkIfPageLoadedSuccessfully(view: WebView) {
                if (view.url?.startsWith("about:blank") == true) {
                    // La pàgina no s'ha carregat amb èxit
                    createToast(getString(R.string.page_not_found))
                } else {
                    // Ha acabat de carregar pàgina però també
                    // hauriem de comprovar si és per timeout es talla i acaba
                    // Ha acabat de carregar la pàgina
                    if (!isTimeout) {
                        // Només mostra el missatge si no és a causa del timeout
                        createToast(getString(R.string.page_loaded_successfully))
                    }
                }
                // Reinicia la variable de control
                isTimeout = false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                // S'ha produït un error de càrrega, pots gestionar-ho aquí
                handleWebViewError(error)
            }
        }
    }

    private fun handleWebViewError(error: WebResourceError) {
        if (error.errorCode == ERROR_HOST_LOOKUP || error.errorCode == ERROR_CONNECT
            || error.errorCode == ERROR_TIMEOUT || error.errorCode == ERROR_IO
        ) {
            // Error de connexió, mostra una notificació o realitza altres accions necessàries
            createToast(getString(R.string.connection_error))
        } else {
            // Altres errors, pots gestionar-los segons les teves necessitats
            createToast(getString(R.string.webview_error))
        }
    }

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    private fun getWeb(url: String) {
        // Verifica la connexió abans de carregar la pàgina
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
            // Configura un temps d'espera de 15 segons per timeout de càrrega
            handler.postDelayed({
                // Verifica si la pàgina encara està carregant després de 15 segons
                if (progressBar.visibility == View.VISIBLE) {
                    // Si encara està carregant, mostra un missatge d'error
                    // Si encara està carregant, mostra un missatge d'error
                    isTimeout = true
                    createToast(getString(R.string.page_load_timeout))
                    // Atura la càrrega de la pàgina
                    webView.stopLoading()
                    // Restaura la interacció de l'usuari
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    // Amaga la barra de progrés
                    progressBar.visibility = View.GONE
                }
            }, 15000) // 15,000 mil·lisegons = 15 segons
        } else {
            webView.loadUrl("about:blank")
            // Unknow error
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
