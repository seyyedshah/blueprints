package com.tinkerpop.blueprints.pgm.util.wrappers.readonly;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.util.ReadOnlyIndexSequence;

import java.util.Set;

/**
 * A ReadOnlyIndexableGraph wraps an IndexableGraph and overrides the underlying graph's mutating methods.
 * In this way, a ReadOnlyIndexableGraph can only be read from, not written to.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndexableGraph<T extends IndexableGraph> extends ReadOnlyGraph<T> implements IndexableGraph, WrapperGraph<T> {

    public ReadOnlyIndexableGraph(final T baseIndexableGraph) {
        super(baseIndexableGraph);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void dropIndex(final String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys, final Parameter... indexParameters) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = this.baseGraph.getIndex(indexName, indexClass);
        if (index.getIndexType().equals(Index.Type.MANUAL))
            return new ReadOnlyIndex<T>(index);
        else
            return new ReadOnlyAutomaticIndex<T>((AutomaticIndex<T>) index);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new ReadOnlyIndexSequence(this.baseGraph.getIndices().iterator());
    }
}
