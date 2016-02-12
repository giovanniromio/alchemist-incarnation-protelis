/*
 * Copyright (C) 2010-2015, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.nodes;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.protelis.lang.datatype.DeviceUID;
import org.protelis.vm.ExecutionEnvironment;
import org.protelis.vm.NetworkManager;

import com.google.common.collect.MapMaker;

import it.unibo.alchemist.model.ProtelisIncarnation;
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Molecule;
import it.unibo.alchemist.protelis.AlchemistNetworkManager;

/**
 */
public class ProtelisNode extends GenericNode<Object>implements DeviceUID, ExecutionEnvironment {

    private static final long serialVersionUID = 7411790948884770553L;
    private final Map<RunProtelisProgram, AlchemistNetworkManager> netmgrs = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Environment<?>, AtomicInteger> IDGENERATOR = new MapMaker()
            .weakKeys().makeMap();
    private final int id;

    /**
     * Builds a new {@link ProtelisNode}.
     * 
     * @param env the environment
     */
    public ProtelisNode(final Environment<?> env) {
        super(true);
        AtomicInteger idgen = IDGENERATOR.get(env);
        if (idgen == null) {
            idgen = new AtomicInteger();
            IDGENERATOR.put(env, idgen);
        }
        id = idgen.getAndIncrement();
    }

    @Override
    protected Object createT() {
        return null;
    }

    @Override
    public String toString() {
        return Long.toString(getId());
    }

    /**
     * Adds a new {@link NetworkManager}.
     * 
     * @param program
     *            the {@link RunProtelisProgram}
     * @param netmgr
     *            the {@link AlchemistNetworkManager}
     */
    public void addNetworkManger(final RunProtelisProgram program, final AlchemistNetworkManager netmgr) {
        netmgrs.put(program, netmgr);
    }

    /**
     * @param program
     *            the {@link RunProtelisProgram}
     * @return the {@link AlchemistNetworkManager} for this specific
     *         {@link RunProtelisProgram}
     */
    public AlchemistNetworkManager getNetworkManager(final RunProtelisProgram program) {
        Objects.requireNonNull(program);
        return netmgrs.get(program);
    }

    private static Molecule makeMol(final String id) {
        return ProtelisIncarnation.instance().createMolecule(id);
    }

    @Override
    public boolean has(final String id) {
        return contains(makeMol(id));
    }

    @Override
    public Object get(final String id) {
        return getConcentration(makeMol(id));
    }

    @Override
    public Object get(final String id, final Object defaultValue) {
        return Optional.ofNullable(get(id)).orElse(defaultValue);
    }

    @Override
    public boolean put(final String id, final Object v) {
        setConcentration(makeMol(id), v);
        return true;
    }

    @Override
    public Object remove(final String id) {
        final Object res = get(id);
        removeConcentration(makeMol(id));
        return res;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null && getClass().equals(o.getClass())) {
            return id == ((ProtelisNode) o).id;
        }
        return false;
    }

    @Override
    public void commit() {
    }

    @Override
    public void setup() {
    }

}
