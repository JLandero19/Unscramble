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

package com.example.unscramble

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unscramble.ui.Game.GameScreen
import com.example.unscramble.ui.Ranking.RankingScreen
import com.example.unscramble.ui.theme.UnscrambleTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            UnscrambleTheme {
                UnscrambleApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UnscrambleApp() {
//    var showSettingsDialog by remember { mutableStateOf(false) }

    // NavController
    val navController = rememberNavController()

    // Ruta inicial
    val currentRoute by navController.currentBackStackEntryFlow
        .map { it.destination.route }
        .collectAsState(initial = null)

    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {  },
//                content = {
//                    Icon(
//                        imageVector = Icons.Default.Settings,
//                        contentDescription = stringResource(R.string.settings)
//                    )
//                }
//            )
//        },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = "game"
            ) {
                composable("game") {
                    GameScreen(navController = navController)
                }
                composable("ranking") {
                    RankingScreen()
                }
            }


        }
    }

}
