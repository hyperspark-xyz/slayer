package xyz.hyperspark.slayer.models

import org.bukkit.entity.EntityType

data class Kill(
    val id: PlayerId,
    val entityType: EntityType,
)
