@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.techquantum.feature.ecommerce.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.techquantum.core.designsystem.AnimatedMoney
import com.techquantum.core.designsystem.QuantityStepper
import com.techquantum.feature.ecommerce.domain.*
import com.techquantum.feature.ecommerce.presentation.*
import java.text.NumberFormat
import java.util.Locale

private fun rupees(amount: Int): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    .apply { maximumFractionDigits = 0 }.format(amount)

@Composable
fun EmptyFashionState(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.ShoppingBag, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Button(onAction, Modifier.heightIn(min = 52.dp)) { Text(action) }
    }
}

@Composable
fun CartScreen(state: EcommerceState, onIntent: (EcommerceIntent) -> Unit, onCheckout: () -> Unit, onBrowse: () -> Unit) {
    if (state.cart.isEmpty()) {
        EmptyFashionState("Your bag has room for a favourite", "Explore thoughtfully chosen pieces for your everyday and special days.",
            "Explore the collection", onBrowse)
        return
    }
    Scaffold(bottomBar = {
        PurchaseBar("Total · ${state.cartCount} items", state.total, "Continue to checkout", onCheckout)
    }, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                Text("Good choices, together.", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("Swipe left to remove a piece, or use its remove button.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(state.cart, key = { it.key }) { item ->
                val dismiss = rememberSwipeToDismissBoxState()
                LaunchedEffect(dismiss.settledValue) {
                    if (dismiss.settledValue == SwipeToDismissBoxValue.EndToStart) onIntent(EcommerceIntent.RemoveFromCart(item.key))
                }
                SwipeToDismissBox(dismiss, backgroundContent = {
                    Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.CenterEnd) {
                        Icon(Icons.Outlined.DeleteOutline, "Remove ${item.product.name}", tint = MaterialTheme.colorScheme.error)
                    }
                }, enableDismissFromStartToEnd = false, modifier = Modifier.animateItem()) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        shadowElevation = 1.dp,
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            ProductArtwork(item.product, Modifier.width(76.dp).height(124.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.product.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                    IconButton({ onIntent(EcommerceIntent.RemoveFromCart(item.key)) }) {
                                        Icon(Icons.Outlined.DeleteOutline, "Remove ${item.product.name}")
                                    }
                                }
                                Text("${item.color} / ${item.size}", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(10.dp))
                                AnimatedMoney(item.total)
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    QuantityStepper(item.quantity,
                                        { onIntent(EcommerceIntent.ChangeQuantity(item.key, -1)) },
                                        { onIntent(EcommerceIntent.ChangeQuantity(item.key, 1)) })
                                }
                            }
                        }
                    }
                }
            }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)),
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f), modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.LocalShipping, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(if (state.delivery == 0) "Your bag qualifies for complimentary delivery."
                            else "Add ${rupees(1999 - state.subtotal)} more for complimentary delivery.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { OrderTotals(state.subtotal, state.delivery, state.total) }
            item { Text("Prices include GST. Delivery is shown before you place your order.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun PurchaseBar(label: String, total: Int, action: String, onAction: () -> Unit, enabled: Boolean = true) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedMoney(total)
            }
            Spacer(Modifier.height(12.dp))
            Button(onAction, Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp), enabled = enabled) {
                Text(action, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun OrderTotals(subtotal: Int, delivery: Int, total: Int) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Order summary", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            TotalLine("Subtotal", rupees(subtotal))
            TotalLine("Delivery", if (delivery == 0) "Complimentary" else rupees(delivery))
            TotalLine("GST", "Included in prices")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                AnimatedMoney(total)
            }
        }
    }
}

@Composable
private fun TotalLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
    }
}

@Composable
fun CheckoutScreen(state: EcommerceState, onIntent: (EcommerceIntent) -> Unit, onBrowse: () -> Unit) {
    if (state.cart.isEmpty()) {
        EmptyFashionState("Your bag is empty", "Add a piece before checking out.", "Browse collection", onBrowse)
        return
    }
    val fields = remember { listOf("name", "phone", "line", "city", "pinCode").associateWith { FocusRequester() } }
    LaunchedEffect(state.error) {
        if (state.error != null) fields.keys.firstOrNull { it in state.addressErrors }?.let { fields.getValue(it).requestFocus() }
    }
    Scaffold(modifier = Modifier.imePadding(), bottomBar = {
        PurchaseBar("Total · all charges included", state.total,
            if (state.placingOrder) "Placing your demo order…" else "Place demo order",
            { onIntent(EcommerceIntent.PlaceOrder) }, enabled = !state.placingOrder)
    }, contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("One last step.", style = MaterialTheme.typography.headlineLarge)
            Text("A showcase checkout. No payment is collected and no order is sent.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Delivery address", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            val address = state.address
            AddressField("Full name", address.name, state.addressErrors["name"], fields.getValue("name")) {
                onIntent(EcommerceIntent.UpdateAddress(address.copy(name = it)))
            }
            AddressField("Mobile number", address.phone, state.addressErrors["phone"], fields.getValue("phone"), KeyboardType.Phone) {
                onIntent(EcommerceIntent.UpdateAddress(address.copy(phone = it)))
            }
            AddressField("House, street and locality", address.line, state.addressErrors["line"], fields.getValue("line")) {
                onIntent(EcommerceIntent.UpdateAddress(address.copy(line = it)))
            }
            AddressField("City", address.city, state.addressErrors["city"], fields.getValue("city")) {
                onIntent(EcommerceIntent.UpdateAddress(address.copy(city = it)))
            }
            AddressField("PIN code", address.pinCode, state.addressErrors["pinCode"], fields.getValue("pinCode"), KeyboardType.Number) {
                onIntent(EcommerceIntent.UpdateAddress(address.copy(pinCode = it)))
            }
            Spacer(Modifier.height(4.dp))
            Text("Payment method", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            PaymentMethod.entries.forEach { method ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (state.payment == method) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(if (state.payment == method) 1.5.dp else 1.dp,
                        if (state.payment == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                ) {
                    Row(Modifier.fillMaxWidth().clickable(role = Role.RadioButton) { onIntent(EcommerceIntent.SelectPayment(method)) }
                        .semantics { selected = state.payment == method }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(state.payment == method, { onIntent(EcommerceIntent.SelectPayment(method)) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant))
                        Column(Modifier.padding(start = 6.dp)) {
                            Text(method.label, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Text(if (method == PaymentMethod.CASH_ON_DELIVERY) "Pay on arrival · demo only" else "Simulated payment · no details required",
                                style = MaterialTheme.typography.bodySmall, color = if (state.payment == method)
                                    MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Text("Review your pieces", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            state.cart.forEach { item -> TotalLine("${item.product.name} × ${item.quantity}", rupees(item.total)) }
            OrderTotals(state.subtotal, state.delivery, state.total)
            state.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive })
                TextButton({ onIntent(EcommerceIntent.ClearError) }, colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Dismiss message") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddressField(label: String, value: String, error: String?, focus: FocusRequester,
    keyboardType: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, modifier = Modifier.fillMaxWidth().focusRequester(focus),
        label = { Text(label) }, isError = error != null, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = if (error != null) ({ Text(error) }) else null,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            cursorColor = MaterialTheme.colorScheme.onSurface))
}

@Composable
fun ConfirmationScreen(order: OrderReceipt?, onContinue: () -> Unit) {
    if (order == null) {
        EmptyFashionState("Your next favourite is waiting", "Explore the collection and place a demo order.", "Continue shopping", onContinue)
        return
    }
    val progress = remember(order.id) { Animatable(0f) }
    val motion = MaterialTheme.motionScheme
    LaunchedEffect(order.id) { progress.animateTo(1f, motion.slowSpatialSpec()) }
    val container = MaterialTheme.colorScheme.primaryContainer
    val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Spacer(Modifier.height(16.dp))
        Canvas(Modifier.size(120.dp).graphicsLayer { scaleX = .8f + .2f * progress.value; scaleY = scaleX }
            .semantics { contentDescription = "Order placed successfully" }) {
            drawCircle(container)
            val points = listOf(Offset(size.width * .28f, size.height * .51f),
                Offset(size.width * .44f, size.height * .67f), Offset(size.width * .74f, size.height * .34f))
            val p = progress.value.coerceIn(0f, 1f)
            val check = Path().apply {
                moveTo(points[0].x, points[0].y)
                if (p < .4f) lineTo(points[0].x + (points[1].x - points[0].x) * p / .4f,
                    points[0].y + (points[1].y - points[0].y) * p / .4f)
                else { lineTo(points[1].x, points[1].y); lineTo(points[1].x + (points[2].x - points[1].x) * (p - .4f) / .6f,
                    points[1].y + (points[2].y - points[1].y) * (p - .4f) / .6f) }
            }
            drawPath(check, ink, style = Stroke(7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
        Text("Made for your moments.", style = MaterialTheme.typography.headlineLarge)
        Text("Thank you, ${order.address.name.substringBefore(' ')}. Your demo order is confirmed.",
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            shadowElevation = 2.dp,
        ) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(order.id, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text("${order.items.sumOf { it.quantity }} pieces · ${order.payment.label}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text("Delivery to", style = MaterialTheme.typography.labelLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text("${order.address.name}\n${order.address.line}\n${order.address.city} ${order.address.pinCode}")
                OrderTotals(order.subtotal, order.delivery, order.total)
            }
        }
        Text("This is a local showcase order. No payment or delivery has been arranged.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Button(onContinue, Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Text("Continue shopping", style = MaterialTheme.typography.titleSmall) }
        Spacer(Modifier.height(20.dp))
    }
}
