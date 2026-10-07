package com.alejandro.luminawear.presentation.lights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.alejandro.luminawear.R
import com.alejandro.luminawear.presentation.models.Light

@Composable
fun LightControlScreen(
    light: Light,
    roomNameRes: Int,
    isOn: Boolean,
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    onTogglePower: () -> Unit,
    onBackClick: () -> Unit,
    contentPadding: PaddingValues,
    listState: TransformingLazyColumnState,
    transformationSpec: TransformationSpec
) {
    val lightName = stringResource(light.nameRes)
    val roomName = stringResource(roomNameRes)
    val title = stringResource(R.string.light_control_title, lightName, roomName).uppercase()
    
    val decreaseDesc = stringResource(R.string.light_control_decrease)
    val increaseDesc = stringResource(R.string.light_control_increase)
    val offDesc = stringResource(R.string.light_control_off_state)

    TransformingLazyColumn(
        contentPadding = contentPadding,
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ListHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.header_demo),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        item {
            val arcColorPrimary = MaterialTheme.colorScheme.primary
            val arcColorTrack = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
                    .transformedHeight(this, transformationSpec),
                contentAlignment = Alignment.Center
            ) {
                // Semicircular Canvas
                Canvas(modifier = Modifier.size(width = 180.dp, height = 90.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    val arcRect = Size(size.width, size.height * 2)
                    
                    // Background track
                    drawArc(
                        color = arcColorTrack,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, 0f),
                        size = arcRect,
                        style = style
                    )
                    
                    // Foreground track
                    if (isOn) {
                        val sweep = (brightness / 100f) * 180f
                        drawArc(
                            color = arcColorPrimary,
                            startAngle = 180f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(0f, 0f),
                            size = arcRect,
                            style = style
                        )
                    }
                }
                
                // Central text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp).semantics {
                        contentDescription = if (isOn) "$brightness %" else offDesc
                    }
                ) {
                    Text(
                        text = if (isOn) "$brightness %" else stringResource(R.string.light_control_off_state),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isOn) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isOn) stringResource(R.string.light_control_brightness) else stringResource(R.string.light_state_off),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (isOn) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .transformedHeight(this, transformationSpec),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val decreaseEnabled = brightness > 1
                    val increaseEnabled = brightness < 100
                    
                    Button(
                        onClick = { if (decreaseEnabled) onBrightnessChange(brightness - 10) },
                        enabled = decreaseEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(56.dp, 48.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .semantics { contentDescription = decreaseDesc }
                    ) {
                        Text(text = "−", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { if (increaseEnabled) onBrightnessChange(brightness + 10) },
                        enabled = increaseEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(56.dp, 48.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .semantics { contentDescription = increaseDesc }
                    ) {
                        Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = onTogglePower,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOn) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isOn) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .heightIn(min = 48.dp)
                    .border(
                        width = 1.dp, 
                        color = if (isOn) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary, 
                        shape = RoundedCornerShape(8.dp)
                    )
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(
                    text = if (isOn) stringResource(R.string.btn_turn_off) else stringResource(R.string.btn_turn_on),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item {
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .heightIn(min = 48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec)
            ) {
                Text(
                    text = stringResource(R.string.btn_back_to_room, roomName),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
