package net.sf.xapp.net.server.idgen;

import ngpoker.infrastructure.types.EntityType;

/**
 * Generates ids and persists sequences for unique ids for entities. The entity type is effectively a
 * namespace. Ids are unique for a node
 */
public interface IdSeqGenerator
{
    String nextId(EntityType entityType);

    long peek(EntityType entityType);
}
