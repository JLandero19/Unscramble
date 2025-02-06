package com.example.unscramble.ui.Ranking

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.unscramble.R
import com.example.unscramble.datamodel.GameModel
import com.example.unscramble.ui.Game.GameViewModel

@SuppressLint("ContextCastToActivity")
@Composable
fun RankingScreen(
    gameViewModel: RankingViewModel = viewModel(factory = RankingViewModel.Factory)
) {
    val activity = (LocalContext.current as Activity)
    val rankingUiState by gameViewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_medium))
    ){
        item {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.ranking),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            HorizontalDivider(thickness = 1.dp)
        }
        itemsIndexed(items = rankingUiState.ranking) { index, game ->
            var showInfo by remember { mutableStateOf(false) }
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_medium)),
                shape = MaterialTheme.shapes.medium
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.rank, index + 1),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        game.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        game.score.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = { showInfo = !showInfo },
                    ) {
                        Icon(imageVector = Icons.Filled.Info, contentDescription = stringResource(R.string.more_info))
                    }
                }
                if (showInfo) {
                    Row (
                        modifier = Modifier.fillMaxWidth().padding(
                            horizontal = dimensionResource(R.dimen.padding_medium),
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.wrong_word, game.wrongWords))
                    }
                    Row (
                        modifier = Modifier.fillMaxWidth().padding(
                            horizontal = dimensionResource(R.dimen.padding_medium),
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.right_word, game.rightWords))
                    }
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