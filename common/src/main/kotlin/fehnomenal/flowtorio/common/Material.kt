package fehnomenal.flowtorio.common

abstract class Material {
    abstract val name: String

    val ingredientOf: List<Recipe> get() = isIngredientOf
    val productOf: List<Recipe> get() = isProductOf

    internal val isIngredientOf = mutableListOf<Recipe>()
    internal val isProductOf = mutableListOf<Recipe>()
}
