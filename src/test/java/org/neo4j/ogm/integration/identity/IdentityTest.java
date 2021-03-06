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
package org.neo4j.ogm.integration.identity;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestServer;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */


public class IdentityTest {

    private static TestServer server = new TestServer();
    private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.integration.identity");

    private Session session;

    @Before
    public void init() {
        session = sessionFactory.openSession(server.url());
    }

    @Test
    public void shouldCreateRelationshipEntityWhenDifferentStartAndEndNodesAreHashCodeEqual() {

        Node start = new Node();
        Node end = new Node();

        // user code deliberately sets the nodes to be equal
        assertEquals(start, end);
        Set<Node> nodes = new HashSet();
        nodes.add(start);
        nodes.add(end);

        // same hashcode, so a single object, not two in set
        assertEquals(1, nodes.size());


        session.save(start);
        session.save(end);

        Edge edge = new Edge();

        edge.start = start;
        edge.end = end;

        start.link = edge;

        session.save(edge);

        session.clear();

        Node checkNode = session.load(Node.class, start.id);

        assertNotNull(checkNode.link);
        assertEquals(start.id, checkNode.link.start.id);
        assertEquals(end.id, checkNode.link.end.id);

    }

    @NodeEntity(label="NODE")
    public static class Node {

        Long id;

        @Relationship(type="EDGE")
        Edge link;

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            return true; // all Node objects are the same, from perspective of client code
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    @RelationshipEntity(type="EDGE")
    public static class Edge {

        Long id;

        @StartNode
        Node start;

        @EndNode
        Node end;

    }

}
