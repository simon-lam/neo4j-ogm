/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.mapper;

import java.util.Map;

import org.neo4j.ogm.cypher.compiler.RelationshipBuilder;

/**
 * A TransientRelationship represents a relationship that is not yet
 * established in the graph, where at least one of either the
 * start node or end node is also a new object.
 *
 * Transient Relationships are recorded while the cypher request
 * to save the domain model is being being constructed, and they are saved
 * in the log of the transaction's current context for post-processing
 * after the save request completes.
 *
 * If the save succeeds, the ids of the two ends of the actual relationship
 * will now be fully known in the response. The start and end nodes of the transient
 * relationship (which were previously place holders) can now be
 * replaced with the correct node ids, and the new MappedRelationship
 * established in the session's mappingContext.
 *
 * @author Mark Angrish
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransientRelationship {

    private final String src;
    private final String tgt;
    private final String ref;
    private final String rel;
    private final Class srcClass;
    private final Class tgtClass;

    public TransientRelationship(String src, String ref, String rel, String tgt, Class srcClass, Class tgtClass) {
        this.src = src;
        this.tgt = tgt;
        this.ref = ref;
        this.rel = rel;
        this.srcClass = srcClass;
        this.tgtClass = tgtClass;
    }

    /**
     * Creates a MappedRelationship from a TransientRelationship
     * using the supplied refMap to lookup and replace the correct start and end node ids
     * @param refMap A Map containing refs to the src/tgt ids
     * @return the MappedRelationship
     */
    public MappedRelationship convert(Map<String, Long> refMap) {

        Long srcIdentity = src.startsWith("_") ? refMap.get(src) : (Long)Long.parseLong(src.substring(1));
        Long tgtIdentity = tgt.startsWith("_") ? refMap.get(tgt) : (Long)Long.parseLong(tgt.substring(1));
        Long relIdentity = ref.startsWith("_") ? refMap.get(ref) : (Long)Long.parseLong(ref.substring(1));

        if (srcIdentity == null) {
            throw new RuntimeException("Couldn't get identity for " + src);
        }

        if (tgtIdentity == null) {
            throw new RuntimeException("Couldn't get identity for " + tgt);
        }

        if (relIdentity == null) {
            throw new RuntimeException("Couldn't get identity for " + ref);
        }

        return new MappedRelationship(srcIdentity, rel, tgtIdentity, relIdentity, srcClass, tgtClass);
    }

    public boolean equalsIgnoreDirection(String src, RelationshipBuilder builder, String tgt) {
        Boolean singleton = builder.isSingleton();
        if (this.rel.equals(builder.getType())) {
            if (singleton) {
                if (this.src.equals(src) && this.tgt.equals(tgt)) {
                    return true;
                }
                if (this.src.equals(tgt) && this.tgt.equals(src)) {
                    return true;
                }
            }
        }
        return false;
    }
}
