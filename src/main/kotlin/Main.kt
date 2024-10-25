import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
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

fun deleteUser(username: String): Boolean {
    val connection = connectToDatabase()
    connection?.autoCommit = false  // Start transaction

    return try {
        val query = "DELETE FROM users WHERE username = ?"
        val statement = connection?.prepareStatement(query)
        statement?.setString(1, username)
        val rowsAffected = statement?.executeUpdate()

        connection?.commit()  // Commit the transaction
        statement?.close()
        rowsAffected!! > 0  // Return true if the user was deleted
    } catch (e: SQLException) {
        connection?.rollback()  // Rollback in case of an error
        println("Error deleting user: ${e.message}")
        false
    } finally {
        connection?.close()
    }
}

fun deleteUsersTable() {
    val connection = connectToDatabase()
    connection?.autoCommit = false  // Start transaction

    try {
        val query = "DROP TABLE IF EXISTS users"
        val statement = connection?.createStatement()
        statement?.executeUpdate(query)

        connection?.commit()  // Commit the transaction
    } catch (e: SQLException) {
        connection?.rollback()  // Rollback in case of an error
        println("Error deleting table: ${e.message}")
    } finally {
        connection?.close()
    }
}


fun addRootUser(connection: Connection) {
    if (!isUsernameTaken("root")) {
        val insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)"
        val preparedStatement = connection.prepareStatement(insertQuery)
        preparedStatement?.setString(1, "root")
        preparedStatement?.setString(2, "wangzhirong")  // Replace with a strong password
        preparedStatement?.executeUpdate()
        preparedStatement?.close()
        println("Root user created successfully.")
    } else {
        println("Root user already exists.")
    }
}


// Function to check if a username exists in the database
fun isUsernameTaken(username: String): Boolean {
    val connection = connectToDatabase()
    val query = "SELECT COUNT(*) FROM users WHERE username = ?"
    val preparedStatement = connection?.prepareStatement(query)
    preparedStatement?.setString(1, username)
    val resultSet: ResultSet? = preparedStatement?.executeQuery()
    val count = resultSet?.getInt(1)
    resultSet?.close()
    preparedStatement?.close()
    connection?.close()

    // If count > 0, the username is taken
    if (count != null) {
        return count > 0
    }
    return false
}
fun verifyUser(connection: Connection, username: String, password: String): Boolean {
    val selectSQL = "SELECT * FROM users WHERE username = ? AND password = ?;"
    val preparedStatement = connection.prepareStatement(selectSQL)
    preparedStatement.setString(1, username)
    preparedStatement.setString(2, password)

    val resultSet: ResultSet = preparedStatement.executeQuery()

    // Check if any result matches
    return resultSet.next()
}

fun insertUser(connection: Connection, username: String, password: String): Boolean {
    // Check if the username already exists
    if (isUsernameTaken(username)) {
        println("Username is already taken")
        return false
    }
    val insertSQL = "INSERT INTO users (username, password) VALUES (?, ?);"
    val preparedStatement = connection.prepareStatement(insertSQL)
    preparedStatement.setString(1, username)
    preparedStatement.setString(2, password)
    preparedStatement.executeUpdate()
    println("User $username has been inserted.")
    return true
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
                                val userCreated = insertUser(connection, username, password)
                                if(!userCreated) {
                                    message = "Username already taken."
                                }else
                                {
                                    message = "User created successfully!"
                                    connection.close()
                                    onUserCreated()  // Navigate back to login
                                }
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
fun LoginPanel(onLoginSuccess: () -> Unit, onCreateAccount: () -> Unit, onAdminLogin: () -> Unit){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val panelBackgroundImage: Painter = painterResource("login.png")
    val connection = connectToDatabase()  // Establish connection when the composable is created
    connection?.let {
        createUsersTable(it) // Ensure the users table exists
    }
    if (connection != null) {
        addRootUser(connection)
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
                                if(username.equals("root"))
                                {
                                    connection.close()
                                    onAdminLogin()  // Admin login is successful
                                }
                                else
                                {
                                    connection.close()
                                    onLoginSuccess()  // Login is successful
                                }

                            } else {
                                message = "Invalid credentials, try again."
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Login")
                    }

                    Spacer(Modifier.height(16.dp))

                    // Create new account button
                    Button(onClick = { onCreateAccount()
                        connection?.close() // Close database connection when navigating away from the create account window
                                     }, modifier = Modifier.fillMaxWidth()) {
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

@Composable
fun UserManagementPanel() {
    var usernameToDelete by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val panelBackgroundImage: Painter = painterResource("manage.png")
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center  // Center everything horizontally and vertically
        )
        {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("User Management Panel", style = MaterialTheme.typography.h5, color = Color.White)

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Specific User
                Text("Delete a specific user by username:", color = Color.White )
                OutlinedTextField(
                    value = usernameToDelete,
                    onValueChange = { usernameToDelete = it },
                    label = { Text("Username") }
                )

                Button(
                    onClick = {
                        if (deleteUser(usernameToDelete)) {
                            message = "User '$usernameToDelete' deleted successfully."
                        } else {
                            message = "User '$usernameToDelete' not found."
                        }
                        usernameToDelete = ""
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Delete User")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Entire Users Table
                Button(
                    onClick = {
                        deleteUsersTable()
                        message = "All users deleted successfully."
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Delete All Users", color = Color.White)
                }

                // Display a message
                if (message.isNotEmpty()) {
                    Text(message, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }



}



@Composable
fun App() {
    var screenState by remember { mutableStateOf(ScreenState.Login) }
    Crossfade(targetState = screenState,
            animationSpec = tween(durationMillis = 2000,
                easing = FastOutSlowInEasing  // Fast start, slow finish
            )  // Set the duration in milliseconds (e.g., 2000ms = 2 seconds)
    ) { state ->
        when (state) {
            ScreenState.Login -> LoginPanel(
                onLoginSuccess = {
                    // Move to the stock trading window (or another screen)
                    screenState = ScreenState.StockTrading
                },
                onCreateAccount = {
                    // Move to the registration screen
                    screenState = ScreenState.Register
                },
                onAdminLogin = {
                    // Move to the admin panel (or another screen)
                    screenState = ScreenState.AdminPanel
                }
            )
            ScreenState.Register -> RegisterUserWindow(
                onUserCreated = {
                    // Return to the login screen
                    screenState = ScreenState.Login
                }
            )
            ScreenState.StockTrading -> StockTradingWindow()
            ScreenState.AdminPanel -> UserManagementPanel()
        }
    }
}

// Enum to represent the different screens
enum class ScreenState {
    Login,
    Register,
    StockTrading,
    AdminPanel
}

fun main() = application {
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
    Window(state = windowState, onCloseRequest = ::exitApplication) {
        App()
    }
}
