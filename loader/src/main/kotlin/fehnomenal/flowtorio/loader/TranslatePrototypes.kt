package fehnomenal.flowtorio.loader

import fehnomenal.flowtorio.common.*
import fehnomenal.flowtorio.loader.lua.LuaKey
import fehnomenal.flowtorio.loader.lua.LuaTable

private fun <K, V> Map<K, V>.getOrThrow(key: K) = this[key] ?: error("key '$key' does not exist")


internal fun translateItemPrototypes(itemsTable: LuaTable) = itemsTable.keys.map {
    val itemTable = itemsTable[it] as LuaTable
    val name = itemTable["name"] as String

    Item(name)
}


internal fun translateFluidPrototypes(fluidsTable: LuaTable) = fluidsTable.keys.map {
    val fluidTable = fluidsTable[it] as LuaTable
    val name = fluidTable["name"] as String

    Fluid(name)
}


internal fun translateRecipePrototypes(
    recipesTable: LuaTable,
    difficulty: Difficulty,
    items: Map<String, Item>,
    fluids: Map<String, Fluid>
) = recipesTable.keys.map {
    val recipeTable = recipesTable[it] as LuaTable

    when (difficulty) {
        Difficulty.NORMAL -> {
            when (recipeTable["normal"]) {
                false -> translateRecipePrototype(recipeTable, recipeTable["expensive"] as LuaTable, items, fluids)
                null -> translateRecipePrototype(recipeTable, recipeTable, items, fluids)
                else -> translateRecipePrototype(recipeTable, recipeTable["normal"] as LuaTable, items, fluids)
            }
        }
        Difficulty.EXPENSIVE -> {
            when (recipeTable["expensive"]) {
                false -> translateRecipePrototype(recipeTable, recipeTable["normal"] as LuaTable, items, fluids)
                null -> translateRecipePrototype(recipeTable, recipeTable, items, fluids)
                else -> translateRecipePrototype(recipeTable, recipeTable["expensive"] as LuaTable, items, fluids)
            }
        }
    }
}

private fun translateRecipePrototype(
    baseTable: LuaTable,
    difficultyTable: LuaTable,
    items: Map<String, Item>,
    fluids: Map<String, Fluid>
): Recipe {
    val energyRequired =
        ((difficultyTable["energy_required"]
            ?: baseTable["energy_required"]
            ?: 0.5) as? Number)!!.toDouble()

    val ingredientsTable =
        (difficultyTable["ingredients"]
            ?: baseTable["ingredients"]
            ?: error("'ingredients' not defined for recipe '${baseTable["name"]}'")) as LuaTable

    val ingredients by lazy {
        val keys = ingredientsTable.keys
        keys.map { translateIngredient(ingredientsTable[it] as LuaTable, items, fluids) }
    }

    val products: List<Product>

    val resultsTable =
        (difficultyTable["results"]
            ?: baseTable["results"]) as? LuaTable

    if (resultsTable == null) {
        val result =
            (difficultyTable["result"]
                ?: baseTable["result"]) as? String
                ?: error("neither 'result' nor 'results' defined for recipe '${baseTable["name"]}'")

        val resultCount =
            ((difficultyTable["result_count"]
                ?: baseTable["result_count"]
                ?: 1.0) as Number).toDouble()


        products = listOf(Product(items.getOrThrow(result), resultCount, resultCount, 1.0))
    } else {
        products = resultsTable.keys.map { translateProduct(resultsTable[it] as LuaTable, items, fluids) }
    }

    return Recipe(
        baseTable["name"] as String,
        energyRequired,
        ingredients,
        products
    )
}

private fun translateIngredient(t: LuaTable, items: Map<String, Item>, fluids: Map<String, Fluid>): Ingredient {
    val isArray = t.keys.all { it is LuaKey.Int }

    val material: Material
    val amount: Double

    if (isArray) {
        material = items.getOrThrow(t[1] as String)
        amount = (t[2] as Number).toDouble()
    } else {
        amount = (t["amount"] as Number).toDouble()

        when (t["type"]) {
            "item", null -> material = items.getOrThrow(t["name"] as String)
            "fluid" -> material = fluids.getOrThrow(t["name"] as String)
            else -> error("'type' has to be 'item' or 'fluid'; was '${t["type"]}'")
        }
    }

    return Ingredient(material, amount)
}

private fun translateProduct(t: LuaTable, items: Map<String, Item>, fluids: Map<String, Fluid>): Product {
    val isArray = t.keys.all { it is LuaKey.Int }

    val material: Material
    val amountMin: Double
    val amountMax: Double
    val probability: Double

    if (isArray) {
        material = items.getOrThrow(t[1] as String)
        amountMin = (t[2] as Number).toDouble()
        amountMax = amountMin
        probability = 1.0
    } else {
        when (t["type"]) {
            "item", null -> material = items.getOrThrow(t["name"] as String)
            "fluid" -> material = fluids.getOrThrow(t["name"] as String)
            else -> error("'type' has to be 'item' or 'fluid'; was '${t["type"]}'")
        }

        if (t["amount"] != null) {
            amountMin = (t["amount"] as Number).toDouble()
            amountMax = amountMin
        } else {
            amountMin = (t["amount_min"] as Number).toDouble()
            amountMax = (t["amount_max"] as Number).toDouble()
        }

        probability = ((t["probability"] ?: 1.0) as Number).toDouble()
    }

    return Product(material, amountMin, amountMax, probability)
}
