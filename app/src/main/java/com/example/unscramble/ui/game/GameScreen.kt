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
package com.example.unscramble.ui.game

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unscramble.R
import com.example.unscramble.data.Language
import com.example.unscramble.data.LevelGame
import com.example.unscramble.ui.theme.UnscrambleTheme

@Composable
fun GameScreen(
    // Por defecto llama al padre de GameViewModel
    // Esto hace que mi ViewModel accede a nuestro repositorio de las preferencias
    gameViewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),

) {
    // Está accediendo al uiState público
    // Se mantiene a la escucha esperando un cambio de estado
    val gameUiState by gameViewModel.uiState.collectAsState()

    // Padding por defecto
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    if(gameUiState.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.width(50.dp),
            color = colorScheme.secondary,
            trackColor = colorScheme.surfaceVariant,
        )
        return
    }

    if (gameUiState.isSettingsDialogVisible) {
        SettingsDialog(
            currentLanguage = gameUiState.language,
            currentLevel = gameUiState.levelGame,
            onDismiss = { gameViewModel.hideSettings() },
            onSave = { newLanguage, newLevel ->
                gameViewModel.setSettings(language = newLanguage, levelGame = newLevel)
                gameViewModel.hideSettings()
            }
        )
    }

    if (gameUiState.isGameOver) {
        FinalScoreDialog(
            score = gameUiState.score,
            onPlayAgain = {
                gameViewModel.resetGame()
            },
        )
    }
    Scaffold (
        floatingActionButton = {
            StandardFloatingButton(
                onClickIconButton = { gameViewModel.showSettings() },
                modifier = Modifier.padding(10.dp),
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .safeDrawingPadding()
                    .padding(mediumPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = typography.titleLarge,
                )
                GameLayout(
                    // gameViewModel.userGuess -> Permite ver la palabra propuesta
                    userGuess = gameViewModel.userGuess,
                    isGuessWrong = gameUiState.isGuessedWordWrong,
                    wordCounter = gameUiState.currentWordCount,
                    maxNoCountWord = gameUiState.levelGame,
                    // updateUserGuess -> es una función guarda la suposición del usuario
                    onUserGuessChanged = { gameViewModel.updateUserGuess(it) },
                    onKeyboardDone = { gameViewModel.checkUserGuess() },
                    currentScrambledWord = gameUiState.currentScrambledWord,
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
            }
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
//    currentScrambledWord: String,
    userGuess: String,
    isGuessWrong: Boolean,
    wordCounter: Int,
    maxNoCountWord: Int = 10,
    onUserGuessChanged: (String) -> Unit,
    onKeyboardDone: () -> Unit,
    currentScrambledWord: String,
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
                text = stringResource(R.string.word_count, wordCounter, maxNoCountWord),
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
                    // Si está equivocado muestra un mensaje de error
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
@Composable
private fun FinalScoreDialog(
    score: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.current

    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.
        },
        title = { Text(text = stringResource(R.string.congratulations)) },
        text = { Text(text = stringResource(R.string.you_scored, score)) },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = {
                    activity?.finish()
                }
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {
            TextButton(onClick = onPlayAgain) {
                Text(text = stringResource(R.string.play_again))
            }
        }
    )
}

@Composable
fun StandardFloatingButton(
    icon: ImageVector = Icons.Filled.Settings,
    contentDescriptionIcon: String = stringResource(R.string.settings),
    onClickIconButton: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClickIconButton,
        modifier = modifier.size(50.dp),
        containerColor = colorScheme.primary,
        contentColor = colorScheme.onPrimary
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescriptionIcon
        )
    }
}

@Composable
fun SettingsDialog(
    currentLanguage: String,
    currentLevel: Int,
    onDismiss: () -> Unit = {},
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
                Text(text = stringResource(R.string.select_language), style = typography.titleMedium)
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
                Text(text = stringResource(R.string.select_game_level), style = typography.titleMedium)
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

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    UnscrambleTheme {
        GameScreen()
    }
}
