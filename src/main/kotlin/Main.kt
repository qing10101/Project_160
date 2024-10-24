import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*

@Composable
@Preview
fun LoginPanel() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val panelBackgroundImage: Painter = painterResource("img.png")

    MaterialTheme {
        Surface(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(Color.Cyan), // Light blue background color
            color = Color.White.copy(alpha = 0.8f), // Semi-transparent background for the login form
        ) {
            // Set the background image for the panel
            Image(
                painter = panelBackgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop, // Scales to fill the panel area
                modifier = Modifier.fillMaxSize() // Fills the panel area
            )
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                //horizontalAlignment =  Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                 ){
                Text("Login", style = MaterialTheme.typography.h5, color = Color.White)

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White) },
                    modifier = androidx.compose.ui.Modifier.padding(vertical = 8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(100,0,255),  // Outline color when not focused
                        cursorColor = Color.White           // Cursor color
                    ),
                    textStyle = TextStyle(color = Color.White)
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = androidx.compose.ui.Modifier.padding(vertical = 8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(100,0,255),  // Outline color when not focused
                        cursorColor = Color.White           // Cursor color
                    ),
                    textStyle = TextStyle(color = Color.White)
                )

                // Login button
                Button(
                    onClick = {
                        if (username == "user" && password == "password") {
                            message = "Login successful!"
                        } else {
                            message = "Invalid credentials, try again."
                        }
                    },
                    modifier = androidx.compose.ui.Modifier.padding(top = 16.dp)
                ) {
                    Text("Login")
                }

                // Display message
                if (message.isNotEmpty()) {
                    Text(message, modifier = androidx.compose.ui.Modifier.padding(top = 16.dp), color = Color.White)
                }
            }
        }
    }
}

fun main() = application {
    val windowState = rememberWindowState(
        width = 300.dp,    // Set the window width
        height = 400.dp,   // Set the window height
        position = WindowPosition(500.dp,300.dp),
        placement = WindowPlacement.Floating
    )
    Window(
        resizable = false,
        onCloseRequest = ::exitApplication,
        state = windowState

    ) {
        LoginPanel()
        window.pack()
    }
}
