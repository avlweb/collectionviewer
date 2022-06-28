package com.avlweb.collectionviewer.util

import com.avlweb.collectionviewer.model.CollectionPropertiesKt
import com.avlweb.collectionviewer.model.CollectionPropertyKt
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonFactory {

    companion object {

        val mapper = jacksonObjectMapper()

        @JvmStatic
        fun readJsonFile() {
        }

        @JvmStatic
        fun writeJsonFile(): String {
            val propertyList = ArrayList<CollectionPropertyKt>()
            val property1 = CollectionPropertyKt(1, "couleur", "zefz zefzef")
            val property2 = CollectionPropertyKt(2, "nom", "rthtrh zefzef")
            val property3 = CollectionPropertyKt(3, "série", "uikuiuk yu")
            propertyList.add(property1)
            propertyList.add(property2)
            propertyList.add(property3)
            val properties = CollectionPropertiesKt()
            properties.properties = propertyList
            return mapper.writeValueAsString(properties)
        }
    }
}