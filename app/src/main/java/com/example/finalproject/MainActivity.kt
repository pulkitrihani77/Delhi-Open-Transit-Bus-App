@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class
)

package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finalproject.database.Combined
import com.example.finalproject.database.DatabaseContract
import com.example.finalproject.database.DatabaseHelper
import com.example.finalproject.ui.theme.FinalProjectTheme
import kotlinx.coroutines.delay
import java.util.PriorityQueue

val customFontFamily = FontFamily(Font(R.font.kanit))

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val dbHelper = DatabaseHelper(this)
                    val buses = dbHelper.getBusStops()
                    val stopCoordinates = dbHelper.getStopCoordinates()
                    val busStops: HashMap<String, ArrayList<String>> = hashMapOf()
                    for ((entityId, routeMap) in buses) {
                        for ((_, stopsList) in routeMap) {
                            for (stop in stopsList) {
                                if (busStops.containsKey(entityId)) {
                                    busStops[entityId]?.add(stop)
                                } else {
                                    busStops[entityId] = arrayListOf(stop)
                                }
                            }
                        }
                    }
                    val stopsList = getStopsData(dbHelper)
                    val stopsNames = getStops(dbHelper)
                    val navController = rememberNavController()
                    MyApp(navController, stopsList = stopsList, buses = buses, stopsNames = stopsNames, busStops = busStops, stopCoordinates = stopCoordinates)
                }

            }
        }
    }
}

@Composable
fun MyApp(navController: NavHostController, stopsList: List<Combined>, buses: HashMap<String, HashMap<String, ArrayList<String>>>, stopsNames: List<String>, busStops: HashMap<String, ArrayList<String>>, stopCoordinates: HashMap<String, Pair<Double, Double>>) {
    NavHost(navController, startDestination = "loading") {
        composable("loading") {
            LoadingScreen(navController = navController)
        }
        composable("home") {
            HomePage(navController = navController)
        }
        composable("travelling") {
            Travelling(navController = navController)
        }
        composable("stopsList") {
            StopsList(stopsList = stopsList)
        }
        composable("busList") {
            BusesList(navController = navController, buses = buses)
        }
        composable("search") {
            Search(navController = navController, buses = buses, stopsNames = stopsNames)
        }
        composable("sensor") {
            BusTravelInputScreen(navController = navController, busStops = busStops, stopCoordinates = stopCoordinates)
        }
        composable(
            route = "matchingBusesScreen/{source}/{destination}/{matchingBuses}",
            arguments = listOf(
                navArgument("source") { type = NavType.StringType },
                navArgument("destination") { type = NavType.StringType },
                navArgument("matchingBuses") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: ""
            val destination = backStackEntry.arguments?.getString("destination") ?: ""
            val matchingBusesString = backStackEntry.arguments?.getString("matchingBuses")
            val matchingBusesList = matchingBusesString?.split(",") ?: emptyList()

            MatchingBusesScreen(navController, stopsList, matchingBusesList, source, destination, buses)
        }
        composable(
            route = "stopsScreen/{source}/{destination}/{stops}",
            arguments = listOf(
                navArgument("source") { type = NavType.StringType },
                navArgument("destination") { type = NavType.StringType },
                navArgument("stops") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stopsString = backStackEntry.arguments?.getString("stops")
            val stopsList = stopsString?.split(",") ?: emptyList()

            MatchingStopsScreen(navController, stopsList)
        }
        composable("nearestStopScreen/{nearestStop}") { backStackEntry ->
            val nearestStop = backStackEntry.arguments?.getString("nearestStop")
            nearestStop?.let {
                NearestStopScreen(navController = navController, nearestStop = it)
            }
        }
    }
}

@Composable
fun LoadingScreen(navController: NavHostController) {
    val isLoadingComplete = remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(2000)
        scale.animateTo(1.5f, animationSpec = tween(durationMillis = 1000))
        scale.animateTo(0f, animationSpec = tween(durationMillis = 1000))
        delay(1000)
        isLoadingComplete.value = true
    }

    if (isLoadingComplete.value) {
        navController.navigate("home")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.bus_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun HomePage(navController: NavController) {
    Surface(color = Color.White) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.loading_bg),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
                    .scale(scaleX = 1f, scaleY = 1f),
                contentScale = ContentScale.FillBounds)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Find  your\nBus route",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFamily = customFontFamily)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate("travelling")
                    },
                    modifier = Modifier
                        .width(65.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE83A30),
                        contentColor = Color.White
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Navigate to Bus Search",
                        tint = Color.White
                    )
                }

            }
        }
    }
}

@Composable
fun Travelling(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.loading_bg),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 200.dp),
            contentScale = ContentScale.FillBounds)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Are you currently\ntravelling?",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFamily = customFontFamily),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { navController.navigate("sensor") },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE83A30),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Yes")
                }
                Button(
                    onClick = { navController.navigate("search") },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE83A30),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "No")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(navController: NavHostController, buses: HashMap<String, HashMap<String, ArrayList<String>>>, stopsNames: List<String>) {
    var source by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var matchingBuses by remember { mutableStateOf<List<String>>(emptyList()) }
    var showMessage by remember { mutableStateOf(false) }
    if (showMessage) {
        Snackbar(
            action = {
                Button(onClick = { showMessage = false }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text("Incorrect Input")
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Row(modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, top = 16.dp)
            .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    navController.navigate("busList")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ){
                Icon(imageVector = Icons.Filled.Menu,
                    contentDescription = "menu",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    navController.navigate("travelling")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "home",
                    tint = Color(0xFFE83A30)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Spacer(modifier = Modifier.height(36.dp))
            Column(modifier = Modifier.padding(start = 24.dp)) {
                Text(
                    text = "Go easily\nwhere you want\nto",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFamily = customFontFamily),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFE83A30))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row (modifier = Modifier.height(26.dp)){
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Location icon",
                                    tint = Color.White
                                )
                                Text(
                                    text = "From",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            TextField(
                                value = source,
                                onValueChange = { source = it },
                                label = { Text("Select Location ") },
                                modifier = Modifier
                                    .height(50.dp)
                                    .padding(start = 24.dp),
                                textStyle = TextStyle(color = Color.White),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(24.dp))
                        Column {
                            Text(
                                text = "--------------------------------------------------------",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE6D66))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = "swap",
                                tint = Color.Black,
                                modifier = Modifier.aspectRatio(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row (modifier = Modifier.height(26.dp)){
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Location icon",
                                    tint = Color.White
                                )
                                Text(
                                    text = "To",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            TextField(
                                value = destination,
                                onValueChange = { destination = it },
                                label = { Text("Select Destination ") },
                                modifier = Modifier
                                    .height(50.dp)
                                    .padding(start = 24.dp),
                                textStyle = TextStyle(color = Color.White),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_bg),
                    contentDescription = "map screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(64.dp))
                )
                Button(
                    onClick = {
                        matchingBuses = findBusesWithStops(source = source, destination = destination, buses = buses)
                        if(matchingBuses.isEmpty()){
                            matchingBuses = findBusesWithStops2(source = source, destination = destination, buses = buses)
                            if(matchingBuses.isEmpty()){
                                showMessage = true
                            }
                            else{
                                navController.navigate("matchingBusesScreen/${source}/${destination}/${matchingBuses.joinToString(",")}")
                            }
                        }
                        //DL1PD2501 Shakarpur - Gazipur Depot, DL1PC9343 Gazipur Depot - ISBT Anand Vihar
                        // Shakarpur-Shakarpur Crossing-Nirman-Sawarthya-New Rajdhani-KarkarDooma-HassanpurDepot-HassanpurVillage-Gazipur-ISBT
                        // DL1PC9140 DL1PC8072 DL1PC7760 DL1PC9145 DL1PC7135
                        else{
                            showMessage = false
                            navController.navigate("matchingBusesScreen/${source}/${destination}/${matchingBuses.joinToString(",")}")
                        }
                              },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(height = 50.dp, width = 120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE83A30),
                        contentColor = Color.White
                    )
                ) {
                    Text("Search")
                }
            }
        }
        if (showMessage) {
            Snackbar(
                action = {
                    Button(onClick = { showMessage = false }) {
                        Text("Dismiss")
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Incorrect Bus Stops")
            }
        }
    }
}


@Composable
fun BusesList(navController: NavController, buses: HashMap<String, HashMap<String, ArrayList<String>>>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("busList")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "busesInfo",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        navController.navigate("travelling")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "home",
                        tint = Color(0xFFE83A30)
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Bus Stops",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    val busList = buses.entries.toList()
                    items(busList) { busEntry ->
                        val busNumber = busEntry.key
                        val routeStopsMap = busEntry.value
                        BusStopGroup(busNumber, routeStopsMap)
                    }
                }
            }
        }
    }
}


@Composable
fun BusStopGroup(busNumber: String, routeStopsMap: HashMap<String, ArrayList<String>>) {
    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp)) {
        Text(
            text = "Bus No.: $busNumber",
            style = MaterialTheme.typography.headlineSmall
        )

        routeStopsMap.forEach { (routeName, stops) ->
            val busStopsString = StringBuilder()
            stops.forEach { stop ->
                busStopsString.append(stop)
                busStopsString.append(", ")
            }
            Text(
                text = "Route: $routeName\nStops:",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(text = "\n{$busStopsString}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BusTravelInputScreen(navController: NavHostController, busStops: HashMap<String, ArrayList<String>>, stopCoordinates: HashMap<String, Pair<Double, Double>>) {
    var busRegistration by remember { mutableStateOf("") }
    var destinationStop by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var showMessage by remember { mutableStateOf(false) }

    if (showMessage) {
        Snackbar(
            action = {
                Button(onClick = { showMessage = false }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text("Incorrect Input")
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("busList")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "menu",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        navController.navigate("travelling")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "home",
                        tint = Color(0xFFE83A30)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                TextField(
                    value = busRegistration,
                    onValueChange = { busRegistration = it },
                    label = { Text("Bus Number you're travelling in") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = destinationStop,
                    onValueChange = { destinationStop = it },
                    label = { Text("Your Destination Stop") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val nearestStop = findNearestStop(
                        busStops,
                        stopCoordinates,
                        busRegistration,
                        latitude,
                        longitude
                    )
                    if(nearestStop.isNullOrEmpty()){
                        showMessage = true
                    }
                    else{
                        showMessage = false
                        navController.navigate("nearestStopScreen/$nearestStop")
                    }
                                 },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE83A30),
                        contentColor = Color.White
                    )) {
                    Text(text = "Search Bus", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (showMessage) {
            Snackbar(
                action = {
                    Button(onClick = { showMessage = false }) {
                        Text("Dismiss")
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Incorrect Details")
            }
        }
    }
}

fun findNearestStop(
    busStops: HashMap<String, ArrayList<String>>,
    stopCoordinates: HashMap<String, Pair<Double, Double>>,
    busNumber: String,
    latitude: Double,
    longitude: Double
): String? {
    val stopsForBus = busStops[busNumber]
    stopsForBus?.let { stops ->
        var nearestStop: String? = null
        var minDistance = Double.MAX_VALUE

        for (stop in stops) {
            val stopCoordinatesPair = stopCoordinates[stop]
            stopCoordinatesPair?.let { pair ->
                val stopLat = pair.first
                val stopLon = pair.second

                val distance = calculateDistance(latitude, longitude, stopLat, stopLon)
                if (distance < minDistance) {
                    minDistance = distance
                    nearestStop = stop
                }
            }
        }
        return nearestStop
    }
    return null
}

private fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val radius = 6371
    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)
    val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return radius * c
}

@Composable
fun NearestStopScreen(navController: NavController, nearestStop: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("busList")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "menu",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        navController.navigate("travelling")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "home",
                        tint = Color(0xFFE83A30)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Nearest Stop: $nearestStop")
            }
        }
    }
}


@Composable
fun StopsList(stopsList: List<Combined>) {
    Column {
        Text(
            text = "Database",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(stopsList) { stop ->
                StopRow(stop)
            }
        }
    }
}

@Composable
fun StopRow(stop: Combined) {
    Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp)){
        Text("Bus Registration No.: ${stop.entityId}")
        Text("Route: ${stop.routeName}")
        Text("Stop Name: ${stop.stopName}")
        Text("Stop Number: ${stop.stopSequence}")
        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun getStopsData(dbHelper: DatabaseHelper): List<Combined> {
    val db = dbHelper.readableDatabase
    val stopsList = mutableListOf<Combined>()
//    val limit = 500
    val cursor = db.query(
        DatabaseContract.CombinedEntry.TABLE_NAME,
        null,
        null,
        null,
        null,
        null,
        null,
//        "$limit"
    )
    with(cursor) {
        while (moveToNext()) {
            val registrationNumber = getString(getColumnIndexOrThrow(DatabaseContract.CombinedEntry.COLUMN_ENTITY_ID))
            val routeName = getString(getColumnIndexOrThrow(DatabaseContract.CombinedEntry.COLUMN_ROUTE_LONG_NAME))
            val stopName = getString(getColumnIndexOrThrow(DatabaseContract.CombinedEntry.COLUMN_STOP_NAME))
            val stopSequence = getInt(getColumnIndexOrThrow(DatabaseContract.CombinedEntry.COLUMN_STOP_SEQUENCE))
            stopsList.add(Combined(registrationNumber, routeName, stopName, stopSequence))
        }
    }
    cursor.close()
    return stopsList
}

fun getStops(dbHelper: DatabaseHelper): List<String> {
    val db = dbHelper.readableDatabase
    val stopsList = mutableListOf<String>()
    val cursor = db.query(
        DatabaseContract.CombinedEntry.TABLE_NAME,
        null,
        null,
        null,
        null,
        null,
        null,
//        "$limit"
    )
    with(cursor) {
        while (moveToNext()) {
            val stopName = getString(getColumnIndexOrThrow(DatabaseContract.CombinedEntry.COLUMN_STOP_NAME))
            stopsList.add(stopName)
        }
    }
    cursor.close()
    return stopsList
}

fun findBusesWithStops(source: String, destination: String, buses: HashMap<String, HashMap<String, ArrayList<String>>>): List<String> {
    val matchingBuses = mutableListOf<String>()
    for ((busNumber, routeStopsMap) in buses) {
        if (routeStopsMap.any { (_, stops) ->
                stops.contains(source) && stops.contains(destination)
            }) {
            matchingBuses.add(busNumber)
        }
    }
    return matchingBuses
}

fun findBusesWithStops2(source: String, destination: String, buses: HashMap<String, HashMap<String, ArrayList<String>>>): List<String> {
    val graph = buildGraph(buses)
    val path = dijkstra(graph, source, destination)
    return path ?: emptyList()
}

fun buildGraph(buses: HashMap<String, HashMap<String, ArrayList<String>>>): Map<String, Map<String, List<String>>> {
    val graph = mutableMapOf<String, MutableMap<String, List<String>>>()
    for ((busNumber, routeStopsMap) in buses) {
        for ((routeName, stops) in routeStopsMap) {
            for (i in 0 until stops.size - 1) {
                val start = stops[i]
                val end = stops[i + 1]
                graph.getOrPut(start) { mutableMapOf() }[end] = listOf(busNumber, routeName)
            }
        }
    }
    return graph
}

fun dijkstra(graph: Map<String, Map<String, List<String>>>, source: String, destination: String): List<String>? {
    val distances = mutableMapOf<String, Int>()
    val prev = mutableMapOf<String, Pair<String, List<String>>>()
    val queue = PriorityQueue<Pair<String, List<String>>>(compareBy { it.second.size })

    distances[source] = 0
    queue.add(source to emptyList())

    while (queue.isNotEmpty()) {
        val (current, path) = queue.poll()
        if (current == destination) {
            return path
        }
        val currentDist = distances[current] ?: continue
        for ((neighbor, busInfo) in graph[current] ?: emptyMap()) {
            val newDist = currentDist + 1
            if (distances[neighbor] == null || newDist < distances[neighbor]!!) {
                distances[neighbor] = newDist
                prev[neighbor] = current to busInfo
                queue.add(neighbor to (path + busInfo))
            }
        }
    }
    return null
}


@Composable
fun MatchingBusesScreen(navController: NavController, stopsList: List<Combined>, matchingBuses: List<String>, source: String,
                        destination: String, buses: HashMap<String, HashMap<String, ArrayList<String>>>) {

    val calculateStops: (String, String) -> List<String> = { entityId, routeName ->
        val busInfo = buses[entityId]
        if (busInfo != null) {
            val routeInfo = busInfo[routeName]
            if (routeInfo != null) {
                val sourceIndex = routeInfo.indexOf(source)
                val destinationIndex = routeInfo.indexOf(destination)
                if (sourceIndex != -1 && destinationIndex != -1) {
                    try {
                        val stopsInRange = if (sourceIndex <= destinationIndex) {
                            routeInfo.subList(sourceIndex, destinationIndex + 1)
                        } else {
                            val stopsInReverseRange = routeInfo.subList(destinationIndex, sourceIndex + 1)
                            stopsInReverseRange.reversed()
                        }
                        stopsInRange
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }



    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 16.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("search")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "menu",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        navController.navigate("travelling")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "home",
                        tint = Color(0xFFE83A30)
                    )
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Available Buses",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFamily = customFontFamily),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                val entityRouteList = stopsList
                    .filter { matchingBuses.contains(it.entityId) }
                    .map { it.entityId to it.routeName }.toSet().toList()

                LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
                    items(entityRouteList) { (entityId, routeName) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val stops = calculateStops(entityId, routeName)
                                    navController.navigate(
                                        "stopsScreen/${source}/${destination}/${
                                            stops.joinToString(
                                                ","
                                            )
                                        }"
                                    )
                                },
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Route Name:\t$routeName",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = LocalContentColor.current,
                                        lineHeight = 24.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = "Bus Number:\t$entityId",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = LocalContentColor.current,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MatchingStopsScreen(navController: NavController, stopsList: List<String>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Row(modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, top = 16.dp)
                .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        navController.navigate("busList")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ){
                    Icon(imageVector = Icons.Filled.Menu,
                        contentDescription = "menu",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        navController.navigate("travelling")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "home",
                        tint = Color(0xFFE83A30)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Bus Stops",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFamily = customFontFamily),
                modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                items(stopsList) { stop ->
                    Card (modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                    ){
                        Column (modifier = Modifier.padding(vertical = 16.dp)){
                            Text(
                                text = "Stop: $stop",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = LocalContentColor.current,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}



