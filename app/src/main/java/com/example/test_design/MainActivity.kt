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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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

data class Product(
    val name: String,
    val description: String,
    val price: Int,
    val category: String
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

                val cart = remember { mutableStateListOf<Product>() }

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") { GradientScreen(navController, cart) }
                    composable("second") {SecondScreen(navController, cart) }
                    composable("pinScreen") {
                        PinScreen(
                            navController = navController,
                            cart = cart,
                            onPinEntered = { pin -> println("PIN: $pin") }
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
    cart: androidx.compose.runtime.snapshots.SnapshotStateList<Product>
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
                Color(0xFFD77575), Color(0xFFFA2F87)
            )
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
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    Button(
                        onClick = { selectedCategory = category },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCategory == category) Color(0xFF4CAF50) else Color.Gray
                        )
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
                ProductCard(product) { cart.add(product) }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddToCart: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(140.dp)
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                    fontSize = 20.sp,
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

                    androidx.compose.material3.Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(
                            text = "Lägg till i varukorgen",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecondScreen(
    navController: NavController,
    cart: androidx.compose.runtime.snapshots.SnapshotStateList<Product>
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFD77575), Color(0xFFFA2F87)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Din varukorg", color = Color.White, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(32.dp))
            CartSummary(cart)
            Spacer(modifier = Modifier.height(32.dp))
            androidx.compose.material3.Button(
                onClick = { navController.navigate("pinScreen") }
            ) { Text(text = "Betala med kort", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            androidx.compose.material3.Button(
                onClick = { navController.navigate("main") }
            ) { Text(text = "Tillbaka", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }}}

@Composable
fun CartSummary(cart: androidx.compose.runtime.snapshots.SnapshotStateList<Product>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C3C)),
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Varukorg", fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                if (cart.isEmpty()) {
                    Text("Din varukorg är tom", color = Color.Gray)
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
                                    Text("${item.name} - ${item.price} kr", color = Color.White)
                                    IconButton(
                                        onClick = { cart.remove(item) }
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
                    val total = cart.sumOf { it.price }
                    Text("Totalt: $total kr", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
        }
}


@Composable
fun PinScreen(
    navController: NavController,
    cart: SnapshotStateList<Product>,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    val total = cart.sumOf { it.price }

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
                    Button(
                        onClick = {
                            when (label) {
                                "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                "OK" -> if (pin.length == 4) onPinEntered(pin)
                                else -> if (pin.length < 4) pin += label
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(label, fontSize = 24.sp, color = Color.White)
                    }
                }
            }
        }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate("second") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "Avbryt köp",
                        color = Color.White
                    )
                }
    }
}