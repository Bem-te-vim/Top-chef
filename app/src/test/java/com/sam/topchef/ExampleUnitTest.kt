package com.sam.topchef

import com.sam.topchef.feature_import_from_tiktok.ia.RecipeInfoByIA
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRecipeInfoByIAInstantiation() {
        val recipeIA = RecipeInfoByIA()
        assertNotNull(recipeIA)
    }
}