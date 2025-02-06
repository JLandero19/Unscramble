/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.unscramble.ui.Game

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.unscramble.R
import com.example.unscramble.data.Language
import com.example.unscramble.data.LevelGame
import com.example.unscramble.ui.theme.UnscrambleTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val gameUiState by gameViewModel.uiState.collectAsState()

    var isSettingsDialogVisible by remember { mutableStateOf(false) }

    //Comprueba la bandera de fin de juego para mostrar el diálogo.
    if (gameUiState.isGameOver) {
        FinalScoreDialog(
            score = gameUiState.score,
            onPlayAgain = { name -> gameViewModel.resetGame(name) },
            onExitGame = { name ->
                gameViewModel.resetGame(name)
                navController.navigate("ranking")
            }
        )
    }

    if (isSettingsDialogVisible) {
        SettingsDialog(
            currentLanguage = gameUiState.language,
            currentLevel = gameUiState.levelGame,
            onDismiss = { isSettingsDialogVisible = false },
            onSave = { language, level -> gameViewModel.setSettings(language, level) })
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (gameUiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            return
        }

        // Miramos si el gameUiState.userMessage tiene un mensaje de error en concreto
        if (gameUiState.userMessage == UserMessage.ERROR_GETTING_WORDS) {
            Image(
                painter = painterResource(R.drawable.no_words_found),
                contentDescription = stringResource(R.string.no_words_found),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(300.dp)
           )
        } else {
            Text(
                text = stringResource(R.string.app_name),
                style = typography.titleLarge,
            )
            GameLayout(
                onUserGuessChanged = { gameViewModel.updateUserGuess(it) },
                onKeyboardDone = { gameViewModel.checkUserGuess() },
                currentScrambledWord = gameUiState.currentScrambledWord,
                userGuess = gameViewModel.userGuess,
                isGuessWrong = gameUiState.isGuessedWordWrong,
                wordCount = gameUiState.usedWords.size,
                maxNoWords = gameUiState.levelGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(mediumPadding)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(mediumPadding),
                verticalArrangement = Arrangement.spacedBy(mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { gameViewModel.checkUserGuess() }
                ) {
                    Text(
                        text = stringResource(R.string.submit),
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = { gameViewModel.skipWord() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.skip),
                        fontSize = 16.sp
                    )
                }
            }

            GameStatus(score = gameUiState.score, modifier = Modifier.padding(20.dp))

            IconButton(
                onClick = { isSettingsDialogVisible = true },
                content = {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
        }
    }
}

@Composable
fun GameStatus(score: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.score, score),
            style = typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun GameLayout(
    onUserGuessChanged: (String) -> Unit,
    onKeyboardDone: () -> Unit,
    currentScrambledWord: String,
    userGuess: String,
    isGuessWrong: Boolean,
    wordCount: Int,
    maxNoWords: Int,
    modifier: Modifier = Modifier
) {
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(mediumPadding)
        ) {
            Text(
                modifier = Modifier
                    .clip(shapes.medium)
                    .background(colorScheme.surfaceTint)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .align(alignment = Alignment.End),
                text = stringResource(R.string.word_count, wordCount, maxNoWords),
                style = typography.titleMedium,
                color = colorScheme.onPrimary
            )
            Text(
                text = currentScrambledWord,
                style = typography.displayMedium
            )
            Text(
                text = stringResource(R.string.instructions),
                textAlign = TextAlign.Center,
                style = typography.titleMedium
            )
            OutlinedTextField(
                value = userGuess,
                singleLine = true,
                shape = shapes.large,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    disabledContainerColor = colorScheme.surface,
                ),
                onValueChange = onUserGuessChanged,
                label = {
                    if (isGuessWrong) {
                        Text(stringResource(R.string.wrong_guess))
                    } else {
                        Text(stringResource(R.string.enter_your_word))
                    }
                },
                isError = isGuessWrong,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onKeyboardDone() }
                )
            )
        }
    }
}

/*
 * Creates and shows an AlertDialog with final score.
 */
@SuppressLint("ContextCastToActivity")
@Composable
private fun FinalScoreDialog(
    score: Int,
    onPlayAgain: (String) -> Unit,
    onExitGame: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = (LocalContext.current as Activity)
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.
        },
        title = { Text(text = stringResource(R.string.congratulations)) },
        text = {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = stringResource(R.string.you_scored, score))
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    shape = shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        disabledContainerColor = colorScheme.surface,
                    ),
                    onValueChange = { text = it },
                    label = {
                        Text(stringResource(R.string.enter_username))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )
                )
            }

        },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = {
                    onExitGame(text)
                }
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {
            TextButton(onClick = { onPlayAgain(text) }) {
                Text(text = stringResource(R.string.play_again))
            }
        }
    )
}

@Composable
fun SettingsDialog(
    currentLanguage: String,
    currentLevel: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var selectedLevel by remember { mutableIntStateOf(currentLevel) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings)) },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Grupo de RadioButtons para el idioma
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Language.entries.forEach { language ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (language.language == selectedLanguage),
                            onClick = { selectedLanguage = language.language }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = language.name)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grupo de RadioButtons para el nivel del juego
                Text(
                    text = stringResource(R.string.select_game_level),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LevelGame.entries.forEach { level ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (level.level == selectedLevel),
                            onClick = { selectedLevel = level.level }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = level.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(selectedLanguage, selectedLevel)
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    // NavController
    val navController = rememberNavController()
    UnscrambleTheme {
        GameScreen(navController = navController)
    }
}
