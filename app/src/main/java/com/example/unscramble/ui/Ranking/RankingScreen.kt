package com.example.unscramble.ui.Ranking

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.unscramble.R
import com.example.unscramble.datamodel.GameModel
import com.example.unscramble.ui.Game.GameViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun RankingScreen(
    gameViewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)
) {
    // Estado para almacenar los juegos
    val games = remember { mutableStateOf<List<GameModel>>(emptyList()) }
    val activity = (LocalContext.current as Activity)

    // Ejecutar la corrutina para obtener los juegos
    LaunchedEffect(Unit) {
        games.value = gameViewModel.rankingGames()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ){
        item {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    stringResource(R.string.ranking),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    stringResource(R.string.name_rank),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    stringResource(R.string.score_rank),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        itemsIndexed(items = games.value) { index, game ->
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_medium)),
                shape = MaterialTheme.shapes.medium
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        stringResource(R.string.rank, index + 1),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        game.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        game.score.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { activity.finish() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.exit),
                    fontSize = 16.sp
                )
            }
        }
    }
}