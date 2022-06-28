package com.avlweb.collectionviewer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class CollectionItemKt {
    @JsonProperty("name")
    val name: String = "";

    @JsonProperty("description")
    val description: String = "";

    @JsonProperty("properties")
    val properties: ArrayList<String> = ArrayList()

    @JsonProperty("images")
    val images: ArrayList<String> = ArrayList()

    val selected: Boolean = false
    val positionInSelectedList: Int = 0
}
