package com.avlweb.collectionviewer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class CollectionItemsKt {
    @JsonProperty("items")
    val items: ArrayList<CollectionItemKt> = ArrayList()
}
