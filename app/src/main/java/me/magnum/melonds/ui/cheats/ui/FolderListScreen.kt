package me.magnum.melonds.ui.cheats.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.magnum.melonds.R
import me.magnum.melonds.domain.model.CheatFolder
import me.magnum.melonds.ui.cheats.model.CheatsScreenUiState
import me.magnum.melonds.ui.cheats.ui.item.FolderItem
import me.magnum.melonds.ui.cheats.ui.item.HighlightedText
import me.magnum.melonds.ui.common.component.dialog.TextInputDialog
import me.magnum.melonds.ui.common.component.dialog.rememberTextInputDialogState
import me.magnum.melonds.ui.common.melonOutlinedTextFieldColors

@Composable
fun FolderListScreen(
    modifier: Modifier,
    contentPadding: PaddingValues,
    folders: CheatsScreenUiState<List<CheatFolder>>,
    onFolderClick: (CheatFolder) -> Unit,
    onAddFolder: (String) -> Unit,
) {
    when (folders) {
        is CheatsScreenUiState.Loading -> LoadingScreen(modifier.padding(contentPadding))
        is CheatsScreenUiState.Ready -> List(
            modifier = modifier,
            contentPadding = contentPadding,
            folders = folders.data,
            onFolderClick = onFolderClick,
            onAddFolder = onAddFolder,
        )
    }
}

@Composable
private fun List(
    modifier: Modifier,
    contentPadding: PaddingValues,
    folders: List<CheatFolder>,
    onFolderClick: (CheatFolder) -> Unit,
    onAddFolder: (String) -> Unit,
) {
    val newFolderDialogState = rememberTextInputDialogState()
    val resources = LocalResources.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults = remember(folders, searchQuery) {
        folders.mapNotNull { folder ->
            val matchingCheats = if (searchQuery.isBlank()) {
                emptyList()
            } else {
                folder.cheats.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            if (searchQuery.isBlank() || folder.name.contains(searchQuery, ignoreCase = true) || matchingCheats.isNotEmpty()) {
                folder to matchingCheats
            } else {
                null
            }
        }
    }

    Box(modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                )
                .consumeWindowInsets(contentPadding),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                label = { Text(stringResource(R.string.hint_search_cheats_and_folders)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.clear),
                            )
                        }
                    }
                },
                colors = melonOutlinedTextFieldColors(),
            )

            if (searchResults.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        modifier = Modifier.padding(24.dp).align(Alignment.Center),
                        text = stringResource(R.string.no_cheats_found),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = 16.dp + 56.dp + 16.dp, // Take FAB into consideration
                    ),
                ) {
                    items(searchResults) { (folder, matchingCheats) ->
                        FolderItem(
                            modifier = Modifier.fillMaxWidth(),
                            folder = folder,
                            highlightQuery = searchQuery,
                            onClick = { onFolderClick(folder) },
                        )
                        matchingCheats.forEach { cheat ->
                            HighlightedText(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { onFolderClick(folder) }
                                    .padding(start = 72.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                                text = cheat.name,
                                query = searchQuery,
                                style = MaterialTheme.typography.body2,
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current) + 16.dp,
                ),
            onClick = {
                newFolderDialogState.show(
                    initialText = resources.getString(R.string.cheat_folder_default_name),
                    onConfirm = {
                        onAddFolder(it)
                    }
                )
            },
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Filled.CreateNewFolder),
                contentDescription = stringResource(R.string.add_cheat_folder),
            )
        }
    }

    TextInputDialog(
        title = stringResource(R.string.add_cheat_folder),
        dialogState = newFolderDialogState,
    )
}
