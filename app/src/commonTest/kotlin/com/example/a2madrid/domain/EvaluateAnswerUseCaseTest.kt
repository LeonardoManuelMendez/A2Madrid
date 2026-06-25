package com.example.a2madrid.domain

import com.example.a2madrid.domain.model.Question
import com.example.a2madrid.domain.usecase.EvaluateAnswerUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluateAnswerUseCaseTest {

    private val useCase = EvaluateAnswerUseCase()
    private val question = Question(
        id = 1,
        text = "¿Capital de España?",
        options = listOf("Barcelona", "Madrid", "Valencia"),
        correctAnswerIndex = 1,
    )

    @Test
    fun `marks the correct option as correct`() {
        val result = useCase(question, selectedOptionIndex = 1)

        assertTrue(result.isCorrect)
        assertEquals(1, result.selectedOptionIndex)
    }

    @Test
    fun `marks a wrong option as incorrect`() {
        val result = useCase(question, selectedOptionIndex = 0)

        assertFalse(result.isCorrect)
        assertEquals(0, result.selectedOptionIndex)
    }
}