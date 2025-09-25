package com.example.spoonacularapi.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.spoonacularapi.presentation.components.ErrorScreen
import com.example.spoonacularapi.presentation.components.ProgressBar
import com.example.spoonacularapi.presentation.screens.home.components.CategoryItem
import com.example.spoonacularapi.presentation.screens.home.components.RecipeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val pagingData = viewModel.pagingData.collectAsLazyPagingItems()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val apiLimitReached by viewModel.apiLimitReached.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(pagingData.loadState) {
        when (val refreshState = pagingData.loadState.refresh) {
            is LoadState.Error -> {
                viewModel.setError("Ошибка загрузки: ${refreshState.error.message}")
                viewModel.setLoading(false)
            }
            is LoadState.NotLoading -> {
                viewModel.setLoading(false)
                viewModel.clearError()
            }
            is LoadState.Loading -> {
                viewModel.setLoading(true)
            }
        }
    }

    LaunchedEffect(pagingData.loadState.append) {
        when (val appendState = pagingData.loadState.append) {
            is LoadState.Error -> {
                viewModel.setError("Ошибка загрузки: ${appendState.error.message}")
            }
            is LoadState.NotLoading -> {
                viewModel.clearError()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Рецепты")
                        if (isOffline) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 8.dp),
                                tint = Color.Gray
                            )
                        }
                        if (apiLimitReached) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "API Limit",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 8.dp),
                                tint = Color.Yellow
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSearch = { keyboardController?.hide() },
                focusRequester = focusRequester,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(categories.size) { index ->
                        CategoryItem(
                            category = categories[index],
                            onCategoryClick = { category ->
                                viewModel.selectCategory(category)
                            }
                        )
                    }
                }
            }

            if (isOffline) {
                OfflineBanner(
                    onRetry = { viewModel.loadRecipesPaged() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> ProgressBar()
                    error != null -> {
                        ErrorScreen(
                            message = error!!,
                            onRetry = {
                                pagingData.retry()
                                viewModel.clearError()
                            }
                        )
                    }
                    pagingData.itemCount == 0 -> {
                        val message = when {
                            isSearching -> "По запросу \"$searchQuery\" ничего не найдено"
                            isOffline -> "Нет сохраненных рецептов. Подключитесь к интернету для загрузки."
                            apiLimitReached -> "Нет сохраненных рецептов. API лимит исчерпан."
                            else -> "Рецепты не найдены"
                        }
                        ErrorScreen(
                            message = message,
                            onRetry = {
                                pagingData.retry()
                                viewModel.clearError()
                            }
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                if (selectedCategory != null && !isSearching) {
                                    Text(
                                        text = "Категория: ${selectedCategory!!.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 16.dp
                                        )
                                    )
                                }
                            }

                            items(pagingData.itemCount) { index ->
                                val recipe = pagingData[index]
                                if (recipe != null) {
                                    RecipeItem(
                                        recipe = recipe,
                                        onItemClick = {
                                            navController.navigate("detail/${recipe.id}")
                                        }
                                    )
                                } else {
                                    RecipeItemPlaceholder()
                                }
                            }

                            if (pagingData.loadState.append is LoadState.Loading) {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .wrapContentWidth(Alignment.CenterHorizontally),
                                        color = Color(0xFF6200EE)
                                    )
                                }
                            }

                            if (pagingData.loadState.append is LoadState.Error) {
                                item {
                                    val errorState = pagingData.loadState.append as LoadState.Error
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Ошибка загрузки: ${errorState.error.message}",
                                            color = Color.Red,
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { pagingData.retry() },
                                            modifier = Modifier.height(40.dp)
                                        ) {
                                            Text("Повторить", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { Text("Поиск рецептов...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Поиск")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun OfflineBanner(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Offline",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Оффлайн режим",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            TextButton(
                onClick = onRetry,
                modifier = Modifier.height(32.dp)
            ) {
                Text("Обновить", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RecipeItemPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(24.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}