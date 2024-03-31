package com.example.project


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }


}














    /*
    private val authViewModel: AuthViewModel by viewModels()
    private val clientViewModel: ClientViewModel by viewModels()

    var deviceInfo: DeviceInfo= DeviceInfo("","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(){
            val navController = rememberNavController()
            NavHost(navController, startDestination = "mainActPlaceHolder") {
                composable("mainActPlaceHolder") { Gather(authViewModel,navController) }
                composable("collectionInfos") { GetLayout(clientViewModel,navController) }
                composable("otpHandler"){ OtpComponent(context = this@MainActivity, PERMISSION_REQUEST_SEND_SMS = 123,navController)}
                composable("checkFingerPrint"){ FingerPrint(unit = showBiometricPrompt(
                    this@MainActivity, // Pass the context
                    CheckFingerPrint.MainThreadExecutor(), // Pass the executor
                    CheckFingerPrint().getBiometricCallBack() // Pass the callback
                ),clientViewModel)}
            }
            deviceInfo=DeviceInfo.fromContext(this)
            Log.d("DeviceInfo", deviceInfo.toString())
            /*Layout(authViewModel)
            BouttonRedirection(navigationViewModel = navigationViewModel)*/
        }}
    fun getDeviceInf(): DeviceInfo {
        return deviceInfo
    }
}

*/
        /*
        navigationViewModel.navigateToDestination.observe(this) { navigate ->
            if (navigate) {
                navigateToYourDestination()
                navigationViewModel.onNavigationHandled()
            }
        }
        }

    fun navigateToYourDestination() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment,CollectionInfosFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }



}




@Composable
fun NavigationBarItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(modifier=Modifier.padding(),verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(3.dp),
            tint = if (selected) Blue else Color.Gray,
        )
        Text(text = label, modifier = Modifier.padding(1.dp), onTextLayout = { result: TextLayoutResult ->
            // Handle text layout result
        })

    }
}

@Composable
fun NavigationBar() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Agences", "Démo")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomAppBar(modifier = Modifier.align(Alignment.BottomCenter)) {
            Row(modifier=Modifier.width(40.dp)){}
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center){
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = getIconForIndex(index),
                        label = item,
                        selected = selectedItem == index
                    ) { selectedItem = index }
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }}
    }
}



fun getIconForIndex(index: Int): ImageVector {
    return when (index) {
        0 -> Icons.Filled.Home
        1 ->Icons.Filled.LocationOn
        2->Icons.Filled.Menu
        else -> Icons.Filled.Home // Default to Home icon
    }
}

@Composable
fun back(){
    Scaffold (
        topBar = {/*thh*/},
        bottomBar = { NavigationBar() },
        content = { padding ->padding}

    )

}

@Composable
fun PreviewNavigationBar() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar()
    }
}



@Composable

fun ElevatedCardExample(authViewModel: AuthViewModel) {

    var launchEffect by remember { mutableStateOf(false) }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        colors =CardDefaults.elevatedCardColors(containerColor = White),
        modifier = Modifier
            .size(width = 350.dp, height = 400.dp)
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        Box( modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center){

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", onTextLayout = { result: TextLayoutResult ->
                        // Handle text layout result
                    }) },
                    placeholder = { Text("example@gmail.com", onTextLayout = { result: TextLayoutResult ->
                        // Handle text layout result
                    }) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Localized description"
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Localized description"
                        )
                    }

                )}

                Spacer(Modifier.height(30.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center){
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de Passe", onTextLayout = { result: TextLayoutResult ->
                        // Handle text layout result
                    }) },
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Mot de Passe", onTextLayout = { result: TextLayoutResult ->
                        // Handle text layout result
                    }) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Localized description"
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Localized description"
                        )
                    }

                )}
                Spacer(Modifier.height(30.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center){

                    ElevatedButtonExample("Connexion") {
                        launchEffect=true
                    }
                    if(launchEffect){
                        LaunchedEffect(Unit) {
                            Log.d("Coroutine", "I entered coroutine")

                            authViewModel.signIn(email,password)
                            Log.e("Email",email)
                            Log.e("Password", password)
                            Log.e("Corounrine","Inside coroutine")
                        }

                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center){
                    TextButton(onClick = { /* Do something! */ }) { Text("Identifiant ou mot de passe oublié?", onTextLayout = { result: TextLayoutResult ->
                        // Handle text layout result
                    }) }
                }



        }
        }



        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalArrangement = Arrangement.Center

    ) {


        }

    }





@Composable
fun ElevatedButtonExample(text:String, onClick: () -> Unit) {
    ElevatedButton(onClick = { onClick() }) {
        Text(text, onTextLayout = { result: TextLayoutResult ->
            // Handle text layout result
        })
    }
}




@Composable
fun ConnectToFireBase(authViewModel:AuthViewModel){

    val authState by authViewModel.authState.collectAsState()
    when (authState)
    {
        AuthState.Initial -> {
            ElevatedCardExample(authViewModel)
            Log.d("Auth", "État d'authentification initial")

        }
        AuthState.Loading -> {
            CircularProgressIndicator()
            Log.d("Auth", "Chargement de l'état d'authentification")
        }
        AuthState.Success -> {
            Log.d("Auth", "Chargement de l'état d'authentification")
        }
        is AuthState.Error -> {
            val errorMessage = (authState as AuthState.Error).message
            Text(errorMessage)
            Log.e("Auth", "Échec de l'authentification de l'utilisateur: $errorMessage")
        }
    }

}


@Composable
fun Layout(authViewModel:AuthViewModel){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

    ){
        Spacer(modifier = Modifier.height(200.dp))

        ElevatedCardExample(authViewModel)

        ConnectToFireBase(authViewModel)
        back()
    }
}


//Navigation
@Composable
fun BouttonRedirection(navigationViewModel: NavigationViewModel){
    val navigateToDestination by navigationViewModel.navigateToDestination.observeAsState()

    if (navigateToDestination == true) {
        LaunchedEffect(Unit) {
            // Call navigateToYourDestination() in MainActivity here
            navigationViewModel.onNavigationHandled()
        }
    }

    ElevatedButtonExample(text = "Ouvrir un compte") {
        navigationViewModel.navigate()
    }
}

*/
