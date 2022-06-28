package com.avlweb.collectionviewer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class CollectionPropertiesKt {
    @JsonProperty("properties")
    var properties: ArrayList<CollectionPropertyKt> = ArrayList()
}