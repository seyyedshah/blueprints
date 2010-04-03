package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeTestSuite extends ModelTestSuite {

    public EdgeTestSuite() {
    }

    public EdgeTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testEdgeEquality(final Graph graph) {
        List<String> ids = generateIds(2);

        Vertex v = graph.addVertex(convertId(ids.get(0)));
        Vertex u = graph.addVertex(convertId(ids.get(1)));
        Edge e = graph.addEdge(null, v, u, convertId("test_label"));
        assertEquals(e, v.getOutEdges().iterator().next());
        assertEquals(e, u.getInEdges().iterator().next());
        assertEquals(v.getOutEdges().iterator().next(), u.getInEdges().iterator().next());
        Set<Edge> set = new HashSet<Edge>();
        set.add(e);
        set.add(e);
        set.add(v.getOutEdges().iterator().next());
        set.add(v.getOutEdges().iterator().next());
        set.add(u.getInEdges().iterator().next());
        set.add(u.getInEdges().iterator().next());
        if (config.supportsEdgeIteration)
            set.add(graph.getEdges().iterator().next());
        assertEquals(set.size(), 1);

    }

    public void testAddEdges(final Graph graph) {
        List<String> ids = generateIds(3);

        this.stopWatch();
        Vertex v1 = graph.addVertex(convertId(ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(ids.get(2)));
        graph.addEdge(null, v1, v2, convertId("knows"));
        graph.addEdge(null, v2, v3, convertId("pets"));
        graph.addEdge(null, v2, v3, convertId("cares_for"));
        assertEquals(1, count(v1.getOutEdges()));
        assertEquals(2, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(1, count(v2.getInEdges()));
        assertEquals(2, count(v3.getInEdges()));
        BaseTest.printPerformance(graph.toString(), 6, "elements added and checked", this.stopWatch());

    }

    public void testAddManyEdges(final Graph graph) {
        int edgeCount = 1000;
        int vertexCount = 2000;
        long counter = 0l;
        this.stopWatch();
        for (int i = 0; i < edgeCount; i++) {
            Vertex out = graph.addVertex(convertId("" + counter++));
            Vertex in = graph.addVertex(convertId("" + counter++));
            graph.addEdge(null, out, in, convertId(UUID.randomUUID().toString()));
        }
        BaseTest.printPerformance(graph.toString(), vertexCount + edgeCount, "elements added", this.stopWatch());
        if (config.supportsEdgeIteration) {
            this.stopWatch();
            assertEquals(edgeCount, count(graph.getEdges()));
            BaseTest.printPerformance(graph.toString(), edgeCount, "edges counted", this.stopWatch());
        }
        if (config.supportsVertexIteration) {
            this.stopWatch();
            assertEquals(vertexCount, count(graph.getVertices()));
            BaseTest.printPerformance(graph.toString(), vertexCount, "vertices counted", this.stopWatch());
            this.stopWatch();
            for (Vertex vertex : graph.getVertices()) {
                if (count(vertex.getOutEdges()) > 0) {
                    assertEquals(1, count(vertex.getOutEdges()));
                    assertFalse(count(vertex.getInEdges()) > 0);

                } else {
                    assertEquals(1, count(vertex.getInEdges()));
                    assertFalse(count(vertex.getOutEdges()) > 0);
                }
            }
            BaseTest.printPerformance(graph.toString(), vertexCount, "vertices checked", this.stopWatch());
        }
    }

    public void testRemoveManyEdges(final Graph graph) {
        long counter = 200000l;
        int edgeCount = 100;
        Set<Edge> edges = new HashSet<Edge>();
        for (int i = 0; i < edgeCount; i++) {
            Vertex out = graph.addVertex(convertId("" + counter++));
            Vertex in = graph.addVertex(convertId("" + counter++));
            edges.add(graph.addEdge(null, out, in, convertId("a" + UUID.randomUUID().toString())));
        }
        assertEquals(edgeCount, edges.size());

        if (config.supportsVertexIteration) {
            this.stopWatch();
            assertEquals(edgeCount * 2, count(graph.getVertices()));
            BaseTest.printPerformance(graph.toString(), edgeCount * 2, "vertices counted", this.stopWatch());
        }

        if (config.supportsEdgeIteration) {
            this.stopWatch();
            assertEquals(edgeCount, count(graph.getEdges()));
            BaseTest.printPerformance(graph.toString(), edgeCount, "edges counted", this.stopWatch());

            int i = edgeCount;
            this.stopWatch();
            for (Edge edge : edges) {
                graph.removeEdge(edge);
                i--;
                assertEquals(i, count(graph.getEdges()));
                if (config.supportsVertexIteration) {
                    int x = 0;
                    for (Vertex vertex : graph.getVertices()) {
                        if (count(vertex.getOutEdges()) > 0) {
                            assertEquals(1, count(vertex.getOutEdges()));
                            assertFalse(count(vertex.getInEdges()) > 0);
                        } else if (count(vertex.getInEdges()) > 0) {
                            assertEquals(1, count(vertex.getInEdges()));
                            assertFalse(count(vertex.getOutEdges()) > 0);
                        } else {
                            x++;
                        }
                    }
                    assertEquals((edgeCount - i) * 2, x);
                }
            }
            BaseTest.printPerformance(graph.toString(), edgeCount, "edges removed and graph checked", this.stopWatch());
        }
    }

    public void testAddingDuplicateEdges(final Graph graph) {

        List<String> ids = generateIds(3);

        Vertex v1 = graph.addVertex(convertId(ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(ids.get(2)));
        graph.addEdge(null, v1, v2, convertId("knows"));
        graph.addEdge(null, v2, v3, convertId("pets"));
        graph.addEdge(null, v2, v3, convertId("pets"));
        graph.addEdge(null, v2, v3, convertId("pets"));
        graph.addEdge(null, v2, v3, convertId("pets"));

        if (config.allowsDuplicateEdges) {
            if (config.supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (config.supportsEdgeIteration)
                assertEquals(5, count(graph.getEdges()));

            assertEquals(0, count(v1.getInEdges()));
            assertEquals(1, count(v1.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            assertEquals(4, count(v2.getOutEdges()));
            assertEquals(4, count(v3.getInEdges()));
            assertEquals(0, count(v3.getOutEdges()));
        } else {
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 3);
            if (config.supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 2);

            assertEquals(0, count(v1.getInEdges()));
            assertEquals(1, count(v1.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            assertEquals(1, count(v2.getOutEdges()));
            assertEquals(1, count(v3.getInEdges()));
            assertEquals(0, count(v3.getOutEdges()));
        }
    }

    public void testRemoveEdgesByRemovingVertex(final Graph graph) {
        List<String> ids = generateIds(3);

        Vertex v1 = graph.addVertex(convertId(ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(ids.get(2)));
        graph.addEdge(null, v1, v2, convertId("knows"));
        graph.addEdge(null, v2, v3, convertId("pets"));
        graph.addEdge(null, v2, v3, convertId("pets"));

        assertEquals(0, count(v1.getInEdges()));
        assertEquals(1, count(v1.getOutEdges()));
        assertEquals(1, count(v2.getInEdges()));
        assertEquals(0, count(v3.getOutEdges()));

        if (!config.ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(ids.get(0)));
            v2 = graph.getVertex(convertId(ids.get(1)));
            v3 = graph.getVertex(convertId(ids.get(2)));

            assertEquals(0, count(v1.getInEdges()));
            assertEquals(1, count(v1.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            assertEquals(0, count(v3.getOutEdges()));
        }

        if (config.supportsVertexIteration)
            assertEquals(3, count(graph.getVertices()));

        graph.removeVertex(v1);

        if (config.supportsVertexIteration)
            assertEquals(2, count(graph.getVertices()));

        if (config.allowsDuplicateEdges)
            assertEquals(2, count(v2.getOutEdges()));
        else
            assertEquals(1, count(v2.getOutEdges()));

        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v2.getInEdges()));

        if (config.allowsDuplicateEdges)
            assertEquals(2, count(v3.getInEdges()));
        else
            assertEquals(1, count(v3.getInEdges()));

    }

    public void testRemoveEdges(final Graph graph) {
        List<String> ids = generateIds(3);
        Vertex v1 = graph.addVertex(convertId(ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(ids.get(2)));
        Edge e1 = graph.addEdge(null, v1, v2, convertId("knows"));
        Edge e2 = graph.addEdge(null, v2, v3, convertId("pets"));
        Edge e3 = graph.addEdge(null, v2, v3, convertId("cares_for"));

        if (config.supportsVertexIteration)
            assertEquals(3, count(graph.getVertices()));

        graph.removeEdge(e1);
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(2, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(2, count(v3.getInEdges()));
        if (!config.ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(ids.get(0)));
            v2 = graph.getVertex(convertId(ids.get(1)));
            v3 = graph.getVertex(convertId(ids.get(2)));
        }
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(2, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(2, count(v3.getInEdges()));

        graph.removeEdge(e2);
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(1, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(1, count(v3.getInEdges()));
        if (!config.ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(ids.get(0)));
            v2 = graph.getVertex(convertId(ids.get(1)));
            v3 = graph.getVertex(convertId(ids.get(2)));
        }
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(1, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(1, count(v3.getInEdges()));

        graph.removeEdge(e3);
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(0, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(0, count(v3.getInEdges()));
        if (!config.ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(ids.get(0)));
            v2 = graph.getVertex(convertId(ids.get(1)));
            v3 = graph.getVertex(convertId(ids.get(2)));
        }
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(0, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(0, count(v3.getInEdges()));

    }

    public void testAddingSelfLoops(final Graph graph) {
        if (config.allowsSelfLoops) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(ids.get(1)));
            Vertex v3 = graph.addVertex(convertId(ids.get(2)));
            graph.addEdge(null, v1, v1, convertId("is_self"));
            graph.addEdge(null, v2, v2, convertId("is_self"));
            graph.addEdge(null, v3, v3, convertId("is_self"));

            if (config.supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (config.supportsEdgeIteration) {
                assertEquals(3, count(graph.getEdges()));
                int counter = 0;
                for (Edge edge : graph.getEdges()) {
                    counter++;
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
                assertEquals(counter, 3);
            }

        }
    }

    public void testRemoveSelfLoops(final Graph graph) {
        if (config.allowsSelfLoops) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(ids.get(1)));
            Vertex v3 = graph.addVertex(convertId(ids.get(2)));
            Edge e1 = graph.addEdge(null, v1, v1, convertId("is_self"));
            Edge e2 = graph.addEdge(null, v2, v2, convertId("is_self"));
            Edge e3 = graph.addEdge(null, v3, v3, convertId("is_self"));

            if (config.supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (config.supportsEdgeIteration) {
                assertEquals(3, count(graph.getEdges()));
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
            }

            graph.removeVertex(v1);
            if (config.supportsEdgeIteration) {
                assertEquals(2, count(graph.getEdges()));
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
            }

            assertEquals(1, count(v2.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            graph.removeEdge(e2);
            assertEquals(0, count(v2.getOutEdges()));
            assertEquals(0, count(v2.getInEdges()));

            if (config.supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 1);
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
            }
        }
    }

    public void testEdgeIterator(final Graph graph) {
        if (config.supportsEdgeIteration) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(ids.get(1)));
            Vertex v3 = graph.addVertex(convertId(ids.get(2)));
            Edge e1 = graph.addEdge(null, v1, v2, convertId("test"));
            Edge e2 = graph.addEdge(null, v2, v3, convertId("test"));
            Edge e3 = graph.addEdge(null, v3, v1, convertId("test"));

            if (config.supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (config.supportsEdgeIteration)
                assertEquals(3, count(graph.getEdges()));

            Set<String> edgeIds = new HashSet<String>();
            int count = 0;
            for (Edge e : graph.getEdges()) {
                count++;
                edgeIds.add(e.getId().toString());
                assertEquals(convertId("test"), e.getLabel());
                if (e.getId().toString().equals(e1.getId().toString())) {
                    assertEquals(v1, e.getOutVertex());
                    assertEquals(v2, e.getInVertex());
                } else if (e.getId().toString().equals(e2.getId().toString())) {
                    assertEquals(v2, e.getOutVertex());
                    assertEquals(v3, e.getInVertex());
                } else if (e.getId().toString().equals(e3.getId().toString())) {
                    assertEquals(v3, e.getOutVertex());
                    assertEquals(v1, e.getInVertex());
                } else {
                    assertTrue(false);
                }
                //System.out.println(e);
            }
            assertEquals(3, count);
            assertEquals(3, edgeIds.size());
            assertTrue(edgeIds.contains(e1.getId().toString()));
            assertTrue(edgeIds.contains(e2.getId().toString()));
            assertTrue(edgeIds.contains(e3.getId().toString()));
        }
    }
}
