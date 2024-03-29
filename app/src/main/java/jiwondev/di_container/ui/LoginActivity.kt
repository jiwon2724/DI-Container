package jiwondev.di_container.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import jiwondev.di_container.R
import jiwondev.di_container.application.App
import jiwondev.di_container.container.AppContainer
import jiwondev.di_container.container.LoginContainer
import jiwondev.di_container.extension.showToast
import jiwondev.di_container.viewmodel.LoginViewModel
import jiwondev.di_container.viewmodel.UserState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val appContainer: AppContainer by lazy { (this.application as App).appContainer }
    private val viewModel: LoginViewModel by viewModels { appContainer.loginContainer?.createLoginViewModelFactory() ?: error("not initialize") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer.loginContainer = LoginContainer(appContainer)
        setContentView(R.layout.activity_login)
        initView()
        handleLoginUiState()
    }

    private fun handleLoginUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it.userState) {
                        UserState.NONE -> Unit
                        UserState.LOGGED_IN -> showToast("로그인 성공")
                        UserState.FAILED -> showToast("로그인 실패")
                    }
                }
            }
        }
    }

    private fun initView() {
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            viewModel.login()
        }

        findViewById<Button>(R.id.btn_logout).setOnClickListener {
            viewModel.logout()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 생명주기에 따라 의존성 관리
        appContainer.loginContainer = null
    }
}