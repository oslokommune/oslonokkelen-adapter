package com.github.oslokommune.oslonokkelen.adapter.action

import com.github.oslokommune.oslonokkelen.adapter.proto.Adapter

data class ActionDescription(
    val id: ActionId,
    val description: String = "Action: ${id.short}",
    val requiredAttachmentTypes: Set<Adapter.AttachmentType> = emptySet(),
    val possibleOutputAttachmentTypes: Set<Adapter.AttachmentType> = emptySet(),
    val parameters: List<Adapter.AdapterManifest.ParameterDefinition> = emptyList()
)
