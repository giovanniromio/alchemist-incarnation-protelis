/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.danilopianini.lang.util.FasterString;
import org.protelis.lang.ProtelisLoader;
import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ExecutionContext;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.NetworkManager;
import org.protelis.vm.ProtelisProgram;
import org.protelis.vm.impl.AbstractExecutionContext;
import org.protelis.vm.util.CodePath;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule;
import it.unibo.alchemist.model.implementations.nodes.ProtelisNode;
import it.unibo.alchemist.model.implementations.timedistributions.DiracComb;
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.TimeDistribution;
import it.unibo.alchemist.model.interfaces.Action;
import it.unibo.alchemist.model.interfaces.Condition;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Incarnation;

/**
 */
public final class ProtelisIncarnation implements Incarnation<Object> {

    private static final String[] ANS_NAMES = { "ans", "res", "result", "answer", "val", "value" };
    private static final Set<FasterString> NAMES;

    private final Cache<String, Optional<ProtelisProgram>> cache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterAccess(1, TimeUnit.HOURS).expireAfterWrite(1, TimeUnit.HOURS).build();

    static {
        NAMES = Collections.unmodifiableSet(Arrays.stream(ANS_NAMES)
                .flatMap(n -> Arrays.stream(new String[] { n.toLowerCase(Locale.US), n.toUpperCase(Locale.US) }))
                .map(FasterString::new)
                .collect(Collectors.toSet()));
    }

    private static final ProtelisIncarnation SINGLETON = new ProtelisIncarnation();

    @Override
    public double getProperty(final Node<Object> node, final Molecule mol, final String prop) {
        Object val = node.getConcentration(mol);
        Optional<ProtelisProgram> prog = cache.getIfPresent(prop);
        if (prog == null) {
            try {
                prog = Optional.of(ProtelisLoader.parse(prop));
                cache.put(prop, prog);
            } catch (final RuntimeException e) {
                /*
                 * all fine, there is no program to evaluate.
                 */
                prog = Optional.empty();
                cache.put(prop, prog);
            }
        }
        val = preprocess(prog, val, node);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else if (val instanceof String) {
            if (val.equals(prop)) {
                return 1;
            }
            return 0;
        } else if (val instanceof Boolean) {
            final Boolean cond = (Boolean) val;
            if (cond) {
                return 1d;
            } else {
                return 0d;
            }
        }
        return Double.NaN;
    }

    private static Object preprocess(final Optional<ProtelisProgram> prog, final Object val, final Node<?> node) {
        try {
            if (prog.isPresent()) {
                final ExecutionContext ctx = new DummyContext(node);
                final ProtelisProgram program = prog.get();
                ctx.setup();
                NAMES.stream().forEach(n -> ctx.getExecutionEnvironment().put(n.toString(), val));
                program.compute(ctx);
                ctx.commit();
                return program.getCurrentValue();
            }
        } catch (final RuntimeException | Error e) {
            /*
             * Something went wrong, fallback.
             */
            return val;
        }
        return val;
    }

    @Override
    public Molecule createMolecule(final String s) {
        return new SimpleMolecule(s);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * @return an instance of a {@link ProtelisIncarnation}
     */
    public static ProtelisIncarnation instance() {
        return SINGLETON;
    }

    private static class DummyContext extends AbstractExecutionContext {
        private final Node<?> node;
        DummyContext(final Node<?> node) {
            super(new ExecutionEnvironment() {
                @Override
                public void setup() {
                }
                @Override
                public Object remove(final String id) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean put(final String id, final Object v) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean has(final String id) {
                    return node.contains(new SimpleMolecule(id));
                }
                @Override
                public Object get(final String id, final Object defaultValue) {
                    return Optional.<Object>ofNullable(get(id)).orElse(defaultValue);
                }
                @Override
                public Object get(final String id) {
                    return node.getConcentration(new SimpleMolecule(id));
                }
                @Override
                public void commit() {
                }
            }, new NetworkManager() {
                @Override
                public void shareState(final Map<CodePath, Object> toSend) {
                }
                @Override
                public Map<DeviceUID, Map<CodePath, Object>> getNeighborState() {
                    return Collections.emptyMap();
                }
            });
            this.node = node;
        }

        @Override
        public DeviceUID getDeviceUID() {
            if (node instanceof ProtelisNode) {
                return (ProtelisNode) node;
            }
            throw new IllegalStateException("You tried to compute a Protelis device UID, on a non-Protelis node");
        }
        @Override
        public Number getCurrentTime() {
            throw new UnsupportedOperationException();
        }
        @Override
        public double nextRandomDouble() {
            return Math.random();
        }
        @Override
        protected AbstractExecutionContext instance() {
            return this;
        }

    }

    @Override
    public Node<Object> createNode(final RandomGenerator rand, final Environment<Object> env, final String param) {
        return new ProtelisNode();
    }

    @Override
    public TimeDistribution<Object> createTimeDistribution(
            final RandomGenerator rand,
            final Environment<Object> env,
            final Node<Object> node,
            final String param) {
        if (param == null) {
            return new ExponentialTime<>(Double.POSITIVE_INFINITY, rand);
        }
        double frequency;
        try {
            frequency = Double.parseDouble(param);
        } catch (final NumberFormatException e) {
            frequency = 1;
        }
        return new DiracComb<>(new DoubleTime(rand.nextDouble() / frequency), frequency);
    }

    @Override
    public Reaction<Object> createReaction(final RandomGenerator rand, final Environment<Object> env,
            final Node<Object> node, final TimeDistribution<Object> time, final String param) {
//        final 
//        if ("send".equals(param)) {
//            
//        }
//        return new it.unibo.alchemist.model.implementations.actions.ProtelisProgram(
//                env,
//                (ProtelisNode) node, r, rand, prog);
        throw new UnsupportedOperationException("Needs to get implemented yet");
    }

    @Override
    public Condition<Object> createCondition(final RandomGenerator rand, final Environment<Object> env,
            final Node<Object> node, final TimeDistribution<Object> time, final Reaction<Object> reaction,
            final String param) {
        throw new UnsupportedOperationException("Needs to get implemented yet");
    }

    @Override
    public Action<Object> createAction(final RandomGenerator rand, final Environment<Object> env,
            final Node<Object> node, final TimeDistribution<Object> time, final Reaction<Object> reaction,
            final String param) {
        throw new UnsupportedOperationException("Needs to get implemented yet");
    }

}
