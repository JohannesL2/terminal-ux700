package com.example.test_design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.utils.generateOrderNumber
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SellerLogin(
    navController: NavController,
    onDismiss: () -> Unit,
    onPinVerified: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    val maxLength = 4

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1E1E2F))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Säljar-login",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ange 4-siffriga PIN \n Få provision, samla dina kunder på en plats!",
                    fontSize = 16.sp,
                    color = Color.White.copy(0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier
                        .height(24.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp))
                {
                    repeat(maxLength) { index ->
                        val filled = index < pin.length
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (index < pin.length) Color.White else Color(0xFF6200EE))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val buttons = listOf(
                    listOf("1","2","3"),
                    listOf("4","5","6"),
                    listOf("7","8","9"),
                    listOf("⌫","0","OK")
                )

                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        row.forEach { label ->
                            val isBack = label == "⌫"
                            val isOk = label == "OK"
                            val enabled = when(label){
                                "⌫" -> pin.isNotEmpty()
                                "OK" -> pin.length == maxLength
                                else -> pin.length < maxLength
                            }

                            Button(onClick = {
                                when {
                                    isBack && pin.isNotEmpty() -> pin = pin.dropLast(1)
                                    isOk && pin.length == maxLength -> onPinVerified(pin)
                                    !isBack && !isOk && pin.length < maxLength -> pin += label
                                }
                            },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = enabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when(label) {
                                        "OK" -> Color(0xFF4CAF50)
                                        "⌫" -> Color(0xFFFFA726)
                                        else -> Color.White
                                    },
                                    contentColor = if(isOk || isBack) Color.White else Color.Black
                                )
                                ) {
                                Text(
                                    text = label,
                                    fontSize = if(isOk) 16.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {onDismiss()},
                    colors = ButtonDefaults.buttonColors(contentColor = Color.Gray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Avbryt", color = Color.White)
                }
            }
        }
    }
}