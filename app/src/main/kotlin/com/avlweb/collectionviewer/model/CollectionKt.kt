package com.avlweb.collectionviewer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class CollectionKt {
    @JsonProperty("content")
    val content: CollectionContentKt = CollectionContentKt()

    @JsonProperty("properties")
    val properties: CollectionPropertiesKt = CollectionPropertiesKt()

    @JsonProperty("items")
    val items: CollectionItemsKt = CollectionItemsKt()
}
