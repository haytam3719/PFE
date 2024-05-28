package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.ActivityCollectionInfosBinding
import com.example.project.viewmodels.CollectInfoViewModel
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionInfosFragment() : Fragment() {
    private val collectInfos: CollectInfoViewModel by viewModels({ requireActivity() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectInfos.navigateToOtp.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navigate when event is triggered
                findNavController().navigate(R.id.collectinfos_to_otp)

                // Reset the navigation event after navigation
                collectInfos.onNavigationComplete()
            }
        })




    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivityCollectionInfosBinding.inflate(inflater, container, false)
        binding.viewModel = collectInfos
        binding.clientPartial = collectInfos.clientPartial
        binding.lifecycleOwner = viewLifecycleOwner

        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        return binding.root
    }




}























/*
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                //GetLayout(clientViewModel, navController )
            }
        }
    }
}





/*

@Composable
fun TextFieldd(label:String, modifier:Modifier, textInput:String, onTextChanged:(String)->Unit){
    var text by rememberSaveable { mutableStateOf(textInput) }
    OutlinedTextField(
        value = text,
        onValueChange = { text=it
                        onTextChanged(it)},
        label = { Text(label) }
    )


}

@Composable
fun TextFieldWithModifier(
    label: String,
    modifier: Modifier = Modifier,
    textInput: String,
    onTextChanged: (String) -> Unit
) {
    Box(modifier =  modifier) {
        TextFieldd(label = label,modifier,textInput,onTextChanged)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePick(openDialog: Boolean, onClose: () -> Unit) {
    // Decoupled snackbar host state from scaffold state for demo purposes.
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()
    SnackbarHost(hostState = snackState, Modifier)

    if (openDialog) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                onClose() // Close the dialog when dismissed
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClose() // Close the dialog when confirmed
                        snackScope.launch {
                            snackState.showSnackbar(
                                "Selected date timestamp: ${datePickerState.selectedDateMillis}"
                            )
                        }
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onClose() // Close the dialog when dismissed
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}




@Composable
fun GetLayout(clientViewModel: ClientViewModel,navController: NavController){
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var date_naissance by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var cin by remember { mutableStateOf("") }
    var domicile by remember { mutableStateOf("") }
    var num_tel by remember { mutableStateOf("") }
    var launchEffect by remember { mutableStateOf(false) }


    CenterAlignedTopAppBarExample(text = " Collection des infos personnelles")
    // State to track whether the date picker dialog should be open
    val (openDialog, setOpenDialog) = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text("Veuillez remplir soigneusement les champs suivants", Modifier.padding(150.dp))
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextFieldWithModifier(label = "Prénom", Modifier.width(170.dp),prenom){
                newText->prenom=newText
            }
            Spacer(Modifier.width(25.dp))
            TextFieldWithModifier(label = "Nom", Modifier.width(170.dp),nom){
                    newText->nom=newText
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextFieldWithModifier(label = "Date de Naissance",
                Modifier
                    .width(170.dp)
                    .clickable { setOpenDialog(true) },
                date_naissance){
                    newText->date_naissance=newText
            }// Show the date picker when clicked

            Spacer(Modifier.width(25.dp))
            TextFieldWithModifier(label = "Adresse", Modifier.width(170.dp),adresse){
                    newText->adresse=newText
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextFieldWithModifier(label = "N° CIN", Modifier.width(170.dp),cin){
                    newText->cin=newText
            }
            Spacer(Modifier.width(25.dp))
            TextFieldWithModifier(label = "Domicile", Modifier.width(170.dp),domicile){
                    newText->domicile=newText
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextFieldWithModifier(label = "Numéro de Tél.", Modifier.width(170.dp),num_tel){
                    newText->num_tel=newText
            }}
        }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Button(
            onClick = { launchEffect=true
                    },
            modifier = Modifier.padding(50.dp)
        ) {
            Text("Continuer")

        }
        val deviceInfo= DeviceInfo
        if(launchEffect){
            LaunchedEffect(Unit){
                Log.e("Coroutine","Inside coroutine")
                val client: Client = Client(null,nom, prenom,date_naissance, adresse, cin, domicile, num_tel,MainActivity().getDeviceInf(),null)
                Log.e("Client Info", "{$nom}, {$prenom},{$date_naissance},{$adresse},{$cin},{$num_tel},")
                clientViewModel.updateClient(client)
                navController.navigate("otpHandler")
            }
        }
        DatePick(openDialog = (openDialog)) {

        }
    }
    }

*/
*/
