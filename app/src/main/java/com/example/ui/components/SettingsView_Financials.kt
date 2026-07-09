package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@Composable
fun SettingsFinancialsPage(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val categories by viewModel.financeCategories.collectAsState()
    
    var newMemberName by remember { mutableStateOf("") }
    var newCategoryName by remember { mutableStateOf("") }
    var selectedCategoryType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"

    SettingsSubpageWorkspace(
        title = "Financial Ledger Settings",
        description = "Configure family members and transaction categories.",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: MANAGE FAMILY MEMBERS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("FAMILY MEMBERS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WaterBlue)
                    Text("Create personal entities to segment assets, liabilities, and transactions.", fontSize = 11.sp, color = Color.Gray)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newMemberName,
                            onValueChange = { newMemberName = it },
                            placeholder = { Text("Name (e.g. Alice, Bob)", fontSize = 13.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newMemberName.isNotBlank()) {
                                    viewModel.createFamilyMember(newMemberName.trim())
                                    newMemberName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaterBlue, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (familyMembers.isEmpty()) {
                        Text("No custom family members registered yet.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            familyMembers.forEach { member ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(member.name, color = Color.White, fontSize = 14.sp)
                                    IconButton(
                                        onClick = { viewModel.deleteFamilyMember(member) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete member", tint = AlertRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- SECTION 2: MANAGE CATEGORIES ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("TRANSACTION CATEGORIES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WaterBlue)
                    Text("Add categories for segmenting income sources and expense destinations.", fontSize = 11.sp, color = Color.Gray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EXPENSE", "INCOME").forEach { type ->
                            val isSelected = selectedCategoryType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) WaterBlue else SurfaceCard)
                                    .clickable { selectedCategoryType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            placeholder = { Text("Category (e.g. Internet, Dining)", fontSize = 13.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    viewModel.createFinanceCategory(newCategoryName.trim(), selectedCategoryType)
                                    newCategoryName = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(WaterBlue),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add category", tint = Color.Black)
                        }
                    }

                    // Segmented lists of current custom categories
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("EXPENSES", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            val expCats = categories.filter { it.type == "EXPENSE" }
                            if (expCats.isEmpty()) {
                                Text("None", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                expCats.forEach { cat ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cat.name, fontSize = 12.sp, color = Color.White)
                                        IconButton(onClick = { viewModel.deleteFinanceCategory(cat) }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete category", tint = AlertRed.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("INCOMES", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            val incCats = categories.filter { it.type == "INCOME" }
                            if (incCats.isEmpty()) {
                                Text("None", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                incCats.forEach { cat ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cat.name, fontSize = 12.sp, color = Color.White)
                                        IconButton(onClick = { viewModel.deleteFinanceCategory(cat) }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete category", tint = AlertRed.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
