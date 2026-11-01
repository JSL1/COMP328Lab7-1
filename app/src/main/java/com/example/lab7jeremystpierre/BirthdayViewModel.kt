package com.example.lab7jeremystpierre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BirthdayViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-2.5-flash",
    )

    fun fetchFamousBirthdayTwin(userName: String, birthdate: String) {
        if (userName.isBlank() || birthdate.isBlank()) {
            _uiState.value = UiState.Error("Please enter both your name and birthdate.")
            return
        }

        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompt = """
                    My name is $userName, and my birthdate is $birthdate.
                    
                    Please identify ONE well-known famous person (such as an actor, scientist, historical figure, athlete, musician, or author) who shares my birthday (same month and day).
                    
                    Respond directly to $userName in a friendly, enthusiastic, and engaging tone:
                    1. State the famous person's full name, birth year, and profession.
                    2. Provide a brief overview of who they are and what they are most famous for.
                    3. Share an interesting or fun fact about them.
                    4. End with a fun personalized note or birthday connection for $userName.
                """.trimIndent()

                val response = generativeModel.generateContent(
                    content {
                        text(prompt)
                    }
                )

                val resultText = response.text
                if (!resultText.isNullOrBlank()) {
                    _uiState.value = UiState.Success(resultText)
                } else {
                    _uiState.value = UiState.Error("Gemini did not return any result. Please try again.")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "An error occurred while communicating with Gemini.")
            }
        }
    }
}