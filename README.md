# Container로 의존성 관리, 주입하는 방법

안드로이드에서 DI 라이브러리(Hilt) 도움 없이 수동으로 Container를 생성해 수동 의존성 주입을 공부해보자.

## Todo

- [ ]  Container를 만들어서 의존성을 관리하고 주입하기
- [ ]  생명주기에 따라 의존성을 관리하기

## 시나리오

- 자동 로그인을 저장하는 프로세스가 있다고 가정한다.
- LoginActivity, MainActivity
    - LoginActivity에서 로그인 성공 시 MainActivity로 이동한다.
    - 입력한 아이디와 미리 지정 해놓은 문자열이 일치한다면 로그인 성공
        - 로그인 성공 시 `isLogin` Boolean 값은 true로 `DataStore` 에 저장된다.
    - 로그아웃 버튼 클릭 시 `DataStore` 에 저장된 `isLogin` 은 false가 된다.

## 아키텍처

- 앱 아키텍처는 다음과 같다.
- 화살표는 의존방향임.
    
    ![아키텍처.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/17b7261d-8574-4fae-97a1-5f7367a227bc/f430459d-5180-457d-a40e-640013e43494/%E1%84%8B%E1%85%A1%E1%84%8F%E1%85%B5%E1%84%90%E1%85%A6%E1%86%A8%E1%84%8E%E1%85%A5.png)
    

## LoginActivty

```kotlin
class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels { ... }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
```

- LoginActivity에서는 LoginViewModel만 의존성을 가지고 있음.
- 따라서 UserDataRepositoy, UserLocalDataSource, DataStore에 대한 의존성은 LoginActivity에 있으면 안됨.
- 각 의존성들을 Container를 통해서 관리하면 다음과 같음.

## AppContainer

```kotlin
class AppContainer(private val context: Context) {
    fun createLocalDataSource(): UserLocalDataSource = UserLocalDataSource(context)
    fun createLocalDataRepository(): UserDataRepository = UserDataRepository(createLocalDataSource())
    fun createLoginViewModelFactory(): AbstractSavedStateViewModelFactory {
        return object : AbstractSavedStateViewModelFactory() {
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return LoginViewModel(createLocalDataRepository()) as T
            }
        }
    }
}
```

- 컨테이너에서 각각 의존성을 만들고 있음.
- 앱 아키텍처에 따라서 UserDataRepository는 UserLocalDataSouce에 의존하는 모습이 보임.

## AppContainer의 문제점

- 위 처럼 하나의 컨테이너에 모든 의존성을 관리하게 되면 문제가 발생함.
- LoginActivity에 AppContainer 객체를 만들게되면 불필요한 의존성 까지 노출됨.
    - `createLocalDataSource`, `createLocalDataRepository`

## LoginContainer

- 위 문제점을 해결하기 위해 로그인에 필요한 의존성만 따로 컨테이너를 만들면됨.

```kotlin
class AppContainer(private val context: Context) {
    fun createLocalDataSource(): UserLocalDataSource = UserLocalDataSource(context)
    var loginContainer: LoginContainer? = null
}

class LoginContainer(private val appContainer: AppContainer) {
    fun createLocalDataRepository(): UserDataRepository = UserDataRepository(appContainer.createLocalDataSource())

    fun createLoginViewModelFactory(): AbstractSavedStateViewModelFactory {
        return object : AbstractSavedStateViewModelFactory() {
            val localDataRepository = createLocalDataRepository()
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return LoginViewModel(localDataRepository) as T
            }
        }
    }
}
```

## 컨테이너에서 의존성 관리

```kotlin
fun createLoginViewModelFactory(): AbstractSavedStateViewModelFactory {
        return object : AbstractSavedStateViewModelFactory() {
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return LoginViewModel(createLocalDataRepository()) as T
            }
        }
    }
```

- 하나의 유저 데이터를 공유하고 싶다면 createLocalDataRepository()를 객체로 만들어서 LoginViewModel생성자에 넣어주면됨.
- 즉, LoginViewModel은 계속 만들어지더라도, repository는 공유할 수 있음.

## 생명주기에 따라 의존성 관리하기

```kotlin
class App : Application() {
    val appContainer: AppContainer = AppContainer(this)
}

class LoginActivity : AppCompatActivity() {
    private val appContainer: AppContainer by lazy { (this.application as App).appContainer }
    private val viewModel: LoginViewModel by viewModels { appContainer.loginContainer?.createLoginViewModelFactory() ?: error("not initialize") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer.loginContainer = LoginContainer(appContainer)
        setContentView(R.layout.activity_login)
        initView()
        handleLoginUiState()
				...
				...

		override fun onDestroy() {
        super.onDestroy()
        // 생명주기에 따라 의존성 관리
        appContainer.loginContainer = null
    }
}
```

- Application Class가 AppContainer를 관리하게됨.
    - 안드로이드 컴포넌트(Activity, Fragment 등)들이 손쉽게 접근하기 위해서임.

## 프로젝트 Repository

https://github.com/jiwon2724/DI-Container

## Done!

- [x]  Container를 만들어서 의존성을 관리하고 주입하기
- [x]  생명주기에 따라 의존성을 관리하기
