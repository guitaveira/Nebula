package com.duo.nebula.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLoggedOut: () -> Unit,
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onPhotoPicked(uri) }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
            state.profile == null -> Text(
                text = state.errorMessage ?: "Não foi possível carregar este perfil.",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
            else -> {
                val profile = state.profile!!
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!state.isOwnProfile) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(text = profile.displayName, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .then(
                                    if (state.isOwnProfile) {
                                        Modifier.clickable { photoPicker.launch("image/*") }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                DefaultAvatarIcon(size = 48.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.isOwnProfile && state.isEditingName) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = state.nameDraft,
                                    onValueChange = viewModel::onNameDraftChanged,
                                    singleLine = true,
                                    label = { Text("Nome de usuário") },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = viewModel::saveName,
                                    enabled = !state.isSaving
                                ) {
                                    if (state.isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Salvar nome",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = viewModel::cancelEditingName,
                                    enabled = !state.isSaving
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Cancelar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.isOwnProfile) {
                                    IconButton(onClick = viewModel::startEditingName, modifier = Modifier.size(28.dp)) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Editar nome de usuário",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (state.isOwnProfile) {
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Contadores de seguidores/seguindo, visíveis tanto no próprio
                        // perfil quanto ao visitar o perfil de outra pessoa.
                        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                            FollowStat(count = state.posts.size, label = "Publicações")
                            FollowStat(count = profile.followersCount.toInt(), label = "Seguidores")
                            FollowStat(count = profile.followingCount.toInt(), label = "Seguindo")
                        }

                        if (!state.isOwnProfile) {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (state.isFollowing) {
                                OutlinedButton(
                                    onClick = viewModel::toggleFollow,
                                    enabled = !state.isTogglingFollow,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Seguindo")
                                }
                            } else {
                                Button(
                                    onClick = viewModel::toggleFollow,
                                    enabled = !state.isTogglingFollow,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Seguir")
                                }
                            }
                        }

                        if (state.isOwnProfile) {
                            if (state.isEditingBio) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = state.bioDraft,
                                        onValueChange = { if (it.length <= 150) viewModel.onBioDraftChanged(it) },
                                        label = { Text("Sua bio") },
                                        supportingText = {
                                            Text(
                                                text = "${state.bioDraft.length}/150",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Column {
                                        IconButton(
                                            onClick = viewModel::saveBio,
                                            enabled = !state.isSaving
                                        ) {
                                            if (state.isSaving) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "Salvar bio",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = viewModel::cancelEditingBio,
                                            enabled = !state.isSaving
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Cancelar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.startEditingBio() }
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = profile.bio.ifBlank { "Adicionar bio..." },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (profile.bio.isBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Editar bio",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = viewModel::logout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                                Text(text = "Sair da conta", modifier = Modifier.padding(start = 8.dp))
                            }
                        } else if (profile.bio.isNotBlank()) {
                            Text(
                                text = profile.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Text(
                        text = "Publicações",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                    )

                    if (state.posts.isEmpty()) {
                        Text(
                            text = if (state.isOwnProfile) "Você ainda não publicou nada." else "Nenhuma publicação ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            items(state.posts, key = { it.publication.id }) { post ->
                                AsyncImage(
                                    model = post.publication.imageUrl,
                                    contentDescription = post.publication.caption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Um número (seguidores, seguindo, publicações) com seu rótulo abaixo. */
@Composable
private fun FollowStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Avatar padrão exibido quando o perfil não tem foto: um fundo em gradiente
 * com um ícone de contorno, bem mais suave que o ícone genérico anterior.
 */
@Composable
private fun DefaultAvatarIcon(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PersonOutline,
            contentDescription = "Foto de perfil",
            modifier = Modifier.size(size * 0.55f),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
