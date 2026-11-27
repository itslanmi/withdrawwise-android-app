package io.github.itslanmi.withdrawwise.feature.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.itslanmi.withdrawwise.R
import io.github.itslanmi.withdrawwise.feature.map.AtmListRow
import io.github.itslanmi.withdrawwise.ui.components.WwTextField
import io.github.itslanmi.withdrawwise.ui.model.AtmUi

/** Searchable list of ATMs for logging a withdrawal without going through the map. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtmPickerSheet(atms: List<AtmUi>, onDismiss: () -> Unit, onPick: (AtmUi) -> Unit, modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    val sorted = remember(atms) { atms.sortedWith(compareBy({ it.quote.total }, { it.atm.name })) }
    val visible = remember(sorted, query) { sorted.filter { it.matches(query) } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxHeight(0.85f).navigationBarsPadding().imePadding()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(stringResource(R.string.picker_title), style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.picker_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                WwTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.map_search_hint),
                    leadingIcon = R.drawable.ic_search
                )
                Spacer(Modifier.height(8.dp))
            }
            LazyColumn(Modifier.fillMaxWidth()) {
                items(visible, key = { it.id }) { atm ->
                    AtmListRow(atm = atm, onClick = { onPick(atm) }, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}
