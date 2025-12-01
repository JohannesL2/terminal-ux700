package com.example.test_design

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.w3c.dom.Text

data class Product(
    val name: String,
    val description: String,
    val price: Int,
    val category: String
)

data class CartItem(
    val product: Product,
    var quantity: Int
)

val sampleProducts = listOf(
    Product("Kaffe", "Vanligt bryggkaffe", 49, "Dryck"),
    Product("Te", "Grönt Te", 39, "Dryck"),
    Product("Cappuccino", "Med mjölkskum", 59, "Dryck"),
    Product("Latte", "Espresso med varm mjölk", 59, "Dryck"),
    Product("Kaka", "Chokladkaka", 45, "Snacks"),
    Product("Smörgås", "Ost och skinka", 55, "Mat")

)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestdesignTheme {
                val navController = rememberNavController()

                val cart = remember { mutableStateListOf<CartItem>() }

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") { GradientScreen(navController, cart) }
                    composable("second") { SecondScreen(navController, cart) }
                    composable("pinScreen") {
                        PinScreen(
                            navController = navController,
                            cart = cart,
                            onPinEntered = { pin ->
                                println("PIN: $pin")
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
    cart: androidx.compose.runtime.snapshots.SnapshotStateList<CartItem>
    ) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alla") }
    val categories = listOf("Alla", "Dryck", "Mat", "Snacks")

    val filteredProducts = sampleProducts.filter { product ->
        (selectedCategory == "Alla" || product.category == selectedCategory) &&
                (searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true))
    }
    Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp) .background(
        Brush.linearGradient(
            listOf(
                Color(0xFF000000),
                Color(0xFF111111),
                Color(0xFF222222)
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    ),) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Sök produkter...") },
                placeholder = { Text ("Sök produkter...", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(0.dp)
            )

            IconButton(
                onClick = { navController.navigate("second") },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF4CAF50))
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Kundvagn",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                 items(categories) {category ->
                    Button(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == category) Color(0xFF4CAF50) else Color.Gray
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(category, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts) { product ->
                ProductCard(product, cart) }
            }
        }
    }

@Composable
fun ProductCard(product: Product, cart: SnapshotStateList<CartItem>) {
    val onAddToCart: () -> Unit = {
        val index = cart.indexOfFirst { it.product.name == product.name }
        if (index >= 0) {
            val item = cart[index]
            cart[index] = item.copy(quantity = item.quantity + 1)
        } else {
            cart.add(CartItem(product, 1))
        }
    }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        )
        {
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
                    color = Color.White
                )

                Text(
                    text = product.description,
                    fontSize = 14.sp,
                    color = Color(0xFFCCCCCC),
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

                    Button(
                        onClick = onAddToCart,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Text(
                            text = "Lägg till i varukorgen",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

@Composable
fun SecondScreen(
    navController: NavController,
    cart: androidx.compose.runtime.snapshots.SnapshotStateList<CartItem>
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF000000),
                        Color(0xFF111111),
                        Color(0xFF222222)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Varukorg", color = Color.White, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(32.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Lägg till ny produkt",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var newProductName by remember { mutableStateOf("") }
            var newProductPrice by remember { mutableStateOf("") }

            TextField(
                value = newProductName,
                onValueChange = { newProductName = it },
                label = { Text("Produktnamn") },
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp)
            )

            TextField(
                value = newProductPrice,
                onValueChange = {
                    newProductPrice = it.filter { c -> c.isDigit() }
                }, // bara siffror
                label = { Text("Pris") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )
        }
    }

            Spacer(modifier = Modifier.height(32.dp))
            CartSummary(cart)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("pinScreen") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (cart.isNotEmpty()) Color(0xFF1976D2) else Color.Gray),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(60.dp),
                enabled = cart.isNotEmpty()
            ) { Text(text = "Betala med kort", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("main") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
            ) { Text(text = "Gå tillbaka", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }}}

@Composable
fun CartSummary(cart: androidx.compose.runtime.snapshots.SnapshotStateList<CartItem>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF33353B)),
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (cart.isEmpty()) {
                    Text("Din varukorg är tom", color = Color.Gray, fontSize = 16.sp)
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
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${item.product.name} x${item.quantity} - ${item.product.price * item.quantity} kr", color = Color.White)
                                    IconButton(
                                        onClick = {
                                            val index = cart.indexOf(item)
                                            if (item.quantity > 1) {
                                                cart[index] = item.copy(quantity = item.quantity - 1)
                                            } else {
                                                cart.remove(item)}
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Ta bort",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val total = cart.sumOf { it.product.price * it.quantity }
                    Text("Totalt: $total kr", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
        }
}


@Composable
fun PinScreen(
    navController: NavController,
    cart: SnapshotStateList<CartItem>,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    val total = cart.sumOf { it.product.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E2F))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Totalt: $total kr",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "*".repeat(pin.length),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("⌫", "0", "OK")
        )

        buttons.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                row.forEach { label ->
                    val isOk = label == "OK"
                    val isEnabled = !isOk || pin.length == 4

                    val buttonColor = when (label) {
                        "OK" -> Color(0xFF4CAF50)
                        "⌫" -> Color(0xFF757575)
                        else -> Color(0xFF2E7D32)
                    }

                        Button(
                            onClick = {
                                when {
                                    label == "⌫" && pin.isNotEmpty() -> pin = pin.dropLast(1)
                                    label == "OK" && pin.length == 4 -> onPinEntered(pin)
                                    label != "⌫" && label != "OK" && pin.length < 4 -> pin += label
                                }
                            },
                            modifier = Modifier.size(80.dp),
                            enabled = isEnabled,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEnabled) buttonColor else Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate("second") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(60.dp)
                ) {
                    Text(
                        text = "Avbryt köp",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }}}