package com.emenjivar.threedimensionalprojections.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emenjivar.threedimensionalprojections.parser.convertToShape
import com.emenjivar.threedimensionalprojections.shapes.CubeInstance
import com.emenjivar.threedimensionalprojections.shapes.Shape
import com.emenjivar.threedimensionalprojections.shapes.TetrahedronInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapePickerBottomSheet(
    sheetState: SheetState,
    selectedShape: Shape,
    availableLocalShapes: List<Shape>,
    onPickLocalShape: (Shape) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    if (sheetState.isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                }
            }
        ) {
            ShapePickerLayout(
                selectedShape = selectedShape,
                availableLocalShapes = availableLocalShapes,
                onPickShape = {
                    coroutineScope.launch {
                        sheetState.hide()
                    }
                    onPickLocalShape(it)
                },
            )
        }
    }
}

@Composable
fun ShapePickerLayout(
    selectedShape: Shape,
    availableLocalShapes: List<Shape>,
    onPickShape: (Shape) -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                loading = true
                val result = context.contentResolver.openInputStream(it)
                    ?.bufferedReader()
                    ?.readText()
                val fileName = getFileName(context = context, uri = uri)
                val pickedShape = convertToShape(
                    name = fileName.orEmpty(),
                    objContent = result.orEmpty()
                )
                loading = false
                onPickShape(pickedShape)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Select a shape",
                style = MaterialTheme.typography.titleLarge
            )

            availableLocalShapes.forEach { shape ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { onPickShape(shape) },
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shape == selectedShape) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    } else {
                        Spacer(modifier = Modifier.width(24.dp))
                    }

                    Text(text = shape.name, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                enabled = !loading,
                onClick = {
                    filePickerLauncher.launch("*/*")
                }
            ) {
                Text("Choose from file system")
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}

@Preview
@Composable
private fun ShapePickerLayoutPreview() {
    ShapePickerLayout(
        selectedShape = CubeInstance,
        availableLocalShapes = listOf(CubeInstance, TetrahedronInstance),
        onPickShape = {},
    )
}