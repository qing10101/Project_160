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
import java.sql.*

fun verifyUser(connection: Connection, username: String, password: String): Boolean {
    val selectSQL = "SELECT * FROM users WHERE username = ? AND password = ?;"
    val preparedStatement = connection.prepareStatement(selectSQL)
    preparedStatement.setString(1, username)
    preparedStatement.setString(2, password)

    val resultSet: ResultSet = preparedStatement.executeQuery()

    // Check if any result matches
    return resultSet.next()
}

fun insertUser(connection: Connection, username: String, password: String) {
    val insertSQL = "INSERT INTO users (username, password) VALUES (?, ?);"
    val preparedStatement = connection.prepareStatement(insertSQL)
    preparedStatement.setString(1, username)
    preparedStatement.setString(2, password)
    preparedStatement.executeUpdate()
    println("User $username has been inserted.")
}

// Function to create users table if it doesn't exist
fun createUsersTable(connection: Connection) {
    val createTableSQL = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL
        );
    """.trimIndent()

    val statement: Statement = connection.createStatement()
    statement.execute(createTableSQL)
}

// Function to connect to the database
fun connectToDatabase(): Connection? {
    return try {
        // SQLite connection string
        val url = "jdbc:sqlite:users.db"  // This will create or open a file named users.db
        DriverManager.getConnection(url).also {
            println("Connection to SQLite has been established.")
        }
    } catch (e: Exception) {
        println(e.message)
        null
    }
}
@Composable
fun RegisterUserWindow(onUserCreated: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val panelBackgroundImage: Painter = painterResource("create_account.png")

    val connection = connectToDatabase()

    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            color = Color.White.copy(alpha = 0.9f)

        ) {
            Image(
                painter = panelBackgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop, // Scales to fill the panel area
                modifier = Modifier.fillMaxSize() // Fills the panel area
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create New Account", style = MaterialTheme.typography.h5, color = Color.White)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    if (connection != null) {
                        if (password == confirmPassword) {
                            if (username.isNotEmpty() && password.isNotEmpty()) {
                                insertUser(connection, username, password)
                                message = "User created successfully!"
                                onUserCreated()  // Navigate back to login
                            } else {
                                message = "Please fill out all fields."
                            }
                        } else {
                            message = "Passwords do not match."
                        }
                    }
                }) {
                    Text("Create Account")
                }

                Spacer(Modifier.height(16.dp))

                Text(message, color = Color.White)
            }
        }
    }
}

// Define the login window
@Composable
@Preview
fun LoginPanel(onLoginSuccess: () -> Unit, onCreateAccount: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val panelBackgroundImage: Painter = painterResource("login.png")
    val connection = connectToDatabase()  // Establish connection when the composable is created
    connection?.let {
        createUsersTable(it) // Ensure the users table exists
    }
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Cyan), // Light blue background color
            color = Color.White.copy(alpha = 0.8f), // Semi-transparent background for the login form、
        ) {
            Image(
                painter = panelBackgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop, // Scales to fill the panel area
                modifier = Modifier.fillMaxSize() // Fills the panel area
            )
            // Box with Alignment.Center to place the content in the center of the window
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center  // Center everything horizontally and vertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.75f),  // Adjust this to control the width of the column
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Login", style = MaterialTheme.typography.h5, color = Color.White)

                    Spacer(Modifier.height(16.dp))

                    // Username input field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(150,0,255),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // Password input field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.White) },
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = TextStyle(color = Color.White),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(150,0,255),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // Login button
                    Button(onClick = {
                        if (connection != null) {
                            if (verifyUser(connection, username, password)) {
                                onLoginSuccess()  // Login is successful
                            } else {
                                message = "Invalid credentials, try again."
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Login")
                    }

                    Spacer(Modifier.height(16.dp))

                    // Create new account button
                    Button(onClick = { onCreateAccount() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Create New Account")
                    }

                    Spacer(Modifier.height(16.dp))

                    // Message display
                    if (message.isNotEmpty()) {
                        Text(message, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StockTradingWindow() {
    var stockSymbol by remember { mutableStateOf("") }
    var portfolio by remember { mutableStateOf(mutableListOf<String>()) }
    var message by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF101010)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Stock Trading Simulator", color = Color.White, style = MaterialTheme.typography.h5)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = stockSymbol,
                    onValueChange = { stockSymbol = it },
                    label = { Text("Enter Stock Symbol", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                Row {
                    Button(onClick = {
                        if (stockSymbol.isNotEmpty()) {
                            portfolio.add("Bought: $stockSymbol")
                            message = "Bought $stockSymbol"
                        }
                    }) {
                        Text("Buy")
                    }

                    Spacer(Modifier.width(16.dp))

                    Button(onClick = {
                        if (stockSymbol.isNotEmpty()) {
                            portfolio.add("Sold: $stockSymbol")
                            message = "Sold $stockSymbol"
                        }
                    }) {
                        Text("Sell")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(message, color = Color.White)

                Spacer(Modifier.height(16.dp))

                Text("Portfolio:", color = Color.White)
                portfolio.forEach {
                    Text(it, color = Color.LightGray)
                }
            }
        }
    }
}


fun main() = application {
    var showLoginWindow by remember { mutableStateOf(true) }
    var showRegisterWindow by remember { mutableStateOf(false) }
    var showStockTradingWindow by remember { mutableStateOf(false) }

    // Get screen dimensions and center window
    val screenWidth = java.awt.Toolkit.getDefaultToolkit().screenSize.width.dp
    val screenHeight = java.awt.Toolkit.getDefaultToolkit().screenSize.height.dp
    val windowWidth = 400.dp // Adjust window width here
    val windowHeight = 600.dp // Adjust window height here
    val windowState = rememberWindowState(
        width = windowWidth,
        height = windowHeight,
        position = WindowPosition((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2),
        placement = WindowPlacement.Floating
    )

    if (showLoginWindow) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Login",
            state = windowState
        ) {
            LoginPanel(
                onLoginSuccess = {
                    showLoginWindow = false
                    showStockTradingWindow = true
                },
                onCreateAccount = {
                    showLoginWindow = false
                    showRegisterWindow = true
                }
            )
        }
    }

    if (showRegisterWindow) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Create Account",
            state = windowState
        ) {
            RegisterUserWindow(
                onUserCreated = {
                    showRegisterWindow = false
                    showLoginWindow = true
                }
            )
        }
    }

    if (showStockTradingWindow) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Stock Trading Simulator",
            state = windowState
        ) {
            StockTradingWindow()
        }
    }
}

