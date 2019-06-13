package fehnomenal.flowtorio.common

data class Recipe(
    val name: String,
    val energyRequired: Double,
    val ingredients: List<Ingredient>,
    val products: List<Product>
) {
    init {
        ingredients.forEach { it.material.isIngredientOf += this }
        products.forEach { it.material.isProductOf += this }
    }
}
