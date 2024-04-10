package com.example.project

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.MainActpalceholderBinding
import com.example.project.models.AuthState
import com.example.project.viewmodels.AuthViewModel
import com.example.project.viewmodels.BiometricViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator


@AndroidEntryPoint
class MainActPlaceHolder : Fragment() {
    private val authViewModel: AuthViewModel by viewModels()
    private val fingerPrintViewModel: BiometricViewModel by viewModels()
    private val mainThreadExecutor: MainThreadExecutor = MainThreadExecutor()
    private val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

    // Generate a key alias
    val keyAlias = "my_key_alias"

    // Generate a symmetric key using KeyGenerator
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val keySpec = KeyGenParameterSpec.Builder(
        keyAlias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setRandomizedEncryptionRequired(false) // Required for biometric authentication
        .build()

    // Create a BiometricPrompt.CryptoObject with the Cipher
    val cryptoObject = BiometricPrompt.CryptoObject(cipher)

    //private var authState= AuthState.Initial
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.navigateToCollectInfos.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(com.example.project.R.id.mainactplace_to_collectinfos)

                    // Reset the navigation event after navigation
                    authViewModel.onNavigationComplete()
                }
            })


        authViewModel.navigateToDashboard.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(com.example.project.R.id.mainactplace_to_dashboard)

                    // Reset the navigation event after navigation
                    authViewModel.onNavigationCompleteDash()
                }
            })

        fingerPrintViewModel.navigateToDashboard.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(com.example.project.R.id.mainactplace_to_dashboard)

                    // Reset the navigation event after navigation
                    fingerPrintViewModel.onNavigationCompleteDash()
                }
            })



        fingerPrintViewModel.showBiometricPrompt.observe(viewLifecycleOwner) { showBiometric ->
            if (showBiometric) {
                showBiometricPrompt()
                fingerPrintViewModel.onBiometricPromptShown()
            }
        }

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()

        // Retrieve the secret key from the AndroidKeyStore
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)


    }




    private fun showBiometricPrompt() {
        val biometricPrompt = BiometricPrompt(
            this,
            mainThreadExecutor,
            fingerPrintViewModel.getBiometricCallback()
        )
        biometricPrompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate with your fingerprint")
                .setSubtitle("Place your finger on the fingerprint sensor")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build(),
            cryptoObject // Pass the CryptoObject to authenticate
        )

    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainActpalceholderBinding.inflate(inflater, container, false)
        binding.viewModel = authViewModel
        binding.clientPartial = authViewModel.clientPartial
        binding.fingerPrintViewModel=fingerPrintViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        connectToFirebase(authViewModel)


        fingerPrintViewModel.biometricEvent.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is BiometricViewModel.BiometricEvent.Success -> {
                    // Handle successful authentication
                    Log.d("Biometric", "Authentication succeeded")

                }
                is BiometricViewModel.BiometricEvent.Error -> {
                    // Handle authentication error
                    Log.e("Biometric", "Authentication error: ${event.errorMessage}")
                }
                is BiometricViewModel.BiometricEvent.Failure -> {
                    // Handle authentication failure
                    Log.e("Biometric", "Authentication failed: ${event.errorMessage}")
                }
            }
        })

        return binding.root

    }


    fun connectToFirebase(authViewModel: AuthViewModel) {
        CoroutineScope(Dispatchers.Main).launch {
            authViewModel.authState.collect { authState ->
                when (authState) {
                    AuthState.Initial -> {
                        Log.d("Auth", "État d'authentification initial")
                    }
                    AuthState.Loading -> {
                        // Show loading indicator
                        Log.d("Auth", "Chargement de l'état d'authentification")
                    }
                    AuthState.Success -> {
                        // Handle success state
                        Log.d("Auth", "Chargement de l'état d'authentification")
                    }
                    is AuthState.Error -> {
                        // Handle error state
                        val errorMessage = authState.message
                        Log.e("Auth", "Échec de l'authentification de l'utilisateur: $errorMessage")
                    }
                    else -> {
                        // Handle other states
                    }
                }
            }
        }
    }



}

  /*
    fun ConnectToFireBase(authViewModel:AuthViewModel) {

        val authState = authViewModel.authState
            .onEach { state ->
                // Handle the state change here
                // For example, update UI based on the state
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        when (authState) {
            AuthState.Initial -> {
                Log.d("Auth", "État d'authentification initial")

            }

            AuthState.Loading -> {

                Log.d("Auth", "Chargement de l'état d'authentification")
            }

            AuthState.Success -> {
                Log.d("Auth", "Chargement de l'état d'authentification")
            }

            is Error -> {
                val errorMessage = (authState as AuthState.Error).message
                //Text(errorMessage)
                Log.e("Auth", "Échec de l'authentification de l'utilisateur: $errorMessage")
            }

            else -> {}
        }

    }
}

     */





/*

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

        else -> {}
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
fun BouttonRedirection(navController: NavController?=null){

    ElevatedButtonExample(text = "Ouvrir un compte") {
        navController?.navigate("collectionInfos")
    }
}


@Composable
fun Gather(authViewModel: AuthViewModel, navController: NavController?){

    Layout(authViewModel)


    BouttonRedirection(navController)
}

*/
