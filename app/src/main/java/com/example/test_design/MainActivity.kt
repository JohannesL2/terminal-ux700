package com.example.test_design

import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.db.AppDatabase
import com.example.test_design.data.entity.ProductEntity
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.test_design.ui.theme.TestdesignTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.ui.draw.alpha
import com.example.test_design.data.utils.generateEAN
import com.example.test_design.data.utils.generateArticleNumber
import com.example.test_design.data.utils.generateRowNumber
import com.example.test_design.data.utils.generateOrderNumber
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.core.view.WindowCompat

data class UiProduct(
    val name: String,
    val description: String,
    val price: Int,
    val category: String,
    val imageRes: Int
)

data class CartItem(
    val product: UiProduct,
    var quantity: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        AndroidThreeTen.init(this)

        val db = AppDatabase.getInstance(this)
        val dao = db.productDao()

        //efter beställning blir det ordernummer och radnummer, kopplat ihop i orderhuvud

        enableEdgeToEdge()
        setContent {
            TestdesignTheme {
                val navController = rememberNavController()

                val cart = remember { mutableStateListOf<CartItem>() }

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        GradientScreen(navController, cart, dao)}

                        composable("second") { SecondScreen(navController, cart, dao) }
                        composable("pinScreen") {
                            PinScreen(
                                navController = navController,
                                cart = cart,
                                dao = dao,
                                onPinEntered = { pin ->
                                    println("PIN: $pin")
                                }
                            )
                        }
                        composable("confirmation")
                        {
                            PaymentConfirmation(
                                navController = navController,
                                cart = cart,
                                dao = dao,
                                onClose = {
                                    cart.clear()
                                    navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun GradientScreen(
        navController: NavController,
        cart: SnapshotStateList<CartItem>,
        dao: ProductDao
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Alla") }
        val categories = listOf("Alla", "Dryck", "Mat", "Snacks")
        var showSellerLogin by remember { mutableStateOf(false) }

        val context = LocalContext.current
        var dbProducts by remember { mutableStateOf(listOf<ProductEntity>()) }

        val haptic = LocalHapticFeedback.current

        LaunchedEffect(Unit) {
            dao.getAllProducts().collect { products ->
                dbProducts = products
            }
        }

        val imageNameMap = mapOf(
            "Kaffe" to "coffee",
            "Latte" to "latte",
            "Kaka" to "cake",
            "Smörgås" to "sandwich"
        )

        val allProducts = dbProducts.map { entity ->
            val imageResId = entity.imageResName?.let { name ->
                val englishName = imageNameMap[name] ?: "placeholder_image"
                val resId =
                    context.resources.getIdentifier(englishName, "drawable", context.packageName)
                if (resId != 0) resId else R.drawable.placeholder_image
            } ?: R.drawable.placeholder_image

            UiProduct(
                name = entity.name,
                description = entity.description,
                price = entity.price,
                category = entity.category,
                imageRes = imageResId
            )
        }

        val filteredProducts = allProducts.filter { product ->
            (selectedCategory == "Alla" || product.category == selectedCategory) &&
                    (searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true))
        }
        Box (
            modifier = Modifier
                .fillMaxSize()
        )
            {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF5F5F5),
                            Color(0xFFEFEFEF)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Sök produkter...", color = Color.Gray) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Sök",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Rensa sökning",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                )


                //IconButton(
                //                    onClick = {
                //                        navController.navigate("second")
                //                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                //                    },
                //                    modifier = Modifier
                //                        .size(70.dp)
                //                        .background(Color.Black)
                //                ) {
                //                    Icon(
                //                        imageVector = Icons.Default.ShoppingCart,
                //                        contentDescription = "Kundvagn",
                //                        tint = Color.White,
                //                        modifier = Modifier.size(36.dp)
                //                    )
                //                }
                IconButton(
                    onClick = { showSellerLogin = true },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color.Black
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Säljarlogin",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            //LÅS APPEN HELT NÄR DU SKRIVER IN DIN PIN HÄR,
            // gör gärna en pin-kod som du kan testa med

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 6.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { category ->
                        Button(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCategory == category) Color(0xFF6200EE) else Color(
                                    0xFFE0E0E0
                                )
                            ),
                            modifier = Modifier
                                .height(42.dp)
                                .defaultMinSize(minWidth = 80.dp)
                        ) {
                            Text(
                                category,
                                color = if (selectedCategory == category) Color.White else Color(
                                    0xFF212121
                                ),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
//Gör en större lås ikon, kanske samma som kundvagnens bakgrund
//men fördela dom så att man inte trycker fel
                //IconButton(
                //                    onClick = { showSellerLogin = true },
                //                    modifier = Modifier
                //                        .size(50.dp)
                //                        .background(
                //                            color = Color(0xFF6200EE)
                //                        ),
                //                ) {
                //                    Icon(
                //                        imageVector = Icons.Filled.Lock,
                //                        contentDescription = "Säljarlogin",
                //                        tint = Color(0xFFFFD700),
                //                        modifier = Modifier.size(36.dp)
                //                    )
                //                }

                if (showSellerLogin) {
                    SellerLogin(
                        navController = navController,
                        onDismiss = { showSellerLogin = false },
                        onPinVerified = { pin ->
                            println("Säljar-PIN: $pin")
                            showSellerLogin = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(product, cart)
                }
            }
        }
                val cardColor by animateColorAsState(
                targetValue = if (cart.isNotEmpty()) Color(0xFF6200EE) else Color.Gray
                )

        if (cart.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable{navController.navigate("second") },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${cart.sumOf { it.quantity }} produkter valda",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${cart.sumOf { it.product.price * it.quantity }} kr",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    //${cart.sumOf { it.product.price * it.quantity }} kr

                        Icon(
                                                    imageVector = Icons.Default.ShoppingCart,
                                                    contentDescription = "Kundvagn",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(32.dp)
                                                )
                    }
                }
            }
        }
    }


    @Composable
    fun ProductCard(product: UiProduct, cart: SnapshotStateList<CartItem>) {
        val cartItemIndex = cart.indexOfFirst { it.product.name == product.name }
        val quantity = if (cartItemIndex >= 0) cart[cartItemIndex].quantity else 0

        val haptic = LocalHapticFeedback.current

        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = Color(0xFF212121),
                        maxLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${product.price} kr",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    if (quantity > 1) {
                                        cart[cartItemIndex] =
                                            cart[cartItemIndex].copy(quantity = quantity - 1)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else if (quantity == 1) {
                                        cart.removeAt(cartItemIndex)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                modifier = Modifier
                                    .size(60.dp)
                                    .offset(y = (0).dp)
                            ) {
                                Text("-", fontSize = 24.sp, color = Color(0xFF212121))
                            }

                            Text(
                                quantity.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = {
                                    if (quantity == 0) {
                                        cart.add(CartItem(product, 1))
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else {
                                        cart[cartItemIndex] =
                                            cart[cartItemIndex].copy(quantity = quantity + 1)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                                modifier = Modifier
                                    .size(60.dp)
                                    .offset(y = (0).dp)
                            ) {
                                Text("+", fontSize = 24.sp, color = Color(0xFF212121))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SecondScreen(
        navController: NavController,
        cart: SnapshotStateList<CartItem>,
        dao: ProductDao
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Varukorg", color = Color.Black, fontSize = 32.sp)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Lägg till ny produkt",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                CartSummary(cart)
                Button(
                    onClick = { navController.navigate("pinScreen") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cart.isNotEmpty()) Color(0xFF6200EE) else Color.Gray
                    ),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = cart.isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Betala med kort",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Betala med kort", fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = { navController.navigate("pinScreen") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cart.isNotEmpty()) Color(0xFF27AE60) else Color.Gray
                    ),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = cart.isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.swish_icon),
                            contentDescription = "Swish",
                            modifier = Modifier
                                .size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Betala med Swish",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = { navController.navigate("main") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) { Text(text = "Gå tillbaka", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    @Composable
    fun CartSummary(cart: androidx.compose.runtime.snapshots.SnapshotStateList<CartItem>) {
        Card(
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (cart.isEmpty()) {
                    Text("Din varukorg är tom", color = Color.LightGray, fontSize = 20.sp)
                } else {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cart) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = item.product.imageRes),
                                        contentDescription = item.product.name,
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                BorderStroke(1.dp, Color(0xFF6200EE)),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )

                                    Text(
                                        "x${item.quantity} ${item.product.name} - ${item.product.price * item.quantity} kr",
                                        color = Color(0xFF212121),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 2.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            val index = cart.indexOf(item)
                                            if (item.quantity > 1) {
                                                cart[index] =
                                                    item.copy(quantity = item.quantity - 1)
                                            } else {
                                                cart.remove(item)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Ta bort",
                                            tint = Color.Red,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val total = cart.sumOf { it.product.price * it.quantity }
                    Text(
                        "Att Betala: $total kr",
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        fontSize = 26.sp
                    )
                }
            }
        }
    }


    @Composable
    fun PinScreen(
        navController: NavController,
        cart: SnapshotStateList<CartItem>,
        dao: ProductDao,
        onPinEntered: (String) -> Unit
    ) {
        var pin by remember { mutableStateOf("") }
        var showConfirmation by remember { mutableStateOf(false) }
        val total = cart.sumOf { it.product.price * it.quantity }

        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1.1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        val haptic = LocalHapticFeedback.current

        val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels /
                LocalContext.current.resources.displayMetrics.density
        val buttonSize = (screenWidth / 4.5f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E2F))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Totalt: $total kr",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Image(
                painter = painterResource(id = R.drawable.ic_contactless),
                contentDescription = "Kontaktlös betalning",
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f))
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    val filled = index < pin.length
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (filled) Color.White.copy(alpha = 1f) else Color(0xFF6200EE)
                            )
                    ) {
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val buttons = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("⌫", "0", "OK")
            )

            buttons.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    row.forEach { label ->
                        val isOk = label == "OK"
                        val isBack = label == "⌫"

                        val isEnabled = when (label) {
                            "⌫" -> pin.isNotEmpty()
                            "OK" -> pin.length == 4
                            else -> true
                        }

                        val buttonColor = when (label) {
                            "OK" -> Color(0xFF4CAF50)
                            "⌫" -> Color(0xFFFFA726)
                            else -> Color.White
                        }.copy(alpha = if (isEnabled) 1f else 0.4f)

                        val textColor = when (label) {
                            "OK", "⌫" -> Color.White.copy(alpha = if (isEnabled) 1f else 0.5f)
                            else -> Color.Black
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                when {
                                    label == "⌫" && pin.isNotEmpty() -> pin = pin.dropLast(1)
                                    label == "OK" && pin.length == 4 -> {
                                        onPinEntered(pin)
                                        showConfirmation = true
                                    }

                                    label != "⌫" && label != "OK" && pin.length < 4 -> pin += label
                                }
                            },
                            modifier = Modifier
                                .size(buttonSize),
                            enabled = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = textColor
                            )
                        ) {
                            if (isBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = textColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                Text(
                                    text = label,
                                    fontSize = if (isOk) 24.sp else 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            var showCancelDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showCancelDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Avbryt köp",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showCancelDialog) {
                Dialog(onDismissRequest = { showCancelDialog = false }) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Är du säker på att du vill avbryta köpet?", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = { showCancelDialog = false }) {
                                    Text("Nej")
                                }
                                Button(onClick = {
                                    cart.clear()
                                    navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }) {
                                    Text("Ja")
                                }
                            }
                        }
                    }
                }
            }

            if (showConfirmation) {
                Dialog(onDismissRequest = { showConfirmation = false }) {
                    PaymentConfirmation(
                        cart = cart,
                        navController = navController,
                        dao = dao,
                        onClose = {
                            showConfirmation = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun PaymentConfirmation(
        navController: NavController,
        cart: SnapshotStateList<CartItem>,
        dao: ProductDao,
        onClose: () -> Unit
    ) {
        val total = cart.sumOf { it.product.price * it.quantity }
        val orderNumber by remember { mutableStateOf(generateOrderNumber()) }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedDateTime = currentDateTime.format(formatter)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E2F))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Betalning godkänd!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Order.Nr $orderNumber",
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(
                modifier = Modifier
                    .height(12.dp)
            )

            Text(
                text = "Belopp: $total kr!\n" +
                        "KORT: VISA CREDIT\n" +
                        "KORTNR: **** **** **** 4321\n" +
                        "METOD: CHIP/PIN\n" +
                        "$formattedDateTime",
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(
                modifier = Modifier
                    .height(24.dp)
            )

            Text(
                text = "TACK FÖR DITT KÖP!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    cart.clear()
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Tillbaka till start", color = Color.White, fontSize = 20.sp)
            }
        }
    }