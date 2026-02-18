package com.thesunnahrevival.sunnahassistant.data.typeconverters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class BooleanAsIntDeserializer : JsonDeserializer<Boolean> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Boolean {
        return json.asInt == 1
    }
}